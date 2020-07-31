package com.kenowa.kingsgame.ui.busqueda

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kenowa.kingsgame.model.Usuario
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_reclutar.view.*

class ReclutarRVAdapter(
    private var playerList: ArrayList<Usuario>,
    private val layout: Int,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ReclutarRVAdapter.ReclutarViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReclutarViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ReclutarViewHolder(itemView, onItemClickListener)
    }

    override fun getItemCount(): Int = playerList.size

    override fun onBindViewHolder(holder: ReclutarViewHolder, position: Int) {
        val usuario: Usuario = playerList[position]
        holder.bindSolicitud(usuario)
    }

    class ReclutarViewHolder(
        itemView: View,
        private var onItemClickListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private lateinit var player: Usuario

        @SuppressLint("SetTextI18n")
        fun bindSolicitud(usuario: Usuario) {
            player = usuario
            itemView.tv_nombre.text = "${player.nombre} ${player.apellido}"
            itemView.tv_posicion.text = player.posicion
            loadPhoto(player, itemView)

            itemView.setOnClickListener {
                clickItem(1)
            }

            itemView.bt_reclutar.setOnClickListener {
                clickItem(2)
            }
        }

        private fun loadPhoto(player: Usuario, itemView: View) {
            if (player.foto.isNotEmpty()) {
                Picasso.get().load(player.foto).into(itemView.iv_foto)
            }
        }

        private fun clickItem(case: Int) {
            onItemClickListener.onItemClick(player, case)
        }

        override fun onClick(v: View?) {
        }
    }

    interface OnItemClickListener {
        fun onItemClick(usuario: Usuario, case: Int)
    }
}