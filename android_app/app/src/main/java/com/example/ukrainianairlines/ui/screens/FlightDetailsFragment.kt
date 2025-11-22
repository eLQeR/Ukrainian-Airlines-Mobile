package com.example.ukrainianairlines.ui.screens
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.data.model.Flight
import com.example.ukrainianairlines.ui.viewmodels.FlightDetailsViewModel
import com.google.android.material.snackbar.Snackbar

class FlightDetailsFragment : Fragment() {

    private val viewModel: FlightDetailsViewModel by viewModels()
    private lateinit var routeText: TextView
    private lateinit var departureText: TextView
    private lateinit var arrivalText: TextView
    private lateinit var durationText: TextView
    private lateinit var airplaneText: TextView
    private lateinit var seatsText: TextView
    private lateinit var priceText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bookButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_flight_details, container, false)

        routeText = root.findViewById(R.id.details_route_text)
        departureText = root.findViewById(R.id.details_departure_time)
        arrivalText = root.findViewById(R.id.details_arrival_time)
        durationText = root.findViewById(R.id.details_duration)
        airplaneText = root.findViewById(R.id.details_airplane)
        seatsText = root.findViewById(R.id.details_seats)
        priceText = root.findViewById(R.id.details_price)
        progressBar = root.findViewById(R.id.progress_bar)
        bookButton = root.findViewById(R.id.details_book_button)

        val flightId = arguments?.getInt("flightId") ?: 0

        bookButton.setOnClickListener {
            val bundle = Bundle().apply { putInt("flightId", flightId) }
            findNavController().navigate(R.id.action_flightDetailsFragment_to_bookingFragment, bundle)
        }

        observeViewModel()
        if (flightId != 0) viewModel.loadFlight(flightId)

        return root
    }

    private fun observeViewModel() {
        viewModel.flight.observe(viewLifecycleOwner) { flight ->
            flight?.let { bindFlight(it) }
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

    private fun bindFlight(flight: Flight) {
        routeText.text = "${flight.route.source.closest_big_city} → ${flight.route.destination.closest_big_city}"
        departureText.text = flight.departure_time
        arrivalText.text = flight.arrival_time
        durationText.text = flight.time_of_flight ?: "N/A"
        airplaneText.text = flight.airplane.name
        seatsText.text = "${flight.tickets_available} seats available"
        priceText.text = "From $299"
    }
}
