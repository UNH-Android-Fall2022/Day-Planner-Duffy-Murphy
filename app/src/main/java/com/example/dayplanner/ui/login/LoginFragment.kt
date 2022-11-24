package com.example.dayplanner.ui.login

import android.app.ActionBar
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dayplanner.*
import com.example.dayplanner.data.eventList
import com.example.dayplanner.databinding.FragmentLoginBinding
import com.example.dayplanner.ui.list.ListFragmentDirections
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val user = FirebaseAuth.getInstance().currentUser

        binding.loginBttn.setOnClickListener {
            // Create and launch sign-in intent
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }

        return root
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ){ res ->
        this.onSignInResult(res)
    }

    val providers = arrayListOf(
        //AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build())

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == Activity.RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Log.d(TAG, "Uploading current events to database")
                for (event in eventList) {
                    db.collection("Users/${user.uid}/events").add(event)
                        .addOnSuccessListener { Log.d(TAG, "Event successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document: ", e) }
                }
                Log.d(TAG, "Getting events from database")
                getEvents(user.uid)
                DB_PULL_COMPLETED = true
            }
//            Log.d(TAG, "Sign in successful. Checking if user already exists")
//            db.collection("Users").document("${user?.uid}").get()
//                .addOnSuccessListener { document ->
//                    Log.d(TAG, "User exists. No modifications necessary")
//                }
//                .addOnFailureListener { exception ->
//                    Log.d(TAG, "User probably does not exist, adding user")
//                    db.collection("Users").document("${user?.uid}").set(User())
//                }
            val action = LoginFragmentDirections.actionNavigationLoginToNavigationSettings()
            findNavController().navigate(action)
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            if (response != null) {
                Log.e(TAG, "Error during signin: ", response.error)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}