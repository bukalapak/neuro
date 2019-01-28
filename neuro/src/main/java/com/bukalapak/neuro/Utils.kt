package com.bukalapak.neuro

import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.regex.Pattern

typealias SignalAction = (Signal) -> Unit
typealias AxonPreprocessor = (AxonProcessor, SignalAction, Signal) -> Unit
typealias AxonProcessor = (SignalAction, Signal) -> Unit
typealias NeuronRoute = Pair<Nucleus.Chosen, AxonBranch?>?
typealias AxonTerminal = ConcurrentSkipListMap<Int, ConcurrentSkipListSet<AxonBranch>>

private const val COMMON_PATTERN = "[^\\/]+"

val ANONYMOUS_REGEX = "<>".toRegex()
val UNPATTERNED_REGEX = "<\\w+>".toRegex()
val PATTERNED_REGEX = "<\\w+:[^>]+>".toRegex()

const val VARIABLE_ABLE_PATTERN = "<(\\w+)(:([^>]+))?>"

internal fun String.toPattern(): String {

    var regex = this

    // add extra backslash
    regex = regex.replace("\\", "\\\\")

    // literally dot
    regex = regex.replace("\\.".toRegex(), "\\\\.")

    // unspecified regex
    regex = regex.replace(ANONYMOUS_REGEX, COMMON_PATTERN)

    // variable with no specific regex
    regex = regex.replace("<\\w+>".toRegex(), "($COMMON_PATTERN)")

    // variable with specific regex
    val varRegexMatcher = Pattern.compile(VARIABLE_ABLE_PATTERN).matcher(regex)
    while (varRegexMatcher.find()) {
        regex = regex.replaceFirst(VARIABLE_ABLE_PATTERN.toRegex(), "(${varRegexMatcher.group(3)})")
    }

    return regex
}