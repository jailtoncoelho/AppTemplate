package com.ifpr.app_reuse.ui.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ifpr.app_reuse.MainActivity
import com.ifpr.app_reuse.R
import com.ifpr.app_reuse.baseclasses.Item
import com.ifpr.app_reuse.databinding.FragmentChatBinding
import java.util.UUID


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null

    private lateinit var navController: NavController

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Initialize NavController
        navController = findNavController()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}