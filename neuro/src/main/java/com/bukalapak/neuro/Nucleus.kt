package com.bukalapak.neuro

sealed class Nucleus(val id: String) : Comparable<Nucleus> {

    // pattern to expression, sort descending from the longest pattern
    private val schemePatterns: List<Pair<Regex, String>> by lazy {
        schemes.map { it.toPattern() to it }
                .sortedByDescending { it.first }
                .map { Regex(it.first) to it.second }
    }

    // pattern to expression, sort descending from the longest pattern
    private val hostPatterns: List<Pair<Regex, String>> by lazy {
        hosts.map { it.toPattern() to it }
                .sortedByDescending { it.first }
                .map { Regex(it.first) to it.second }
    }

    // only return boolen
    internal fun isMatch(scheme: String?,
                         host: String?,
                         port: Int
    ): Boolean {

        val hostMatch = hostPatterns.isEmpty() || hostPatterns.any {
            it.first.matches(host ?: "")
        }
        if (!hostMatch) return false

        val schemeMatch = schemePatterns.isEmpty() || schemePatterns.any {
            it.first.matches(scheme ?: "")
        }
        if (!schemeMatch) return false

        val portMatch = ports.isEmpty() || ports.contains(port)
        if (!portMatch) return false

        // all passed, means all is match
        return true
    }

    // return matched nucleus
    internal fun nominate(scheme: String?,
                          host: String?,
                          port: Int
    ): Chosen? {

        val chosenHost = if (hostPatterns.isEmpty()) null
        else hostPatterns.find {
            it.first.matches(host ?: "")
        }?.let {
            it.second
        }

        val chosenScheme = if (schemePatterns.isEmpty()) null
        else schemePatterns.find {
            it.first.matches(scheme ?: "")
        }?.let {
            it.second
        }

        val chosenPort = if (ports.isEmpty()) null
        else if (ports.contains(port)) port
        else return null

        // all passed, means all is match
        return Chosen(this, chosenScheme, chosenHost, chosenPort)
    }

    internal val memberCount: Int by lazy {
        val schemeSize = if (schemes.isEmpty()) 1 else schemes.size
        val hostSize = if (hosts.isEmpty()) 1 else hosts.size
        val portSize = if (ports.isEmpty()) 1 else ports.size

        schemeSize * hostSize * portSize
    }

    /**
     * Priorities:
     * #1 lowest priority number
     * #2 highest member count
     * #3 alphabetically by id
     */
    final override fun compareTo(other: Nucleus): Int {
        val priority1 = priority
        val priority2 = other.priority

        return if (priority1 == priority2) {
            val memberCount1 = memberCount
            val memberCount2 = other.memberCount

            if (memberCount1 == memberCount2) {
                val id1 = id
                val id2 = other.id

                id1.compareTo(id2)
            } else memberCount2 - memberCount1
        } else priority1 - priority2
    }

    // empty means may be included or not
    open val schemes: List<String> = emptyList()

    // empty means may be included or not
    open val hosts: List<String> = emptyList()

    // empty means may be included or not
    open val ports: List<Int> = emptyList()

    open val priority: Int = Int.MAX_VALUE - 1 // because SomaFallback should be the lowest priority

    class Chosen(val nucleus: Nucleus,
                 val scheme: String?,
                 val host: String?,
                 val port: Int?
    )

    final override fun toString(): String = id

    final override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is Nucleus) {
            return false
        }
        return id == other.id

    }

    final override fun hashCode(): Int = 31 * id.hashCode()
}

abstract class Soma(id: String) : Nucleus(id) {

    internal val noBranchAction: AxonBranch by lazy { AxonBranch("") { onProcessNoBranch(it) } }
    internal val otherBranchAction: AxonBranch by lazy { AxonBranch("/<path:.+>") { onProcessOtherBranch(it) } }

    // do return false if you want to forward action to AxonBranch
    open fun onSomaProcess(signal: Signal): Boolean = false

    // onSomaProcess must return false to be processed here
    open fun onProcessNoBranch(signal: Signal) = Unit

    // onSomaProcess must return false to be processed here
    open fun onProcessOtherBranch(signal: Signal) = Unit
}

abstract class SomaOnly(id: String) : Nucleus(id) {

    open fun onSomaProcess(signal: Signal) = Unit
}

abstract class SomaFallback : SomaOnly("*") {

    final override val schemes = super.schemes
    final override val hosts = super.hosts
    final override val ports = super.ports
    final override val priority: Int = Int.MAX_VALUE
}