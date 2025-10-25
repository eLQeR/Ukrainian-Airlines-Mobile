package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import androidx.navigation.NavDirections
import com.example.ukrainianairlines.R
import kotlin.Int
import kotlin.String

public class SearchFragmentDirections private constructor() {
  private data class ActionNavSearchToFlightResultsFragment(
    public val sourceAirport: Int,
    public val destinationAirport: Int,
    public val departureDate: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_nav_search_to_flightResultsFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("sourceAirport", this.sourceAirport)
        result.putInt("destinationAirport", this.destinationAirport)
        result.putString("departureDate", this.departureDate)
        return result
      }
  }

  public companion object {
    public fun actionNavSearchToFlightResultsFragment(
      sourceAirport: Int,
      destinationAirport: Int,
      departureDate: String,
    ): NavDirections = ActionNavSearchToFlightResultsFragment(sourceAirport, destinationAirport,
        departureDate)
  }
}
