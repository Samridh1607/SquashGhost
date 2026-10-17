package com.squashghosting.trainer.ui

import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.squashghosting.trainer.R
import com.squashghosting.trainer.core.Pattern
import com.squashghosting.trainer.core.VoiceGender
import com.squashghosting.trainer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TrainingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()

        setupDropdowns()
        setupSliders()

        binding.startButton.setOnClickListener { viewModel.startTraining() }
        binding.endButton.setOnClickListener { viewModel.endTraining() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::render)
            }
        }
    }

    private fun applyInsets() {
        val base = binding.contentRoot.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentRoot) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = base + bars.left,
                top = base + bars.top,
                right = base + bars.right,
                bottom = base + bars.bottom,
            )
            insets
        }
    }

    private fun setupDropdowns() {
        val state = viewModel.state.value

        binding.patternDropdown.setAdapter(
            ArrayAdapter(this, R.layout.item_dropdown, Pattern.entries.map { it.displayName })
        )
        binding.patternDropdown.setText(state.pattern.displayName, false)
        binding.patternDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setPattern(Pattern.entries[position])
        }

        binding.voiceDropdown.setAdapter(
            ArrayAdapter(this, R.layout.item_dropdown, VoiceGender.entries.map { it.displayName })
        )
        binding.voiceDropdown.setText(state.voice.displayName, false)
        // Fires only on a user selection (never on restore), like the Kivy spinner
        binding.voiceDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setVoice(VoiceGender.entries[position])
        }
    }

    private fun setupSliders() {
        val state = viewModel.state.value

        binding.setsSlider.value = state.totalSets.toFloat()
        binding.setsSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setTotalSets(value.toInt())
        }

        binding.setsDelaySlider.value = state.delayBetweenSets.toFloat()
        binding.setsDelaySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setDelayBetweenSets(value.toInt())
        }

        binding.insDelaySlider.value = state.delayBetweenInstructions
        binding.insDelaySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setDelayBetweenInstructions(value)
        }
    }

    private fun render(state: TrainingUiState) = with(binding) {
        setsLabel.text = getString(R.string.label_sets, state.totalSets)
        setsDelayLabel.text = getString(R.string.label_delay_sets, state.delayBetweenSets)
        insDelayLabel.text = getString(
            R.string.label_delay_ins,
            String.format(Locale.US, "%.1f", state.delayBetweenInstructions)
        )

        statusLabel.text = state.status
        progressLabel.text = state.progress

        val idle = !state.isTraining
        startButton.isEnabled = idle
        endButton.isEnabled = state.isTraining
        setsSlider.isEnabled = idle
        setsDelaySlider.isEnabled = idle
        patternLayout.isEnabled = idle
        // Voice and instruction delay stay adjustable during training (as before)

        // Keep the screen on while training
        if (state.isTraining) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
