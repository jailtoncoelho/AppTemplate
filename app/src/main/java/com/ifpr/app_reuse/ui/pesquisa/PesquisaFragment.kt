package com.ifpr.app_reuse.ui.pesquisa


import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.app_reuse.R
import com.ifpr.app_reuse.baseclasses.Coleta
import com.ifpr.app_reuse.baseclasses.ColetaAdapter
import com.ifpr.app_reuse.baseclasses.Item
import com.ifpr.app_reuse.baseclasses.ItemColeta
import com.ifpr.app_reuse.baseclasses.StoreAdapter
import com.ifpr.app_reuse.databinding.FragmentPesquisaBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PesquisaFragment : Fragment() {

    private var _binding: FragmentPesquisaBinding? = null
    private lateinit var recyclerViewStores: RecyclerView
    private lateinit var storeAdapter: StoreAdapter
    private lateinit var currentAddressTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var recyclerViewColetas: RecyclerView
    private lateinit var coletaAdapter: ColetaAdapter
    private val coletas = mutableListOf<Coleta>() // Lista para armazenar as coletas


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPesquisaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        currentAddressTextView = root.findViewById(R.id.currentAddressTextView)
        recyclerViewStores = root.findViewById(R.id.recyclerViewDenuncias)
        recyclerViewStores.layoutManager = LinearLayoutManager(context)

        recyclerViewColetas = root.findViewById(R.id.recyclerViewColetas)
        recyclerViewColetas.layoutManager = LinearLayoutManager(context)




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        database = FirebaseDatabase.getInstance().reference

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            getCurrentLocation()
        }






        return root
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                fetchStores(null)
                Snackbar.make(
                    requireView(),
                    "Permission denied. Cannot access location.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadColetas() {
        database.child("coletas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                coletas.clear()
                for (snapshot in dataSnapshot.children) {
                    val coleta = snapshot.getValue(Item::class.java)

                    // Extrair manualmente a lista de itens
                    val itensList = mutableListOf<ItemColeta>()
                    val itensSnapshot = snapshot.child("itens")
                    for (itemSnapshot in itensSnapshot.children) {
                        val itemColeta = itemSnapshot.getValue(ItemColeta::class.java)
                        itemColeta?.let { itensList.add(it) }
                    }

                    coleta?.let {
                        val coleta =
                            Coleta(it.userId,it.titulo, it.endereco, it.descricao, it.imageUrl, itensList)
                        coletas.add(coleta)
                    }
                }
                coletaAdapter = ColetaAdapter(coletas)
                recyclerViewColetas.adapter = coletaAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to load coletas: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


    }


    private fun fetchStores(userLocation: Location?) {
        database.child("denuncias").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storeList = mutableListOf<Item>()
                for (storeSnapshot in snapshot.children) {
                    val store = storeSnapshot.getValue(Item::class.java)
                    store?.let { storeList.add(it) }
                }
                storeAdapter = StoreAdapter(requireContext(), storeList, userLocation)
                recyclerViewStores.adapter = storeAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to load denúncias: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fetchStores(null)
            // Carregar coletas do Firebase
            loadColetas()
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    displayAddress(location)
                    fetchStores(location)
                    // Carregar coletas do Firebase
                    loadColetas()
                }
            }
        }

        locationRequest = LocationRequest.create().apply {
            interval = 60000 // Intervalo em milissegundos para atualizações de localização
            fastestInterval =
                60000 // O menor intervalo de tempo que você deseja receber atualizações de localização
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun displayAddress(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = address
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = "Error: ${e.message}"
                }
            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}