package com.anthonyponte.honker

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anthonyponte.honker.databinding.ActivityMainBinding
import com.araujo.jordan.excuseme.ExcuseMe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class MainActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var speech: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private lateinit var player: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                if (isChecked) {
                    ExcuseMe.couldYouGive(this).permissionFor(
                        android.Manifest.permission.RECORD_AUDIO,
                    ) {
                        if (it.granted.contains(android.Manifest.permission.RECORD_AUDIO)) {
                            player = MediaPlayer.create(this, R.raw.entryofthegladiators)
                            startSpeechRecognizer()
                            startIntent()
                            speech.startListening(recognizerIntent)
                        }
                    }
                } else {
                    speech.stopListening()
                    speech.destroy()
                    player.reset()
                }
            } else {
                binding.checkBox.isChecked = false

                MaterialAlertDialogBuilder(this)
                    .setMessage(resources.getString(R.string.no_disponible))
                    .setNegativeButton(resources.getString(R.string.cancelar)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speech.stopListening()
        speech.destroy()
        player.reset()
    }

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        player.pause()
        speech.stopListening()
        speech.destroy()

        startSpeechRecognizer()
        startIntent()
        speech.startListening(recognizerIntent)
    }

    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "$error ERROR_AUDIO"
            SpeechRecognizer.ERROR_CLIENT -> "$error ERROR_CLIENT"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "$error ERROR_INSUFFICIENT_PERMISSIONS"
            SpeechRecognizer.ERROR_NETWORK -> "$error ERROR_NETWORK"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "$error ERROR_NETWORK_TIMEOUT"
            SpeechRecognizer.ERROR_NO_MATCH -> "$error ERROR_NO_MATCH"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "$error ERROR_RECOGNIZER_BUSY"
            SpeechRecognizer.ERROR_SERVER -> "$error ERROR_SERVER"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "$error ERROR_SPEECH_TIMEOUT"
            else -> "ERROR_UNKNOWN"
        }

        Toast.makeText(this, "onError $message", Toast.LENGTH_LONG).show()
    }

    override fun onResults(results: Bundle?) {}

    override fun onPartialResults(partialResults: Bundle?) {
        player.start()
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun startSpeechRecognizer() {
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech.setRecognitionListener(this)
    }

    private fun startIntent() {
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                3000
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }
}