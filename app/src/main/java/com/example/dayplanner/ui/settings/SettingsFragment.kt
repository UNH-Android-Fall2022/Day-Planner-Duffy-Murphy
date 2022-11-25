package com.example.dayplanner.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.*
import com.example.dayplanner.databinding.FragmentSettingsBinding
import com.example.dayplanner.background.UserData.Companion.logout
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            val action = SettingsFragmentDirections.actionNavigationSettingsToNavigationLogin()
            findNavController().navigate(action)
        }  else {
            val toolbar: Toolbar = binding.settingsToolbar
            toolbar.title = user.displayName

            binding.signoutButton.setOnClickListener {
                AuthUI.getInstance()
                    .signOut(root.context)
                    .addOnCompleteListener {
                        Log.d(TAG, "Successfully signed out")
                    }
                logout()

                val action = SettingsFragmentDirections.actionNavigationSettingsToNavigationLogin()
                findNavController().navigate(action)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}