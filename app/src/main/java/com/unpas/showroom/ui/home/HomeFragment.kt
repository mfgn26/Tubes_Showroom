package com.unpas.showroom.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.unpas.showroom.R
import com.unpas.showroom.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onResume() {
        super.onResume()

        // Menyembunyikan tombol appBarMain
        (requireActivity() as AppCompatActivity).findViewById<FloatingActionButton>(R.id.fab).hide()
    }

    override fun onPause() {
        super.onPause()

        // Menampilkan kembali tombol appBarMain
        (requireActivity() as AppCompatActivity).findViewById<FloatingActionButton>(R.id.fab).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}