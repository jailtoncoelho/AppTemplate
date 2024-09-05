package com.ifpr.app_reuse.ui.coleta

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import com.ifpr.app_reuse.baseclasses.ItemColeta
import com.ifpr.app_reuse.baseclasses.ItemColetaAdapter
import com.ifpr.app_reuse.databinding.FragmentColetaSeletivaBinding
import java.util.UUID


class ColetaSeletivaFragment : Fragment() {

    private var _binding: FragmentColetaSeletivaBinding? = null

    private lateinit var navController: NavController
    private lateinit var coletaImageView: ImageView
    private lateinit var coletaTituloEditText: EditText
    private lateinit var coletaDescricaoEditText: EditText
    private lateinit var coletaEnderecoEditText: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var editTextQuantity: EditText
    private lateinit var radioGroupUnit: RadioGroup
    private lateinit var buttonAddItem: Button
    private lateinit var listViewItems: ListView
    private lateinit var selectImageButton: Button
    private lateinit var registerColetaButton: Button
    private var imageUri: Uri? = null
    private lateinit var storageReference: StorageReference

    private val itemList = mutableListOf<ItemColeta>()

    private lateinit var adapter: ArrayAdapter<String>

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
        val view = inflater.inflate(R.layout.fragment_coleta_seletiva, container, false)

        // Initialize NavController
        navController = findNavController()

        coletaImageView = view.findViewById(R.id.image_coleta)
        coletaTituloEditText = view.findViewById(R.id.edit_text_coleta_titulo)
        coletaDescricaoEditText = view.findViewById(R.id.edit_text_coleta_descricao)
        coletaEnderecoEditText = view.findViewById(R.id.edit_text_coleta_endereco)
        selectImageButton = view.findViewById(R.id.button_select_image)
        registerColetaButton = view.findViewById(R.id.button_register_coleta)

        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        editTextQuantity = view.findViewById(R.id.editTextQuantity)
        radioGroupUnit = view.findViewById(R.id.radioGroupUnit)
        buttonAddItem = view.findViewById(R.id.buttonAddItem)
        listViewItems = view.findViewById(R.id.listViewItems)

        val adapter = ItemColetaAdapter(requireContext(), itemList)
        listViewItems.adapter = adapter


        try {
            storageReference = FirebaseStorage.getInstance().reference.child("coletas_images")
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Erro ao obter referência para o Firebase Storage", e)
            // Trate o erro conforme necessário, por exemplo:
            Toast.makeText(context, "Erro ao acessar o Firebase Storage", Toast.LENGTH_SHORT).show()
        }

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        registerColetaButton.setOnClickListener {
            val titulo = coletaTituloEditText.text.toString()
            val descricao = coletaDescricaoEditText.text.toString()
            val endereco = coletaEnderecoEditText.text.toString()

            if (imageUri == null) {
                Toast.makeText(context, "Por favor, adicione uma imagem", Toast.LENGTH_SHORT)
                    .show()
            } else if (titulo.isEmpty() || descricao.isEmpty() || endereco.isEmpty()) {
                Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT)
                    .show()
            } else {

                // Exibe o ProgressDialog enquanto o upload está em andamento
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle("Enviando...")
                progressDialog.setCancelable(false)
                progressDialog.show()


                try {
                    // Chama o método de upload da imagem
                    uploadImageToFirebase()
                    // Fecha o ProgressDialog ao término do upload
                    progressDialog.dismiss()

                } catch (e: Exception) {
                    // Fecha o ProgressDialog em caso de erro
                    progressDialog.dismiss()
                    Toast.makeText(
                        context,
                        "Erro ao cadastrar a nova solicitação de coleta: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }

            }
        }

        buttonAddItem.setOnClickListener {
            val category = spinnerCategory.selectedItem.toString()
            val quantityText = editTextQuantity.text.toString()
            val selectedUnitId = radioGroupUnit.checkedRadioButtonId
            val unit = view.findViewById<RadioButton>(selectedUnitId)?.text.toString()

            if (quantityText.isNotEmpty()) {
                // Converta a quantidade para Double
                val quantity = quantityText.toDoubleOrNull()
                if (quantity != null) {
                    val newItem = ItemColeta(categoria = category, quantidade = quantity, unidade = unit)
                    itemList.add(newItem)
                    adapter.notifyDataSetChanged()

                    editTextQuantity.text.clear()
                    radioGroupUnit.clearCheck()
                } else {
                    Toast.makeText(requireContext(), "Quantidade inválida", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Por favor, digite a quantidade", Toast.LENGTH_SHORT).show()
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

    fun clearFields() {
        coletaImageView.setImageDrawable(null)
        coletaTituloEditText.text.clear()
        coletaDescricaoEditText.text.clear()
        coletaEnderecoEditText.text.clear()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(coletaImageView)
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
        val titulo = coletaTituloEditText.text.toString()
        val descricao = coletaDescricaoEditText.text.toString()
        val endereco = coletaEnderecoEditText.text.toString()

        if (titulo.isEmpty() || descricao.isEmpty() || endereco.isEmpty()) {
            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT)
                .show()
            return
        }
        var user = MainActivity.usuarioLogado
        val coleta = Item(
            "", titulo, descricao, "", imageUrl, endereco, user?.uid.toString(), false,
            0.0, 0.0, ""
        )

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val storesReference: DatabaseReference = database.getReference("coletas")

        // Verifica se a referência "denúncias" existe
        storesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Se o nó "stores" não existe, cria a referência
                    storesReference.setValue("initial_value") // Você pode definir um valor inicial se necessário
                        .addOnSuccessListener {
                            saveColetaToDatabase(coleta, storesReference)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Falha ao criar referência 'solicitação de coleta'",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Se o nó "stores" já existe, salva a loja diretamente
                    saveColetaToDatabase(coleta, storesReference)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Erro ao verificar referência 'solicitação de coleta'",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    private fun saveColetaToDatabase(store: Item, storesReference: DatabaseReference) {
        // Cria uma chave única para a nova coleta
        val coletaId = storesReference.push().key
        if (coletaId != null) {
            storesReference.child(coletaId).setValue(store)
                .addOnSuccessListener {
                    // Pega os itens do ListView
                    val itens = getItensFromListView() // Método para converter ListView para lista de ItemColeta
                    // Salva os itens dentro da coleta
                    saveItensToDatabase(coletaId, itens, storesReference)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Falha ao cadastrar a solicitação de coleta", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            Toast.makeText(context, "Erro ao gerar o ID da solicitação de coleta", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para obter os itens do ListView e transformá-los em uma lista de ItemColeta
    private fun getItensFromListView(): List<ItemColeta> {
        val itens = mutableListOf<ItemColeta>()

        val adapter = listViewItems.adapter as? ArrayAdapter<ItemColeta> // Verifica se o adapter é um ArrayAdapter de ItemColeta
        if (adapter != null) {
            for (i in 0 until adapter.count) {
                val item = adapter.getItem(i)
                if (item != null) {
                    itens.add(item)
                }
            }
        }

        return itens
    }



    private fun saveItensToDatabase(
        coletaId: String,
        itens: List<ItemColeta>, // Atualizado para usar a classe ItemColeta
        storesReference: DatabaseReference
    ) {
        // Caminho para os itens da coleta
        val itensReference = storesReference.child(coletaId).child("itens")

        // Salva cada item individualmente dentro da coleta
        itens.forEachIndexed { index, item ->
            val itemId = itensReference.push().key // Cria uma chave para cada item
            if (itemId != null) {
                itensReference.child(itemId).setValue(item) // Salva o item de tipo ItemColeta
                    .addOnSuccessListener {
                        if (index == itens.size - 1) {
                            // Exibe a mensagem de sucesso quando todos os itens forem salvos
                            Toast.makeText(context, "Solicitação de coleta cadastrada com sucesso!", Toast.LENGTH_SHORT)
                                .show()

                            clearFields()

                            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                            bottomNavigationView.selectedItemId = R.id.navigation_pesquisar

                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Falha ao salvar um item da coleta", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
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