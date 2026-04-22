package com.closingassistant.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.closingassistant.data.model.ClientProfile
import com.closingassistant.data.model.SalesStep
import com.closingassistant.databinding.ActivitySalesFlowBinding
import com.closingassistant.ui.viewmodels.SalesFlowViewModel

class SalesFlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySalesFlowBinding
    private val viewModel: SalesFlowViewModel by viewModels()

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
        binding = ActivitySalesFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractClientProfile()
        setupToolbar()
        setupObservers()
        setupClickListeners()

        viewModel.loadScript(clientProfile)
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
        binding.btnNextStep.setOnClickListener {
            val isLast = viewModel.isLastStep.value == true
            if (isLast) {
                navigateToRecommendation()
            } else {
                viewModel.nextStep()
            }
        }

        binding.btnPrevStep.setOnClickListener {
            viewModel.prevStep()
        }
    }

    private fun setupObservers() {
        viewModel.currentStep.observe(this) { step ->
            renderStep(step)
        }

        viewModel.currentStepIndex.observe(this) { index ->
            val total = viewModel.getTotalSteps()
            binding.tvStepLabel.text = "Step ${index + 1} of $total"
            updateStepDots(index, total)
        }

        viewModel.progress.observe(this) { progress ->
            binding.stepProgress.progress = progress
        }

        viewModel.isLastStep.observe(this) { isLast ->
            binding.btnNextStep.text = if (isLast) "View Recommendation" else "Next Step"
        }

        viewModel.isFirstStep.observe(this) { isFirst ->
            binding.btnPrevStep.visibility = if (isFirst) View.GONE else View.VISIBLE
        }

        viewModel.steps.observe(this) { steps ->
            buildStepDots(steps.size)
        }
    }

    private fun renderStep(step: SalesStep) {
        binding.tvStepBadge.text = step.stepNumber.toString()
        binding.tvCurrentStepTitle.text = step.title
        binding.tvStepTitle.text = step.title
        binding.tvScript.text = step.script
        binding.tvTips.text = step.tips
        binding.tvTips.visibility = if (step.tips.isNotBlank()) View.VISIBLE else View.GONE
        binding.cardTips.visibility = if (step.tips.isNotBlank()) View.VISIBLE else View.GONE

        // Animate in the card
        binding.tvScript.alpha = 0f
        binding.tvScript.animate().alpha(1f).setDuration(300).start()
    }

    private fun buildStepDots(count: Int) {
        binding.stepDotsContainer.removeAllViews()
        val context = this
        val sizePx = (10 * resources.displayMetrics.density).toInt()
        val marginPx = (6 * resources.displayMetrics.density).toInt()

        for (i in 0 until count) {
            val dot = View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(sizePx, sizePx).also {
                    it.marginStart = marginPx
                    it.marginEnd = marginPx
                }
                background = androidx.core.content.ContextCompat.getDrawable(
                    context, com.closingassistant.R.drawable.bg_step_indicator
                )
                alpha = if (i == 0) 1.0f else 0.3f
            }
            binding.stepDotsContainer.addView(dot)
        }
    }

    private fun updateStepDots(activeIndex: Int, total: Int) {
        val container = binding.stepDotsContainer
        for (i in 0 until container.childCount) {
            container.getChildAt(i)?.alpha = if (i == activeIndex) 1.0f else 0.3f
        }
    }

    private fun navigateToRecommendation() {
        val intent = Intent(this, RecommendationActivity::class.java).apply {
            putExtra(RecommendationActivity.EXTRA_AGE, clientProfile.age)
            putExtra(RecommendationActivity.EXTRA_MONTHLY_INCOME, clientProfile.monthlyIncome)
            putExtra(RecommendationActivity.EXTRA_DEPENDENTS, clientProfile.numberOfDependents)
            putExtra(RecommendationActivity.EXTRA_FINANCIAL_GOALS, clientProfile.financialGoals)
            putExtra(RecommendationActivity.EXTRA_CONCERNS, clientProfile.concerns)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}
