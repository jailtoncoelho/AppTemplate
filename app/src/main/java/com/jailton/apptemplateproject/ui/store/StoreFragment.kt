package com.jailton.apptemplateproject.ui.store

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
import com.jailton.apptemplateproject.MainActivity
import com.jailton.apptemplateproject.R
import com.jailton.apptemplateproject.baseclasses.Item
import com.jailton.apptemplateproject.databinding.FragmentStoreBinding
import java.util.UUID


class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null

    private lateinit var navController: NavController
    private lateinit var storeImageView: ImageView
    private lateinit var storeNameEditText: EditText
    private lateinit var storeEmailEditText: EditText
    private lateinit var storeEnderecoEditText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var registerStoreButton: Button
    private var imageUri: Uri? = null
    private lateinit var storageReference: StorageReference

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        // Initialize NavController
        navController = findNavController()

        storeImageView = view.findViewById(R.id.image_store)
        storeNameEditText = view.findViewById(R.id.edit_text_store_name)
        storeEmailEditText = view.findViewById(R.id.edit_text_store_email)
        storeEnderecoEditText = view.findViewById(R.id.edit_text_store_endereco)
        selectImageButton = view.findViewById(R.id.button_select_image)
        registerStoreButton = view.findViewById(R.id.button_register_store)

        try {
            storageReference = FirebaseStorage.getInstance().reference.child("store_images")
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Erro ao obter referência para o Firebase Storage", e)
            // Trate o erro conforme necessário, por exemplo:
            Toast.makeText(context, "Erro ao acessar o Firebase Storage", Toast.LENGTH_SHORT).show()
        }


        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        registerStoreButton.setOnClickListener {
            val name = storeNameEditText.text.toString()
            val email = storeEmailEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || imageUri == null) {
                Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                uploadImageToFirebase()
            }
        }

        return view
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(storeImageView)
        }
    }

    private fun uploadImageToFirebase() {
        if (imageUri != null) {
            val fileReference = storageReference.child(UUID.randomUUID().toString())
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        registerStore(imageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Falha ao fazer upload da imagem", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun registerStore(imageUrl: String) {
        val name = storeNameEditText.text.toString()
        val email = storeEmailEditText.text.toString()
        val endereco = storeEnderecoEditText.text.toString()

        if (name.isEmpty() || email.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT)
                .show()
            return
        }
        var user = MainActivity.usuarioLogado
        val store = Item("", name, email, imageUrl, endereco, user?.uid.toString(),0.0,0.0, "")

        val database: FirebaseDatabase =
            FirebaseDatabase.getInstance("https://apptemplate-35820-default-rtdb.firebaseio.com/")
        val storesReference: DatabaseReference = database.getReference("stores")

        // Verifica se a referência "stores" existe
        storesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Se o nó "stores" não existe, cria a referência
                    storesReference.setValue("initial_value") // Você pode definir um valor inicial se necessário
                        .addOnSuccessListener {
                            saveStoreToDatabase(store, storesReference)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Falha ao criar referência 'stores'",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Se o nó "stores" já existe, salva a loja diretamente
                    saveStoreToDatabase(store, storesReference)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao verificar referência 'stores'", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun saveStoreToDatabase(store: Item, storesReference: DatabaseReference) {
        // Cria uma chave única para a nova loja
        val storeId = storesReference.push().key
        if (storeId != null) {
            storesReference.child(storeId).setValue(store)
                .addOnSuccessListener {
                    Toast.makeText(context, "Loja cadastrada com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Falha ao cadastrar a loja", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Erro ao gerar o ID da loja", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Acessar usuarioLogado
        var useFirebase = MainActivity.usuarioLogado

        // Verifica se o usuário atual já está definido
        if (useFirebase == null) {
            Toast.makeText(
                context,
                "Por favor, faça o login antes de prosseguir!",
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}