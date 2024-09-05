package com.ifpr.app_reuse.ui.home


import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
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
import com.ifpr.app_reuse.baseclasses.Item
import com.ifpr.app_reuse.baseclasses.StoreAdapter
import com.ifpr.app_reuse.databinding.FragmentHomeBinding
import com.ifpr.app_reuse.ui.coleta.ColetaSeletivaFragment
import com.ifpr.app_reuse.ui.denuncia.DenunciaFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerViewStores: RecyclerView
    private lateinit var storeAdapter: StoreAdapter
    private lateinit var currentAddressTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest



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

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonColeta = view.findViewById<Button>(R.id.button_solicitacao_coleta)
        val buttonDenuncias = view.findViewById<Button>(R.id.button_denuncias)

        // Usando NavController para navegar
        val navController = view.findNavController()

        buttonColeta.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_coletaSeletivaFragment)
        }

        buttonDenuncias.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_denunciaFragment)
        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}