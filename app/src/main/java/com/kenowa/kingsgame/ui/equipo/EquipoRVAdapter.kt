package com.kenowa.kingsgame.ui.equipo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Equipo

class EquipoRVAdapter(
    private var equipoList: ArrayList<Equipo>
) : RecyclerView.Adapter<EquipoRVAdapter.EquipoViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EquipoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return EquipoViewHolder(itemView)
    }

    override fun getItemCount(): Int = equipoList.size

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val equipo: Equipo = equipoList[position]
        holder.bindEquipo(equipo)
    }

    class EquipoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindEquipo(equipo: Equipo) {
        }
    }
}