package com.example.dayplanner.ui.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dayplanner.databinding.FragmentListBinding
import com.example.dayplanner.ui.list_add.ListAddFragment
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dayplanner.databinding.FragmentListAddBinding


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val listViewModel =
            ViewModelProvider(this).get(ListViewModel::class.java)

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textList
        listViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val fab = binding.floatingActionButton
        fab.show()
//        TODO: Set on click listener
        fab.setOnClickListener() {
            // fab.hide()
            val action = ListFragmentDirections.actionNavigationListToNavigationListAdd()
            findNavController().navigate(action)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}