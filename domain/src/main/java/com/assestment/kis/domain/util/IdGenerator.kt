package com.assestment.kis.domain.util

/** Injected id source so session creation is deterministic in tests. */
fun interface IdGenerator {
    fun newId(): String
}
