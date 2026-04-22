package com.closingassistant.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.closingassistant.data.model.ClientProfile
import com.closingassistant.data.model.SalesStep
import com.closingassistant.data.repository.ScriptRepository

class SalesFlowViewModel : ViewModel() {

    private val scriptRepository = ScriptRepository()

    private val _steps = MutableLiveData<List<SalesStep>>()
    val steps: LiveData<List<SalesStep>> = _steps

    private val _currentStepIndex = MutableLiveData(0)
    val currentStepIndex: LiveData<Int> = _currentStepIndex

    private val _currentStep = MutableLiveData<SalesStep>()
    val currentStep: LiveData<SalesStep> = _currentStep

    private val _isLastStep = MutableLiveData(false)
    val isLastStep: LiveData<Boolean> = _isLastStep

    private val _isFirstStep = MutableLiveData(true)
    val isFirstStep: LiveData<Boolean> = _isFirstStep

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    fun loadScript(profile: ClientProfile) {
        val generatedSteps = scriptRepository.generateSalesSteps(profile)
        _steps.value = generatedSteps
        setStep(0)
    }

    fun nextStep() {
        val current = _currentStepIndex.value ?: 0
        val total = _steps.value?.size ?: 1
        if (current < total - 1) {
            setStep(current + 1)
        }
    }

    fun prevStep() {
        val current = _currentStepIndex.value ?: 0
        if (current > 0) {
            setStep(current - 1)
        }
    }

    private fun setStep(index: Int) {
        val stepList = _steps.value ?: return
        val total = stepList.size

        _currentStepIndex.value = index
        _currentStep.value = stepList[index]
        _isLastStep.value = index == total - 1
        _isFirstStep.value = index == 0
        _progress.value = ((index + 1).toFloat() / total * 100).toInt()
    }

    fun getTotalSteps(): Int = _steps.value?.size ?: 0

    fun getCurrentStepNumber(): Int = (_currentStepIndex.value ?: 0) + 1
}
