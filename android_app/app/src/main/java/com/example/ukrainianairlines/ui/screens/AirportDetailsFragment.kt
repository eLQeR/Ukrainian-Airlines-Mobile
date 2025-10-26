package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.ui.adapters.RoutesAdapter
import com.example.ukrainianairlines.ui.viewmodels.AirportsViewModel
import com.google.android.material.snackbar.Snackbar

class AirportDetailsFragment : Fragment() {

    private val viewModel: AirportsViewModel by viewModels()
    private lateinit var nameText: TextView
    private lateinit var cityText: TextView
    private lateinit var routesRecycler: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var routesAdapter: RoutesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_airport_details, container, false)
        nameText = root.findViewById<TextView>(R.id.airport_name)
        cityText = root.findViewById<TextView>(R.id.airport_city)
        routesRecycler = root.findViewById<RecyclerView>(R.id.routes_recycler_view)
        progressBar = root.findViewById<ProgressBar>(R.id.progress_bar)

        routesAdapter = RoutesAdapter()
        routesRecycler.layoutManager = LinearLayoutManager(requireContext())
        routesRecycler.adapter = routesAdapter

        val airportId = arguments?.getInt("airportId") ?: 0
        observeViewModel()
        viewModel.loadAirport(airportId)

        return root
    }

    private fun observeViewModel() {
        viewModel.selectedAirport.observe(viewLifecycleOwner) { airport ->
            airport?.let {
                nameText.text = it.name
                cityText.text = it.closest_big_city
            }
        }

        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            routesAdapter.submitList(routes)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
}
