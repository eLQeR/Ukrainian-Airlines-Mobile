package com.example.ukrainianairlines.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ukrainianairlines.UkrainianAirlinesApplication
import com.example.ukrainianairlines.data.model.Flight
import com.example.ukrainianairlines.data.repository.UkrainianAirlinesRepository
import kotlinx.coroutines.launch

class FlightDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = (application as UkrainianAirlinesApplication).tokenManager
    private val repository = UkrainianAirlinesRepository { tokenManager.getAccessToken() }

    private val _flight = MutableLiveData<Flight?>()
    val flight: LiveData<Flight?> = _flight

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFlight(flightId: Int) {
        if (tokenManager.isTokenExpired()) {
            // token refresh handled in repository calls in other viewmodels; keep simple here
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getFlight(flightId).collect { result ->
                result.onSuccess { f ->
                    _flight.value = f
                }.onFailure { ex ->
                    _error.value = ex.message
                }
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
