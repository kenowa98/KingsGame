package com.kenowa.kingsgame.ui.noticia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.model.Noticia
import kotlinx.android.synthetic.main.fragment_noticia.*

class NoticiaFragment : Fragment() {
    private var allNews: MutableList<Noticia> = mutableListOf()
    private lateinit var noticiasAdapter: NoticiasRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_noticia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadNews()

        rv_noticias.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        noticiasAdapter = NoticiasRVAdapter(allNews as ArrayList<Noticia>)
        rv_noticias.adapter = noticiasAdapter
    }

    private fun loadNews() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("noticias")
        addNewsOnList(myRef)
    }

    private fun addNewsOnList(myRef: DatabaseReference) {
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val noticia = datasnapshot.getValue(Noticia::class.java)
                    allNews.add(noticia!!)
                }
                noticiasAdapter.notifyDataSetChanged()
            }
        }
        myRef.addValueEventListener(postListener)
    }
}