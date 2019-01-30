package com.bukalapak.neuro

import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet

typealias SignalAction = (Signal) -> Unit
typealias AxonPreprocessor = (AxonProcessor, SignalAction, Signal) -> Unit
typealias AxonProcessor = (SignalAction, Signal) -> Unit
typealias NeuronRoute = Pair<Nucleus.Chosen, AxonBranch?>?
typealias AxonTerminal = ConcurrentSkipListMap<Int, ConcurrentSkipListSet<AxonBranch>>

private const val COMMON_PATTERN = """[^/]+"""

val ANONYMOUS_REGEX = """<>""".toRegex()
val UNPATTERNED_REGEX = """<\w+>""".toRegex()
val PATTERNED_REGEX = """<\w+:[^>]+>""".toRegex()

val ANY_VARIABLE = """<[^>]*>""".toRegex()
val LITERAL_STRING_REGEX = """(^|>)([^<]+)""".toRegex()
val VARIABLE_ABLE_REGEX = """<(\w+)(:([^>]+))?>""".toRegex()

internal fun String.toPattern(): String {
    return this.replace(LITERAL_STRING_REGEX) {

        // add literal string boundary
        val prefix = it.groups[1]?.value ?: ""
        val value = it.groups[2]?.value ?: ""
        """$prefix\Q$value\E"""
    }
            .replace(ANONYMOUS_REGEX, COMMON_PATTERN) // unspecified regex
            .replace(UNPATTERNED_REGEX, "($COMMON_PATTERN)") // variable with no specific regex
            .replace(VARIABLE_ABLE_REGEX, "($3)") // variable with specific regex
}