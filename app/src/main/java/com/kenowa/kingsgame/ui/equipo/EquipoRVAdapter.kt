package com.kenowa.kingsgame.ui.equipo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Player

class EquipoRVAdapter(
    private var playerList: ArrayList<Player>
) : RecyclerView.Adapter<EquipoRVAdapter.EquipoViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EquipoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return EquipoViewHolder(itemView)
    }

    override fun getItemCount(): Int = playerList.size

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val player: Player = playerList[position]
        holder.bindEquipo(player)
    }

    class EquipoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindEquipo(player: Player) {
        }
    }
}