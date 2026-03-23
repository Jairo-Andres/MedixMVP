# Medix Voice Assistant (Android)

Aplicación Android construida en Kotlin + Jetpack Compose para gestionar conversaciones por voz con backend IA de Medix y agendar citas médicas.

## Stack
- Kotlin
- Jetpack Compose
- ViewModel + StateFlow
- Retrofit + OkHttp
- WebSocket
- Coroutines
- MediaRecorder
- TextToSpeech

## Estructura
```
com.medix.app/
├── MainActivity.kt
├── ui/
│   ├── screens/VoiceScreen.kt
│   ├── components/MicButton.kt
│   └── theme/
├── viewmodel/VoiceViewModel.kt
├── data/
│   ├── api/ApiService.kt
│   ├── api/WebSocketClient.kt
│   ├── repository/VoiceRepository.kt
│   └── models/
├── audio/
│   ├── AudioRecorder.kt
│   └── AudioPlayer.kt
└── utils/Constants.kt
```

## Configuración
La URL base está en:
- `app/src/main/java/com/medix/app/utils/Constants.kt`

Valor por defecto:
- `http://10.43.101.9:8000`

## Backend esperado
- `POST /asr/transcribe`
- `POST /conversation`
- `WS /ws/conversation/{session_id}`

## Flujo
1. Mantén presionado el botón de micrófono para grabar.
2. Al soltar, se envía el audio a `/asr/transcribe`.
3. El texto transcrito se envía a `/conversation`.
4. Se muestra respuesta en pantalla y se reproduce con TextToSpeech.
5. Además, puedes probar envío por WebSocket con el botón de test.

## Permisos
- `RECORD_AUDIO`
- `INTERNET`

## Ejecutar
```bash
./gradlew :app:assembleDebug
```

APK generado en:
- `app/build/outputs/apk/debug/`
