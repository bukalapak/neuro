package com.bukalapak.neuro

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.regex.Pattern

object Neuro {

    private const val TAG = "Neuro"

    internal val neurons = ConcurrentSkipListMap<Nucleus, AxonTerminal>()
    var preprocessor: AxonPreprocessor? = null

    // this comparator used to priority sorting for the terminal index based on path count
    // from: -2, -1, -3, 5, 4, 1, -4, 3, 2, -5
    // to: 5, 4, 3, 2, 1, -5, -4, -3, -2, -1
    private val terminalComparator = Comparator<Int> { index1, index2 ->
        when {
            index1 < 0 && index2 < 0 -> index1 - index2
            else -> index2 - index1
        }
    }

    fun connect(soma: Soma, branches: List<AxonBranch>) {
        if (branches.any { it.expression.isBlank() }) throw IllegalArgumentException("One/more of branch expression is blank")

        val terminal = neurons[soma] ?: let {
            val newTerminal = AxonTerminal(terminalComparator)
            neurons[soma] = newTerminal
            newTerminal
        }

        branches.forEach { branch ->
            val cleanExpression = branch.expression.removePrefix("/")

            // use path count as index to save in hashmap
            // path: /aaa/bbb/ccc/ddd
            // literalPathCount: 4
            val literalPathCount = cleanExpression.split('/').size

            // because specifix regex might contain regex to accept / and it will make path count might be increased
            val hasPatternedRegex = cleanExpression.contains(PATTERNED_REGEX)

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

    fun proceed(url: String,
                context: Context? = null,
                axonProcessor: AxonProcessor? = null,
                args: Bundle = Bundle(),
                route: NeuronRoute = null
    ) {
        Log.i(TAG, "Starting time: " + System.currentTimeMillis().toString())
        Log.i(TAG, "Transporting url $url")
        val uri = url.toOptimizedUri() ?: throw IllegalArgumentException("Url is not valid")
        val parts = route ?: findRoute(uri)

        if (parts == null) {
            Log.e(TAG, "Url $url has no route")
            return
        } else {
            Log.i(TAG, "Routing via ${parts.first.nucleus} and ${parts.second}")
        }

        val chosenNucleus = parts.first
        val nucleus = chosenNucleus.nucleus
        val branch = parts.second

        val signal = extractSignal(chosenNucleus, context, branch, uri, args)

        if (signal == null) {
            Log.e(TAG, "Url $url has no successful match")
            return
        }

        when (nucleus) {
            is Soma -> {
                val transportDone = nucleus.onSomaProcess(signal)

                // check whether Soma need to forward to AxonBranch or not
                if (transportDone) {
                    Log.i(TAG, "Nucleus transporter returned false")
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

        Log.i(TAG, "Finishing time: " + System.currentTimeMillis().toString())
        usedAxonPreprocessor.invoke(
                usedAxonProcessor,
                branch.action,
                signal
        )
    }

    fun findRoute(uri: Uri): NeuronRoute {
        val scheme = uri.scheme
        val host = uri.host
        val port = uri.port
        val path = uri.path

        // find matched nucleus
        val chosenNucleus = neurons.keys.asSequence().find {
            it.isMatch(scheme, host, port)
        }?.let {
            it.nominate(scheme, host, port)
        } ?: return null

        val nucleus = chosenNucleus.nucleus

        val chosenTerminal = neurons[nucleus] ?: return null

        // find matched branch
        val branch = when (nucleus) {
            is SomaOnly -> null
            is Soma -> {
                val pathCount = uri.pathSegments.size

                if (pathCount == 0) {
                    nucleus.noBranchAction
                } else {
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

        return chosenNucleus to branch
    }

    // remove path's last slash
    private fun String.toOptimizedUri(): Uri? {
        val uri = Uri.parse(this)
        val path = uri.path
        return if (path?.endsWith('/') == true) {
            val optimizedPath = path.removeSuffix("/")
            val usedPath = if (optimizedPath.isNotEmpty()) optimizedPath else null
            Uri.Builder()
                    .scheme(uri.scheme)
                    .authority(uri.authority)
                    .fragment(uri.fragment)
                    .query(uri.query)
                    .path(usedPath)
                    .build()
        } else uri
    }

    private fun String.adaptWithLiteral() = """\E$this\Q"""

    private fun extractSignal(chosenNucleus: Nucleus.Chosen,
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
            append(branch?.expression ?: "(?:/.+)?".adaptWithLiteral())
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
        if (!matcher.matches()) return null

        val variables = OptWave()
        variableNames.forEachIndexed { index, name ->
            val value = matcher.group(index + 1)
            variables.put(name, value)
        }

        // collect the fragment
        val fragment = uri.fragment

        // collect the queries
        val queries = OptWaves()
        if (!uri.query.isNullOrEmpty()) {
            try {
                val names = uri.queryParameterNames // It crashes in very long query value

                // put into the container
                names.forEach {
                    queries[it] = uri.getQueryParameters(it)
                }
            } catch (ignored: Exception) {
                // exception never been catched, internal bug from android (?)
            }
        }

        return Signal(context, uri, uri.toString(), variables, queries, fragment, args)
    }
}