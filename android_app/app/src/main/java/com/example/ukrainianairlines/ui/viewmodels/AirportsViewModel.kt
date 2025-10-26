package com.example.ukrainianairlines.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ukrainianairlines.UkrainianAirlinesApplication
import com.example.ukrainianairlines.data.model.Airport
import com.example.ukrainianairlines.data.model.Route
import com.example.ukrainianairlines.data.repository.UkrainianAirlinesRepository
import kotlinx.coroutines.launch

class AirportsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = (application as UkrainianAirlinesApplication).tokenManager
    private val repository = UkrainianAirlinesRepository { tokenManager.getAccessToken() }

    private val _airports = MutableLiveData<List<Airport>>(emptyList())
    val airports: LiveData<List<Airport>> = _airports

    private val _selectedAirport = MutableLiveData<Airport?>(null)
    val selectedAirport: LiveData<Airport?> = _selectedAirport

    private val _routes = MutableLiveData<List<Route>>(emptyList())
    val routes: LiveData<List<Route>> = _routes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val TAG = "AirportsViewModel"

    fun loadAirports(name: String? = null, city: String? = null) {
        if (tokenManager.isTokenExpired()) {
            // attempt refresh but don't block UI
            refreshTokenIfNeeded()
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAirports(name, city).collect { result ->
                result.onSuccess { list ->
                    Log.d(TAG, "loadAirports: success, count=${list.size}")
                    _airports.value = list
                }
                result.onFailure { ex ->
                    Log.e(TAG, "loadAirports: failure", ex)
                    _error.value = ex.message
                }
                _isLoading.value = false
            }
        }
    }

    fun loadAirport(id: Int) {
        if (tokenManager.isTokenExpired()) {
            refreshTokenIfNeeded()
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAirport(id).collect { result ->
                result.onSuccess { airport ->
                    Log.d(TAG, "loadAirport: success id=${airport.id} name=${airport.name}")
                    _selectedAirport.value = airport
                    // load routes where this airport is source or destination
                    repository.getRoutes(source = id).collect { r1 ->
                        r1.onSuccess { list1 ->
                            repository.getRoutes(destination = id).collect { r2 ->
                                r2.onSuccess { list2 ->
                                    _routes.value = (list1 + list2).distinctBy { it.id }
                                }.onFailure { ex2 ->
                                    Log.e(TAG, "loadAirport: getRoutes destination failed", ex2)
                                    _error.value = ex2.message
                                }
                            }
                        }.onFailure { ex1 ->
                            Log.e(TAG, "loadAirport: getRoutes source failed", ex1)
                            _error.value = ex1.message
                        }
                    }
                }.onFailure { ex ->
                    Log.e(TAG, "loadAirport: failure", ex)
                    _error.value = ex.message
                }
                _isLoading.value = false
            }
        }
    }

    private fun refreshTokenIfNeeded() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken != null) {
            viewModelScope.launch {
                repository.refreshToken(refreshToken).collect { result ->
                    result.onSuccess { token ->
                        tokenManager.updateAccessToken(token)
                        Log.d(TAG, "refreshTokenIfNeeded: refreshed token")
                    }
                    result.onFailure {
                        _error.value = "Session expired. Please login again."
                        Log.e(TAG, "refreshTokenIfNeeded: failed", it)
                    }
                }
            }
        }
    }

    fun clearError() { _error.value = null }
}
