package com.closingassistant.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.closingassistant.R
import com.closingassistant.data.model.ClientProfile
import com.closingassistant.databinding.ActivityClientProfileBinding
import com.closingassistant.ui.viewmodels.ClientProfileViewModel
import com.closingassistant.ui.viewmodels.ProfileUiState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ClientProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientProfileBinding
    private val viewModel: ClientProfileViewModel by viewModels()

    companion object {
        const val EXTRA_CLIENT_PROFILE = "extra_client_profile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, R.id.menu_logout, 0, "Logout")
            .setIcon(android.R.drawable.ic_lock_power_off)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            clearErrors()
            viewModel.validateAndProceed(
                ageStr = binding.etAge.text?.toString().orEmpty().trim(),
                incomeStr = binding.etMonthlyIncome.text?.toString().orEmpty().trim(),
                dependentsStr = binding.etDependents.text?.toString().orEmpty().trim(),
                financialGoals = binding.etFinancialGoals.text?.toString().orEmpty().trim(),
                concerns = binding.etConcerns.text?.toString().orEmpty().trim()
            )
        }
    }

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is ProfileUiState.Idle -> setLoading(false)
                is ProfileUiState.Loading -> setLoading(true)
                is ProfileUiState.Success -> {
                    setLoading(false)
                    navigateToSalesFlow(state.profile)
                    viewModel.resetState()
                }
                is ProfileUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                    viewModel.resetState()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnNext.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun clearErrors() {
        binding.tvError.visibility = View.GONE
        binding.tilAge.error = null
        binding.tilMonthlyIncome.error = null
        binding.tilDependents.error = null
        binding.tilFinancialGoals.error = null
        binding.tilConcerns.error = null
    }

    private fun navigateToSalesFlow(profile: ClientProfile) {
        val intent = Intent(this, SalesFlowActivity::class.java).apply {
            putExtra(SalesFlowActivity.EXTRA_AGE, profile.age)
            putExtra(SalesFlowActivity.EXTRA_MONTHLY_INCOME, profile.monthlyIncome)
            putExtra(SalesFlowActivity.EXTRA_DEPENDENTS, profile.numberOfDependents)
            putExtra(SalesFlowActivity.EXTRA_FINANCIAL_GOALS, profile.financialGoals)
            putExtra(SalesFlowActivity.EXTRA_CONCERNS, profile.concerns)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}
