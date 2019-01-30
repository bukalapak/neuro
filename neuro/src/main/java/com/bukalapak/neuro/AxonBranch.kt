package com.bukalapak.neuro

class AxonBranch(val expression: String,
                 val action: SignalAction
) : Comparable<AxonBranch> {

    private val comparedPattern: String by lazy {

        // because space's ascii number is smaller than all supported character in URL
        val char = " "
        expression.replace(ANY_VARIABLE, char)
    }

    private val pattern: Regex by lazy {
        Regex(expression.toPattern())
    }

    internal fun isMatch(path: String?): Boolean {
        val cleanPath = path ?: ""

        // if pattern is longer than path, it's an imposible match
        if (comparedPattern.length > cleanPath.length) return false

        return pattern.matches(cleanPath)
    }
    /**
     * Priorities:
     * #1 length
     * #2 alphabetical
     */
    override fun compareTo(other: AxonBranch): Int {
        val pattern1 = comparedPattern
        val pattern2 = other.comparedPattern

        return if (pattern1.length == pattern2.length) {
            pattern2.compareTo(pattern1)
        } else pattern2.length - pattern1.length
    }

    override fun toString(): String {
        return expression
    }
}