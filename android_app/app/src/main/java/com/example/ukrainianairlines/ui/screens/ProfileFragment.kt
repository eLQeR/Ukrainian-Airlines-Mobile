package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.ui.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var headerText: TextView
    private lateinit var loggedInGroup: LinearLayout
    private lateinit var loggedOutGroup: LinearLayout
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        headerText = root.findViewById(R.id.header_text)
        loggedInGroup = root.findViewById(R.id.logged_in_group)
        loggedOutGroup = root.findViewById(R.id.logged_out_group)
        loginButton = root.findViewById(R.id.login_button)
        registerButton = root.findViewById(R.id.register_button)
        logoutButton = root.findViewById(R.id.logout_button)

        setupUI()
        observeViewModel()
        updateUI()

        return root
    }

    private fun setupUI() {
        loginButton.setOnClickListener {
            navigateToLogin()
        }

        registerButton.setOnClickListener {
            navigateToRegister()
        }

        logoutButton.setOnClickListener {
            authViewModel.logout()
        }
    }

    private fun observeViewModel() {
        authViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            updateUI()
        }

        authViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                authViewModel.clearError()
            }
        }
    }

    private fun updateUI() {
        val isLoggedIn = authViewModel.isLoggedIn.value ?: false

        if (isLoggedIn) {
            loggedInGroup.visibility = View.VISIBLE
            loggedOutGroup.visibility = View.GONE
            headerText.text = getString(R.string.my_profile)
        } else {
            loggedInGroup.visibility = View.GONE
            loggedOutGroup.visibility = View.VISIBLE
            headerText.text = getString(R.string.login_required)
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.loginFragment)
    }

    private fun navigateToRegister() {
        findNavController().navigate(R.id.registerFragment)
    }
}