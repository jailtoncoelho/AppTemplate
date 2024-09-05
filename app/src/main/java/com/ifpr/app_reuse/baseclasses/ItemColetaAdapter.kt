package com.ifpr.app_reuse.baseclasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.ifpr.app_reuse.R

class ItemColetaAdapter(context: Context, items: List<ItemColeta>) :
    ArrayAdapter<ItemColeta>(context, R.layout.item_coleta, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_coleta, parent, false)
        val item = getItem(position)
        val textView = view.findViewById<TextView>(R.id.textView)

        textView.text = "${item?.categoria} - ${item?.quantidade} ${item?.unidade}"

        return view
    }
}




