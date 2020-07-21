package com.kenowa.kingsgame.ui.equipo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.model.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_player_edit.view.*

class EquipoRVAdapter(
    private var playerList: ArrayList<Usuario>,
    private val layout: Int,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<EquipoRVAdapter.EquipoViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EquipoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return EquipoViewHolder(itemView, onItemClickListener)
    }

    override fun getItemCount(): Int = playerList.size

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val player: Usuario = playerList[position]
        holder.bindEquipo(player)
    }

    class EquipoViewHolder(
        itemView: View,
        private var onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var usuario: Usuario

        @SuppressLint("SetTextI18n")
        fun bindEquipo(player: Usuario) {
            usuario = player
            itemView.tv_nombre.text = "${player.nombre} ${player.apellido}"
            itemView.tv_posicion.text = player.posicion
            loadPhoto(player, itemView)
            itemView.setOnClickListener(this)
        }

        private fun loadPhoto(player: Usuario, itemView: View) {
            if (player.foto.isNotEmpty()) {
                Picasso.get().load(player.foto).into(itemView.iv_foto)
            }
        }

        override fun onClick(v: View?) {
            onItemClickListener.onItemClick(usuario)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(usuario: Usuario)
    }
}
