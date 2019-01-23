package com.bukalapak.neuro

class AxonBranch(val expression: String,
                 val action: SignalAction
) : Comparable<AxonBranch> {

    private val comparedPattern by lazy {

        // because comma's ascii number is between alphabet and * and it's invalid charater in url
        expression.replace(VARIABLE_REGEX.toRegex(), ','.toString())
    }

    private val pattern by lazy {
        Regex(expression.toPattern(true))
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