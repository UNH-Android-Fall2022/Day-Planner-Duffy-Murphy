package com.example.dayplanner.ui.list_add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.databinding.FragmentListAddBinding

class ListAddFragment : Fragment() {

    private var _binding: FragmentListAddBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val listAddViewModel =
            ViewModelProvider(this).get(ListAddViewModel::class.java)

        _binding = FragmentListAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textList
        listAddViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}