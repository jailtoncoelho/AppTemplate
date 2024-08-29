package com.jailton.apptemplateproject.ui.usuario

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.jailton.apptemplateproject.MainActivity
import com.jailton.apptemplateproject.R
import com.jailton.apptemplateproject.baseclasses.Usuario
import com.jailton.apptemplateproject.databinding.FragmentPerfilUsuarioBinding


class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null

    private lateinit var userProfileImageView: ImageView
    private lateinit var registerNameEditText: EditText
    private lateinit var registerEmailEditText: EditText
    private lateinit var registerPasswordEditText: EditText
    private lateinit var registerConfirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sairButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false)

        // Inicializa o Firebase Auth
        auth = FirebaseAuth.getInstance()

        userProfileImageView = view.findViewById(R.id.userProfileImageView)
        registerNameEditText = view.findViewById(R.id.registerNameEditText)
        registerEmailEditText = view.findViewById(R.id.registerEmailEditText)
        registerPasswordEditText = view.findViewById(R.id.registerPasswordEditText)
        registerConfirmPasswordEditText = view.findViewById(R.id.registerConfirmPasswordEditText)
        registerButton = view.findViewById(R.id.registerButton)
        sairButton = view.findViewById(R.id.sairButton)

        // Acessar currentUser
        val user = MainActivity.usuarioLogado

        if (user != null) {
            sairButton.visibility = View.VISIBLE
            registerPasswordEditText.visibility = View.GONE
            registerConfirmPasswordEditText.visibility = View.GONE
            registerEmailEditText.isEnabled = false
        }

        user?.let {
            // Exibe a foto do perfil usando a biblioteca Glide
            Glide.with(this).load(it.photoUrl).into(userProfileImageView)
        }

        registerButton.setOnClickListener {
            updateUser()
        }

        sairButton.setOnClickListener {
            signOut()
        }

        return view
    }

    private fun signOut() {
        auth.signOut()
        MainActivity.usuarioLogado = null
        Toast.makeText(
            context,
            "Logout realizado com sucesso!",
            Toast.LENGTH_SHORT
        ).show()
        MainActivity.usuarioLogado = null

        requireActivity().finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Exibe os dados do usuário logado, se disponível

        // Acessar currentUser
        val user = MainActivity.usuarioLogado

        var user_firebase = MainActivity.usuarioLogado
        if(user_firebase != null){
            registerNameEditText.setText(user_firebase.displayName)
            registerEmailEditText.setText(user_firebase.email)
        }
    }

    private fun updateUser() {
        val name = registerNameEditText.text.toString().trim()

        // Acessar currentUser
        val user = MainActivity.usuarioLogado

        // Verifica se o usuário atual já está definido
        if (user != null) {
            // Se o usuário já existe, atualiza os dados
            updateProfile(user, name)
        } else {
            Toast.makeText(context, "Não foi possível encontrar o usuário logado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile(user: FirebaseUser?, displayName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Nome do usuario alterado com sucesso.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Não foi possivel alterar o nome do usuario.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}