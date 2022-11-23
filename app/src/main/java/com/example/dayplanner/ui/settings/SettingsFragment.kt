package com.example.dayplanner.ui.settings

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.MainActivity
import com.example.dayplanner.TAG
import com.example.dayplanner.data.Event
import com.example.dayplanner.data.User
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.FragmentSettingsBinding
import com.example.dayplanner.getEvents
import com.example.dayplanner.ui.list.ListFragmentDirections
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

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
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            val action = SettingsFragmentDirections.actionNavigationSettingsToLoginFragment()
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
                eventList.clear()
                val action = SettingsFragmentDirections.actionNavigationSettingsToLoginFragment()
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