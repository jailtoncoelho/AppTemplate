package com.ifpr.app_reuse.baseclasses


import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ifpr.app_reuse.R

class StoreAdapter(
    private val context: Context,
    private val storeList: List<Item>,
    private val userLocation: Location?
) :
    RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val denuncia = storeList[position]
        holder.denunciaTituloTextView.text = denuncia.titulo
        holder.denunciaEnderecoTextView.text = denuncia.endereco
        holder.denunciaDescricaoTextView.text = denuncia.descricao
        holder.denunciaDistanceTextView.text = getDistanceToStore(denuncia)

        if (holder.denunciaDistanceTextView.text.isEmpty()) {
            holder.denunciaDistanceTextView.visibility = View.GONE
        } else {
            holder.denunciaDistanceTextView.visibility = View.VISIBLE
        }

        // Ajustar a visibilidade do ícone do mapa
        if (denuncia.endereco.isEmpty()) {
            holder.mapIconImageView.visibility = View.GONE
            holder.wazeIconImageView.visibility = View.GONE
        } else {
            holder.mapIconImageView.visibility = View.VISIBLE
            holder.wazeIconImageView.visibility = View.GONE
        }
        holder.mapIconImageView.setOnClickListener {
            val endereco = holder.denunciaEnderecoTextView.text.toString()
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(endereco)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(it.context.packageManager) != null) {
                it.context.startActivity(mapIntent)
            }
        }

        holder.wazeIconImageView.setOnClickListener {
            val endereco = holder.denunciaEnderecoTextView.text.toString()
            val wazeUri = Uri.parse("https://waze.com/ul?q=${Uri.encode(endereco)}")
            val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri)
            wazeIntent.setPackage("com.waze")
            if (wazeIntent.resolveActivity(it.context.packageManager) != null) {
                it.context.startActivity(wazeIntent)
            } else {
                Toast.makeText(it.context, "Waze não está instalado", Toast.LENGTH_SHORT).show()
            }
        }

        Glide.with(holder.denunciaImageView.context)
            .load(denuncia.imageUrl)
            .into(holder.denunciaImageView)
    }


    private fun getDistanceToStore(store: Item): String {
        // Implementar lógica para calcular a distância entre currentAddress e storeAddress
        // Isso pode envolver usar uma API de mapas ou uma biblioteca de geocodificação

        if (userLocation == null) {
            return ""
        }
        val userLocation = Location("").apply {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
        }
        if (store.latitude.toInt() == 0 && store.longitude.toInt() == 0) {
            return ""
        }
        val storeLocation = Location("").apply {
            latitude = store.latitude
            longitude = store.longitude
        }
        val distanceInMeters = userLocation.distanceTo(storeLocation)
        val distanceInKm = distanceInMeters / 1000
        return String.format("%.2f km", distanceInKm)
    }

    override fun getItemCount(): Int {
        return storeList.size
    }

    class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val denunciaImageView: ImageView = itemView.findViewById(R.id.denunciaImageView)
        val denunciaTituloTextView: TextView = itemView.findViewById(R.id.denunciaTituloTextView)
        val denunciaDescricaoTextView: TextView = itemView.findViewById(R.id.denunciaDescricaoTextView)
        val denunciaEnderecoTextView: TextView = itemView.findViewById(R.id.denunciaEnderecoTextView)
        val denunciaDistanceTextView: TextView = itemView.findViewById(R.id.storeDistanceTextView)
        val mapIconImageView: ImageView = itemView.findViewById(R.id.mapIconImageView)
        val wazeIconImageView: ImageView = itemView.findViewById(R.id.wazeIconImageView)
    }
}
