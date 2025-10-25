package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmStatic

public data class FlightResultsFragmentArgs(
  public val sourceAirport: Int,
  public val destinationAirport: Int,
  public val departureDate: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("sourceAirport", this.sourceAirport)
    result.putInt("destinationAirport", this.destinationAirport)
    result.putString("departureDate", this.departureDate)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("sourceAirport", this.sourceAirport)
    result.set("destinationAirport", this.destinationAirport)
    result.set("departureDate", this.departureDate)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): FlightResultsFragmentArgs {
      bundle.setClassLoader(FlightResultsFragmentArgs::class.java.classLoader)
      val __sourceAirport : Int
      if (bundle.containsKey("sourceAirport")) {
        __sourceAirport = bundle.getInt("sourceAirport")
      } else {
        throw IllegalArgumentException("Required argument \"sourceAirport\" is missing and does not have an android:defaultValue")
      }
      val __destinationAirport : Int
      if (bundle.containsKey("destinationAirport")) {
        __destinationAirport = bundle.getInt("destinationAirport")
      } else {
        throw IllegalArgumentException("Required argument \"destinationAirport\" is missing and does not have an android:defaultValue")
      }
      val __departureDate : String?
      if (bundle.containsKey("departureDate")) {
        __departureDate = bundle.getString("departureDate")
        if (__departureDate == null) {
          throw IllegalArgumentException("Argument \"departureDate\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"departureDate\" is missing and does not have an android:defaultValue")
      }
      return FlightResultsFragmentArgs(__sourceAirport, __destinationAirport, __departureDate)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): FlightResultsFragmentArgs {
      val __sourceAirport : Int?
      if (savedStateHandle.contains("sourceAirport")) {
        __sourceAirport = savedStateHandle["sourceAirport"]
        if (__sourceAirport == null) {
          throw IllegalArgumentException("Argument \"sourceAirport\" of type integer does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"sourceAirport\" is missing and does not have an android:defaultValue")
      }
      val __destinationAirport : Int?
      if (savedStateHandle.contains("destinationAirport")) {
        __destinationAirport = savedStateHandle["destinationAirport"]
        if (__destinationAirport == null) {
          throw IllegalArgumentException("Argument \"destinationAirport\" of type integer does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"destinationAirport\" is missing and does not have an android:defaultValue")
      }
      val __departureDate : String?
      if (savedStateHandle.contains("departureDate")) {
        __departureDate = savedStateHandle["departureDate"]
        if (__departureDate == null) {
          throw IllegalArgumentException("Argument \"departureDate\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"departureDate\" is missing and does not have an android:defaultValue")
      }
      return FlightResultsFragmentArgs(__sourceAirport, __destinationAirport, __departureDate)
    }
  }
}
