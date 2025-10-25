package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import androidx.navigation.NavDirections
import com.example.ukrainianairlines.R
import kotlin.Int

public class FlightResultsFragmentDirections private constructor() {
  private data class ActionFlightResultsFragmentToBookingFragment(
    public val flightId: Int,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_flightResultsFragment_to_bookingFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("flightId", this.flightId)
        return result
      }
  }

  public companion object {
    public fun actionFlightResultsFragmentToBookingFragment(flightId: Int): NavDirections =
        ActionFlightResultsFragmentToBookingFragment(flightId)
  }
}
