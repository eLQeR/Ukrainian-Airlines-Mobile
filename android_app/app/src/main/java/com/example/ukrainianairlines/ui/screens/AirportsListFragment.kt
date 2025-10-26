package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.data.model.Airport
import com.example.ukrainianairlines.ui.adapters.AirportsAdapter
import com.example.ukrainianairlines.ui.viewmodels.AirportsViewModel
import com.google.android.material.snackbar.Snackbar

class AirportsListFragment : Fragment() {

    private val viewModel: AirportsViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AirportsAdapter
    private lateinit var emptyState: TextView
    private lateinit var progressBar: ProgressBar

    private val TAG = "AirportsListFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val root = inflater.inflate(R.layout.fragment_airports_list, container, false)
            recyclerView = root.findViewById(R.id.airports_recycler_view)
            emptyState = root.findViewById(R.id.empty_state_text)
            progressBar = root.findViewById(R.id.progress_bar)

            adapter = AirportsAdapter { airport ->
                try {
                    val bundle = Bundle().apply { putInt("airportId", airport.id) }
                    findNavController().navigate(R.id.action_nav_airports_to_airportDetails, bundle)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation failed", e)
                    view?.let { Snackbar.make(it, "Unable to open airport details", Snackbar.LENGTH_LONG).show() }
                }
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter

            observeViewModel()
            try {
                viewModel.loadAirports()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load airports", e)
                root.let { Snackbar.make(it, "Failed to load airports", Snackbar.LENGTH_LONG).show() }
            }

            root
        } catch (e: Exception) {
            Log.e(TAG, "onCreateView failed", e)
            // Inflate a simple fallback view so we don't crash
            val fallback = inflater.inflate(R.layout.fragment_airports_list, container, false)
            Snackbar.make(fallback, "Error opening Airports screen", Snackbar.LENGTH_LONG).show()
            fallback
        }
    }

    private fun observeViewModel() {
        viewModel.airports.observe(viewLifecycleOwner) { airports ->
            try {
                Log.d(TAG, "observeViewModel: airports received size=${airports.size}")
                adapter.submitList(airports)
                emptyState.visibility = if (airports.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error updating UI with airports", e)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            try {
                Log.d(TAG, "observeViewModel: isLoading=$isLoading")
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error updating loading state", e)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.w(TAG, "observeViewModel: error=$it")
                view?.let { v -> Snackbar.make(v, it, Snackbar.LENGTH_LONG).show() }
                viewModel.clearError()
            }
        }
    }
}
