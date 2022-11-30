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
import kotlinx.coroutines.delay
import java.lang.Thread.sleep
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
        }


        val startNotifSwitch: SwitchMaterial = binding.startNotifSwitch
        val endNotifSwitch: SwitchMaterial = binding.endNotifSwitch
        val locationServSwitch: SwitchMaterial = binding.locationServSwitch

        if (userData?.startNotifications != null && userData!!.startNotifications) {
            startNotifSwitch.isChecked = true
            startNotifSwitch.text = getString(R.string.yes)
        }
        if (userData?.endNotifications != null && userData!!.endNotifications) {
            endNotifSwitch.isChecked = true
            endNotifSwitch.text = getString(R.string.yes)
        }
        if (userData?.locationServices != null && userData!!.locationServices) {
            locationServSwitch.isChecked = true
            locationServSwitch.text = getString(R.string.yes)
        }

        startNotifSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            // Responds to switch being checked/unchecked
            if (isChecked) {
                startNotifSwitch.text = getString(R.string.yes)
                //Log.d(TAG, "User data: ${userData}")
                userData?.startNotifications = true
            } else {
                startNotifSwitch.text = getString(R.string.no)
                userData?.startNotifications = false
            }
            userData?.let { Firebase.firestore.collection("Users").document(user!!.uid).set(it, SetOptions.merge()) }
            resetAlarms()
        })

        endNotifSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            // Responds to switch being checked/unchecked
            if (isChecked) {
                endNotifSwitch.text = getString(R.string.yes)
                userData?.endNotifications = true
            } else {
                endNotifSwitch.text = getString(R.string.no)
                userData?.endNotifications = false
            }
            userData?.let { Firebase.firestore.collection("Users").document(user!!.uid).set(it, SetOptions.merge()) }
            resetAlarms()
        })

        locationServSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            // Responds to switch being checked/unchecked
            if (isChecked) {
                locationServSwitch.text = getString(R.string.yes)
                userData?.locationServices = true
            } else {
                locationServSwitch.text = getString(R.string.no)
                userData?.locationServices = false
            }
            userData?.let { Firebase.firestore.collection("Users").document(user!!.uid).set(it, SetOptions.merge()) }
            // No need to call any functions, as onResume() will update the necessary RecyclerViews
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}