package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.jvm.JvmStatic

public data class BookingFragmentArgs(
  public val flightId: Int,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("flightId", this.flightId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("flightId", this.flightId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): BookingFragmentArgs {
      bundle.setClassLoader(BookingFragmentArgs::class.java.classLoader)
      val __flightId : Int
      if (bundle.containsKey("flightId")) {
        __flightId = bundle.getInt("flightId")
      } else {
        throw IllegalArgumentException("Required argument \"flightId\" is missing and does not have an android:defaultValue")
      }
      return BookingFragmentArgs(__flightId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): BookingFragmentArgs {
      val __flightId : Int?
      if (savedStateHandle.contains("flightId")) {
        __flightId = savedStateHandle["flightId"]
        if (__flightId == null) {
          throw IllegalArgumentException("Argument \"flightId\" of type integer does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"flightId\" is missing and does not have an android:defaultValue")
      }
      return BookingFragmentArgs(__flightId)
    }
  }
}
