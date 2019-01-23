package com.bukalapak.neuro

import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.regex.Pattern

typealias SignalAction = (Signal) -> Unit
typealias AxonPreprocessor = (AxonProcessor, SignalAction, Signal) -> Unit
typealias AxonProcessor = (SignalAction, Signal) -> Unit
typealias NeuronRoute = Pair<Nucleus.Chosen, AxonBranch?>?
typealias AxonTerminal = ConcurrentSkipListMap<Int, ConcurrentSkipListSet<AxonBranch>>

const val VARIABLE_REGEX = "<(\\w+)(:([^>]+))?>"
private const val DEFAULT_VARIABLE_REGEX = "[^\\/]+"

// TODO: remove extraBackslash if dynamic deeplink test has passed
internal fun String.toPattern(extraBackslash: Boolean = false): String {

    var regex = this

    if (extraBackslash) {
        regex = regex.replace("\\", "\\\\")
    }

    regex = regex
            .replace("\\.".toRegex(), "\\\\.") // literally dot
            .replace("\\*".toRegex(), ".+") // wildcard
            .replace("<\\w+>".toRegex(), "($DEFAULT_VARIABLE_REGEX)") // variable with no specific regex

    val varRegexMatcher = Pattern.compile(VARIABLE_REGEX).matcher(regex)

    // sanitize variable in expression with specific regex
    while (varRegexMatcher.find()) {
        regex = regex.replaceFirst(VARIABLE_REGEX.toRegex(), "(${varRegexMatcher.group(3)})")
    }

    return regex
}