package com.example.dayplanner.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.*
import com.example.dayplanner.databinding.FragmentSettingsBinding
import com.example.dayplanner.background.UserData.Companion.logout
import com.example.dayplanner.background.UserData.Companion.resetAlarms
import com.example.dayplanner.data.User
import com.firebase.ui.auth.AuthUI
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
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

            val startNotifSwitch: SwitchMaterial = binding.startNotifSwitch
            val endNotifSwitch: SwitchMaterial = binding.endNotifSwitch

            if (userData!!.startNotifications) {
                startNotifSwitch.isChecked = true
                startNotifSwitch.text = getString(R.string.yes)
            }
            if (userData!!.endNotifications) {
                endNotifSwitch.isChecked = true
                endNotifSwitch.text = getString(R.string.yes)
            }

            startNotifSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                // Responds to switch being checked/unchecked
                if (isChecked) {
                    startNotifSwitch.text = getString(R.string.yes)
                    //Log.d(TAG, "User data: ${userData}")
                    userData = User(userData!!.defaultScreen,
                        userData!!.locationServices,
                        userData!!.etaEnabled,
                        true,
                        userData!!.endNotifications)
                } else {
                    startNotifSwitch.text = getString(R.string.no)
                    userData = User(userData!!.defaultScreen,
                        userData!!.locationServices,
                        userData!!.etaEnabled,
                        false,
                        userData!!.endNotifications)
                }
                Firebase.firestore.collection("Users").document(user.uid).set(userData!!, SetOptions.merge())
                resetAlarms()
            })

            endNotifSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                // Responds to switch being checked/unchecked
                if (isChecked) {
                    endNotifSwitch.text = getString(R.string.yes)
                    userData = User(userData!!.defaultScreen,
                        userData!!.locationServices,
                        userData!!.etaEnabled,
                        userData!!.startNotifications,
                        true)
                } else {
                    endNotifSwitch.text = getString(R.string.no)
                    userData = User(userData!!.defaultScreen,
                        userData!!.locationServices,
                        userData!!.etaEnabled,
                        userData!!.startNotifications,
                        false)
                }
                Firebase.firestore.collection("Users").document(user.uid).set(userData!!, SetOptions.merge())
                resetAlarms()
            })
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}