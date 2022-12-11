package com.example.dayplanner.background

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.window.SplashScreen
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dayplanner.DB_PULL_COMPLETED
import com.example.dayplanner.databinding.SplashScreenBinding
import com.example.dayplanner.background.SplashScreenFragmentDirections
import kotlinx.coroutines.*
import java.util.*

open class SplashScreenFragment : Fragment() {
    private var _binding: SplashScreenBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val job = Job()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SplashScreenBinding.inflate(inflater, container, false)
        val root: View = binding.root
//        val action = SplashScreen.actionNavigationListToNavigationListAdd()
//        findNavController().navigate(action)

        // Coroutine suggestion found from:
        // https://stackoverflow.com/questions/59608923/launch-coroutine-from-click-event-in-fragmenthttps://stackoverflow.com/questions/59608923/launch-coroutine-from-click-event-in-fragment
        // This coroutine is to animate the dots appearing in the splash screen text

        val uiScope = CoroutineScope(Dispatchers.Main + job)

        uiScope.launch(Dispatchers.IO){
            withContext(Dispatchers.Main){
                var i = 1
                val num_dots = 3
                val theText = binding.loadingText.text.toString()
                while(true) {
                    delay(600L)
                    var dotString = theText
                    for(x in 1..i) {
                        dotString += "."
                    }
                    binding.loadingText.text = dotString
                    i = (i + 1) % (num_dots + 1)
                    if (DB_PULL_COMPLETED) {
                        val action = SplashScreenFragmentDirections.actionNavigationSplashScreenToNavigationSettings()
                        findNavController().navigate(action)
                    }
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        job.cancel()
    }
}