package com.ifpr.app_reuse.baseclasses

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ifpr.app_reuse.R

class ColetaAdapter(private val coletas: List<Coleta>) :
    RecyclerView.Adapter<ColetaAdapter.ColetaViewHolder>() {

    inner class ColetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewTitulo: TextView = view.findViewById(R.id.textViewTitulo)
        val textViewEndereco: TextView = view.findViewById(R.id.textViewEndereco)
        val textViewDescricao: TextView = view.findViewById(R.id.textViewDescricao)
        val listViewItens: ListView = view.findViewById(R.id.listViewItems)
        val mapIconImageView: ImageView = itemView.findViewById(R.id.mapIconImageView)
        val wazeIconImageView: ImageView = itemView.findViewById(R.id.wazeIconImageView)
        val coletaImageView: ImageView = itemView.findViewById(R.id.coletaImageView)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coleta_recycler, parent, false)
        return ColetaViewHolder(view)
    }

    fun setListViewHeightBasedOnItems(listView: ListView) {
        val listAdapter = listView.adapter ?: return

        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }


    override fun onBindViewHolder(holder: ColetaViewHolder, position: Int) {
        val coleta = coletas[position]
        holder.textViewTitulo.text = coleta.titulo
        holder.textViewEndereco.text = coleta.endereco
        holder.textViewDescricao.text = coleta.descricao

        // Configurar o ListView para os itens da coleta
        val itemAdapter = ItemColetaAdapter(holder.itemView.context, coleta.itens)
        holder.listViewItens.adapter = itemAdapter
        setListViewHeightBasedOnItems(holder.listViewItens)


        Glide.with(holder.coletaImageView.context)
            .load(coleta.imageUrl)
            .into(holder.coletaImageView)


        // Ajustar a visibilidade do ícone do mapa
        if (coleta.endereco.isEmpty()) {
            holder.mapIconImageView.visibility = View.GONE
            holder.wazeIconImageView.visibility = View.GONE
        } else {
            holder.mapIconImageView.visibility = View.VISIBLE
            holder.wazeIconImageView.visibility = View.GONE
        }
        holder.mapIconImageView.setOnClickListener {
            val endereco = holder.textViewEndereco.text.toString()
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(endereco)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(it.context.packageManager) != null) {
                it.context.startActivity(mapIntent)
            }
        }
    }

    override fun getItemCount(): Int = coletas.size
}
