package com.closingassistant.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.closingassistant.R
import com.closingassistant.data.model.ClientProfile
import com.closingassistant.data.model.EmotionalTrigger
import com.closingassistant.data.model.Recommendation
import com.closingassistant.data.model.TriggerType
import com.closingassistant.databinding.ActivityRecommendationBinding
import com.closingassistant.ui.viewmodels.RecommendationUiState
import com.closingassistant.ui.viewmodels.RecommendationViewModel
import com.google.android.material.snackbar.Snackbar

class RecommendationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendationBinding
    private val viewModel: RecommendationViewModel by viewModels()

    private lateinit var clientProfile: ClientProfile

    companion object {
        const val EXTRA_AGE = "extra_age"
        const val EXTRA_MONTHLY_INCOME = "extra_monthly_income"
        const val EXTRA_DEPENDENTS = "extra_dependents"
        const val EXTRA_FINANCIAL_GOALS = "extra_financial_goals"
        const val EXTRA_CONCERNS = "extra_concerns"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractClientProfile()
        setupToolbar()
        setupObservers()
        setupClickListeners()

        viewModel.generateRecommendation(clientProfile)
    }

    private fun extractClientProfile() {
        clientProfile = ClientProfile(
            age = intent.getIntExtra(EXTRA_AGE, 25),
            monthlyIncome = intent.getDoubleExtra(EXTRA_MONTHLY_INCOME, 0.0),
            numberOfDependents = intent.getIntExtra(EXTRA_DEPENDENTS, 0),
            financialGoals = intent.getStringExtra(EXTRA_FINANCIAL_GOALS).orEmpty(),
            concerns = intent.getStringExtra(EXTRA_CONCERNS).orEmpty()
        )
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupClickListeners() {
        binding.btnSaveClient.setOnClickListener {
            viewModel.saveClientProfile(clientProfile)
        }

        binding.btnNewClient.setOnClickListener {
            val intent = Intent(this, ClientProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is RecommendationUiState.Loading -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.scrollContent.visibility = View.GONE
                }
                is RecommendationUiState.Success -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.scrollContent.visibility = View.VISIBLE
                    renderRecommendation(state.recommendation)
                }
                is RecommendationUiState.Saved -> {
                    Snackbar.make(
                        binding.root,
                        "✅ Client saved successfully!",
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(ContextCompat.getColor(this, R.color.success))
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .show()
                    binding.btnSaveClient.isEnabled = false
                    binding.btnSaveClient.text = "Saved ✓"
                }
                is RecommendationUiState.Error -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.scrollContent.visibility = View.VISIBLE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(this, R.color.error))
                        .setTextColor(ContextCompat.getColor(this, R.color.white))
                        .show()
                }
            }
        }
    }

    private fun renderRecommendation(rec: Recommendation) {
        // Plan details
        binding.tvPlanName.text = rec.planName
        binding.tvPlanDescription.text = rec.planDescription
        binding.tvEstPremium.text = rec.estimatedPremium
        binding.tvCoverage.text = rec.coverage
        binding.tvClosingScript.text = rec.closingScript

        // Talking Points
        buildTalkingPoints(rec.talkingPoints)

        // Emotional Triggers
        buildEmotionalTriggers(rec.emotionalTriggers)

        // Animate in
        binding.scrollContent.alpha = 0f
        binding.scrollContent.animate().alpha(1f).setDuration(400).start()
    }

    private fun buildTalkingPoints(points: List<String>) {
        binding.talkingPointsContainer.removeAllViews()
        val dp8 = (8 * resources.displayMetrics.density).toInt()
        val dp4 = (4 * resources.displayMetrics.density).toInt()

        points.forEachIndexed { index, point ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.bottomMargin = dp8
                }
            }

            // Number badge
            val badge = TextView(this).apply {
                text = "${index + 1}"
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@RecommendationActivity, R.color.white))
                background = ContextCompat.getDrawable(this@RecommendationActivity, R.drawable.bg_step_indicator)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    (28 * resources.displayMetrics.density).toInt(),
                    (28 * resources.displayMetrics.density).toInt()
                ).also { it.marginEnd = dp8; it.topMargin = dp4 }
                minWidth = (28 * resources.displayMetrics.density).toInt()
            }

            // Point text
            val text = TextView(this).apply {
                this.text = point
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@RecommendationActivity, R.color.text_primary))
                setLineSpacing(0f, 1.5f)
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            row.addView(badge)
            row.addView(text)
            binding.talkingPointsContainer.addView(row)

            // Divider (except last)
            if (index < points.size - 1) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).also { it.topMargin = dp4; it.bottomMargin = dp4 }
                    setBackgroundColor(ContextCompat.getColor(this@RecommendationActivity, R.color.divider))
                }
                binding.talkingPointsContainer.addView(divider)
            }
        }
    }

    private fun buildEmotionalTriggers(triggers: List<EmotionalTrigger>) {
        binding.triggersContainer.removeAllViews()
        val dp12 = (12 * resources.displayMetrics.density).toInt()
        val dp8 = (8 * resources.displayMetrics.density).toInt()

        triggers.forEach { trigger ->
            val bgDrawable = when (trigger.backgroundType) {
                TriggerType.FAMILY -> R.drawable.bg_trigger_family
                TriggerType.SECURITY -> R.drawable.bg_trigger_security
                TriggerType.FUTURE -> R.drawable.bg_trigger_future
                TriggerType.LEGACY -> R.drawable.bg_chip_rounded
            }
            val textColor = when (trigger.backgroundType) {
                TriggerType.FAMILY -> R.color.chip_family_text
                TriggerType.SECURITY -> R.color.chip_security_text
                TriggerType.FUTURE -> R.color.chip_future_text
                TriggerType.LEGACY -> R.color.primary
            }

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(this@RecommendationActivity, bgDrawable)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dp8 }
                setPadding(dp12, dp12, dp12, dp12)
            }

            val labelRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dp8 }
            }

            val emoji = TextView(this).apply {
                text = trigger.emoji
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = dp8 }
            }

            val label = TextView(this).apply {
                text = trigger.label
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@RecommendationActivity, textColor))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            labelRow.addView(emoji)
            labelRow.addView(label)

            val desc = TextView(this).apply {
                text = trigger.description
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@RecommendationActivity, R.color.text_primary))
                setLineSpacing(0f, 1.5f)
            }

            card.addView(labelRow)
            card.addView(desc)
            binding.triggersContainer.addView(card)
        }
    }
}
