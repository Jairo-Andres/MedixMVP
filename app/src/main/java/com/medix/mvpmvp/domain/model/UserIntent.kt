package com.medix.mvpmvp.domain.model

import java.time.LocalDate
import java.time.LocalTime

sealed class UserIntent {
    data class Agendar(val date: LocalDate?, val time: LocalTime?) : UserIntent()
    data object Confirmar : UserIntent()
    data object Cancelar : UserIntent()
    data class Reprogramar(val date: LocalDate?, val time: LocalTime?) : UserIntent()
    data object CambiarCedula : UserIntent()
    data object Listar : UserIntent()
    data object Ayuda : UserIntent()
    data class ProveerCedula(val cedula: String) : UserIntent()
    data class SeleccionarHora(val time: LocalTime) : UserIntent()
    data object Desconocido : UserIntent()
}
