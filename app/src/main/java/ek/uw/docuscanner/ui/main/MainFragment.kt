package ek.uw.docuscanner.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ek.uw.docuscanner.R

class MainFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var newButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.main_fragment, container, false)
        newButton = root.findViewById(R.id.newButton)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        newButton.setOnClickListener { view ->
            view.findNavController().navigate(R.id.action_mainFragment_to_cameraFragment2)
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
