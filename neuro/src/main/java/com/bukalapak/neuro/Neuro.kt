package com.bukalapak.neuro

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import java.util.Locale
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.regex.Pattern

class Neuro {

    internal val neurons = ConcurrentSkipListMap<Nucleus, AxonTerminal>()
    var preprocessor: AxonPreprocessor? = null
    var logger: Logger? = Logger.DEFAULT

    // this comparator used to priority sorting for the terminal index based on path count
    // from: -2, -1, -3, 5, 4, 1, -4, 3, 2, -5
    // to: 5, 4, 3, 2, 1, -5, -4, -3, -2, -1
    private val terminalComparator: Comparator<Int> = Comparator { index1, index2 ->
        when {
            index1 < 0 && index2 < 0 -> index1 - index2
            else -> index2 - index1
        }
    }

    @Synchronized
    fun connect(soma: Soma, branches: List<AxonBranch>) {
        val expression = branches.map { it.expression }
        val blacklistedExpression = listOf(
            Soma.EXPRESSION_NO_BRANCH,
            Soma.EXPRESSION_NO_BRANCH_WITH_SLASH,
            Soma.EXPRESSION_OTHER_BRANCH
        )
        val diff = expression - blacklistedExpression
        if (diff.size < expression.size) {
            Log.e(
                TAG,
                "Please move your logic of expression $diff to " +
                    "Soma.onProcessNoBranch() and/or Soma.onProcessOtherBranch()"
            )
            return
        }

        val terminal = neurons[soma] ?: let {
            val newTerminal = AxonTerminal(terminalComparator)
            neurons[soma] = newTerminal
            newTerminal
        }

        branches.forEach { branch ->
            // use path count as index to save in hashmap
            // path: /aaa/bbb/ccc/ddd
            // literalPathCount: 4
            val literalPathCount = branch.expression.split('/').size - 1

            // because specific regex might contain regex to accept / and it will make path count might be increased
            val hasPatternedRegex = branch.expression.contains(PATTERNED_REGEX)

            // if it has wildcard, it means that the path count might be more than it should be, saved at index minus
            val usedIndex = if (hasPatternedRegex) -literalPathCount else literalPathCount

            val usedBranches = terminal[usedIndex] ?: let {
                val newBranches = ConcurrentSkipListSet<AxonBranch>()
                terminal[usedIndex] = newBranches
                newBranches
            }
            usedBranches.add(branch)
        }
    }

    fun connect(soma: Soma, branch: AxonBranch) {
        connect(soma, listOf(branch))
    }

    fun connect(soma: SomaOnly) {
        // only dummy, because ConcurrentSkipListMap can't accept null value
        val dummy = AxonTerminal()
        neurons[soma] = dummy
    }

    @JvmOverloads
    fun proceed(
        url: String,
        decision: RouteDecision?,
        context: Context? = null,
        axonProcessor: AxonProcessor? = null,
        args: Bundle = Bundle()
    ) {
        proceedInternal(url, decision, context, axonProcessor, args)
    }

    @JvmOverloads
    fun proceed(
        url: String,
        context: Context? = null,
        axonProcessor: AxonProcessor? = null,
        args: Bundle = Bundle()
    ) {
        proceedInternal(url, findRoute(url), context, axonProcessor, args)
    }

    private fun proceedInternal(
        url: String,
        decision: RouteDecision?,
        context: Context? = null,
        axonProcessor: AxonProcessor? = null,
        args: Bundle = Bundle()
    ) {
        logger?.onRoutingUrl(url)

        if (decision == null) {
            logger?.onUrlHasNoResult(url)
            return
        } else {
            logger?.onUrlHasResult(url, decision.first.nucleus, decision.second)
        }

        val chosenNucleus = decision.first
        val nucleus = chosenNucleus.nucleus
        val branch = decision.second
        val uri = decision.third

        val signal = extractSignal(chosenNucleus, context, branch, uri, args) ?: return

        when (nucleus) {
            is Soma -> {
                val transportDone = nucleus.onSomaProcess(signal)

                // check whether Soma need to forward to AxonBranch or not
                if (transportDone) {
                    logger?.onNucleusReturnedFalse(url)
                    return
                }
            }
            is SomaOnly -> {
                nucleus.onSomaProcess(signal)
            }
        }

        if (branch == null) return

        val usedAxonPreprocessor = this.preprocessor ?: { _axonProcessor, _action, _signal ->
            _axonProcessor.invoke(_action, _signal)
        }
        val usedAxonProcessor = axonProcessor ?: { _action, _signal ->
            _action.invoke(_signal)
        }

        usedAxonPreprocessor.invoke(
            usedAxonProcessor,
            branch.action,
            signal
        )
    }

    fun findRoute(url: String): RouteDecision? {
        val uri = url.toOptimizedUri() ?: throw IllegalArgumentException("Url is not valid")

        logger?.onFindRouteStarted(url)

        val scheme = uri.scheme
        val host = uri.host
        val port = uri.port
        val path = uri.path

        // find matched nucleus
        val chosenNucleus = neurons.keys.asSequence().find {
            it.isMatch(scheme, host, port)
        }?.nominate(scheme, host, port) ?: return null

        val nucleus = chosenNucleus.nucleus

        val chosenTerminal = neurons[nucleus] ?: return null

        // find matched branch
        val branch = when (nucleus) {
            is SomaOnly -> null
            is Soma -> {
                val pathCount = uri.path?.let {
                    it.split('/').size - 1
                }

                when {
                    pathCount == null || pathCount == 0 -> {
                        nucleus.noBranchAction
                    }
                    path == Soma.EXPRESSION_NO_BRANCH_WITH_SLASH -> {
                        nucleus.noBranchWithSlashAction
                    }
                    else -> {
                        // find all possible branches for prioritizing
                        // pathCount: 5
                        // possibleBranches: 5, -5, -4, -3, -2, -1
                        val possibleBranches = chosenTerminal
                            .filter { (index, _) ->
                                index == pathCount || (index >= -pathCount && index < 0)
                            }
                            .map { (_, branches) -> branches }
                            .flatten()

                        val matchedBranch = possibleBranches.find {
                            it.isMatch(path)
                        }
                        matchedBranch ?: nucleus.otherBranchAction
                    }
                }
            }
        }

        logger?.onFindRouteFinished(url)

        return Triple(chosenNucleus, branch, uri)
    }

    // convert to lowercase for scheme and authority
    private fun String.toOptimizedUri(): Uri? {
        val uri = Uri.parse(this)
        val encodedPath = uri.encodedPath

        return if (uri.isOpaque) uri else { // if like mailto:aaa@bbb.com
            uri.buildUpon()
                .scheme(uri.scheme?.toLowerCase(Locale.ROOT))
                .encodedAuthority(uri.encodedAuthority?.toLowerCase(Locale.ROOT))
                .encodedPath(encodedPath)
                .build()
        }
    }

    private fun String.adaptWithLiteral() = """\E$this\Q"""

    private fun extractSignal(
        chosenNucleus: Nucleus.Chosen,
        context: Context?,
        branch: AxonBranch?,
        uri: Uri,
        args: Bundle
    ): Signal? {

        // build the final expression, if null, means that its optional, might be written or not
        val expression = StringBuilder().apply {
            append(chosenNucleus.scheme?.let { "$it://" } ?: "(?:[^:]*://)?".adaptWithLiteral())
            append(chosenNucleus.host?.let { it } ?: "(?:[^/|:]+)?".adaptWithLiteral())
            append(chosenNucleus.port?.let { ":$it" } ?: "(?::[^/]*)?".adaptWithLiteral())
            append(branch?.expression ?: "(?:/.*)?".adaptWithLiteral())
        }.toString()

        val cleanUrl = uri.toString()
            .split('#').first()
            .split('?').first()

        val pattern = expression.toPattern()

        val matcher = Pattern.compile(pattern).matcher(cleanUrl)

        val variableNames = mutableListOf<String>()

        val variableMatcher = VARIABLE_ABLE_REGEX.toPattern().matcher(expression)

        // collect variable names from expression
        while (variableMatcher.find()) {
            variableNames.add(variableMatcher.group(1))
        }

        // collect the variables
        if (!matcher.matches()) { // may happen if regex for variable is invalid
            Log.e(TAG, "Regex is invalid")
            return null
        }

        val variables = OptWave()
        variableNames.forEachIndexed { index, name ->
            val value = matcher.group(index + 1)
            variables.put(name, value)
        }

        // collect the fragment
        val fragment = uri.fragment

        // collect the queries
        val queries = OptWaves()
        if (!uri.query.isNullOrEmpty() && uri.isHierarchical) { // TODO: parse query for opaque form
            val names = uri.queryParameterNames

            // put into the container
            names.forEach {
                queries[it] = uri.getQueryParameters(it)
            }
        }

        return Signal(context, uri, uri.toString(), variables, queries, fragment, args)
    }

    fun clearConnection() {
        neurons.clear()
    }

    companion object {
        internal const val TAG = "Neuro"
    }
}
