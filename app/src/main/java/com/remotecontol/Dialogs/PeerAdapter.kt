package com.remotecontol.Dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.remotecontol.R

class PeerAdapter(val peers: MutableList<List<String?>>, val context: Context) : BaseAdapter() {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.peer_list_item, parent, false)

        view.findViewById<TextView>(R.id.Name).setText(peers[position][1])

        return view
    }

    override fun getItem(p0: Int): List<String?> {
        return peers[p0]
    }

    override fun getCount(): Int {
        return peers.size
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

}