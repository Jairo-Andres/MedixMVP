package com.medix.mvp.domain.usecase

object ResponseTemplates {
    val saludos = listOf(
        "Hola, soy Medix. Estoy para ayudarte con tus citas médicas.",
        "Hola, soy Medix. Te ayudo a gestionar tus citas.",
    )
    val pedirCedula = listOf(
        "Por favor, dime tu cédula para iniciar.",
        "Listo, empecemos. ¿Cuál es tu cédula?",
    )
    val pedirFecha = listOf("Perfecto, ¿para qué día la necesitas?", "Claro. ¿Qué fecha prefieres?")
    val pedirHora = listOf("De acuerdo, ¿a qué hora te gustaría?", "Perfecto, dime una hora.")
    val pedirDoctor = listOf("¿Con qué doctor o especialidad prefieres?", "Listo, ¿con qué doctor te agendo?")
    val pedirLugar = listOf("¿En qué sede o lugar la prefieres?", "Perfecto, dime la sede o consultorio.")
    val noEntendi = listOf("No te entendí bien, ¿me lo repites?", "Perdón, ¿puedes decirlo de otra forma?")
    val despedida = listOf("Listo, cerré la sesión.", "De acuerdo, terminé esta sesión.")

    fun pick(options: List<String>, turn: Int): String = options[turn % options.size]
}
