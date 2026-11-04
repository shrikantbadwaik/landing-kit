package com.shrikantbadwaik.landingkit.core.data.local.backup

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest

/** Prevents ordinary repository access while startup recovery cannot establish a safe state. */
class BackupRecoveryAccess(initiallyReady: Boolean = true) {
    private val readyState = MutableStateFlow(initiallyReady)
    private val statusState = MutableStateFlow(
        if (initiallyReady) BackupRecoveryStatus.Ready else BackupRecoveryStatus.Recovering,
    )
    val readiness: StateFlow<Boolean> = readyState
    val status: StateFlow<BackupRecoveryStatus> = statusState
    val isReady: Boolean get() = readyState.value

    internal fun ready() {
        readyState.value = true
        statusState.value = BackupRecoveryStatus.Ready
    }
    internal fun recovering() {
        readyState.value = false
        statusState.value = BackupRecoveryStatus.Recovering
    }
    internal fun blocked() {
        readyState.value = false
        statusState.value = BackupRecoveryStatus.Failed
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun <T> guard(flow: Flow<T>): Flow<T> =
        readiness.flatMapLatest { if (it) flow else emptyFlow() }
    fun requireReady() {
        check(isReady) { "Saved travel information needs restore recovery before it can be opened" }
    }
}

enum class BackupRecoveryStatus { Recovering, Ready, Failed }
