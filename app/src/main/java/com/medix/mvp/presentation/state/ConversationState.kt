package com.medix.mvp.presentation.state

import com.medix.mvp.domain.model.FlowContext
import com.medix.mvp.domain.model.Proposal

sealed class ConversationState {
    data object Welcome : ConversationState()
    data object AskCedula : ConversationState()
    data object Ready : ConversationState()
    data class SchedulingFlow(val context: FlowContext) : ConversationState()
    data class ConfirmingFlow(val context: FlowContext, val proposal: Proposal) : ConversationState()
    data class CancelFlow(val context: FlowContext) : ConversationState()
    data class RescheduleFlow(val context: FlowContext) : ConversationState()
    data object SessionEnded : ConversationState()
    data class ErrorState(val reason: String) : ConversationState()
}

sealed class MedixUiState {
    data object Idle : MedixUiState()
    data class Listening(val partialText: String) : MedixUiState()
    data class Processing(val finalText: String) : MedixUiState()
    data class Speaking(val text: String) : MedixUiState()
    data class Error(val message: String, val suggestion: String) : MedixUiState()
}
