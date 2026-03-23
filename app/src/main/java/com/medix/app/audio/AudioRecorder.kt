package com.medix.app.audio

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): File {
        val file = File.createTempFile("medix_record_", ".m4a", context.cacheDir)
        outputFile = file

        recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        return file
    }

    fun stop(): File? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            outputFile
        } catch (_: Exception) {
            null
        } finally {
            recorder = null
        }
    }
}
