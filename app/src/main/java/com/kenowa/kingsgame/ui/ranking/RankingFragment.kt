package com.kenowa.kingsgame.ui.ranking

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kenowa.kingsgame.R
import com.kenowa.kingsgame.hideProgressBar
import com.kenowa.kingsgame.model.Ranking
import com.kenowa.kingsgame.organizeRanking
import com.kenowa.kingsgame.referenceDatabase
import kotlinx.android.synthetic.main.fragment_ranking.*
import kotlinx.android.synthetic.main.fragment_ranking.view.*

class RankingFragment : Fragment() {
    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_ranking, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        organizeRanking()
        top3()
        yourTeams()
    }

    private fun top3() {
        var pos1 = ""
        var teams1 = false
        var pos2 = ""
        var teams2 = false
        var pos3 = ""
        var teams3 = false
        val myRef = referenceDatabase("ranking")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val team = datasnapshot.getValue(Ranking::class.java)
                    when (team?.puesto) {
                        1 -> {
                            if (teams1) {
                                pos1 = pos1.plus("\n${team.id} (${team.puntaje})")
                            } else {
                                pos1 = pos1.plus("${team.id} (${team.puntaje})")
                                teams1 = true
                            }
                        }
                        2 -> {
                            if (teams2) {
                                pos2 = pos2.plus("\n${team.id} (${team.puntaje})")
                            } else {
                                pos2 = pos2.plus("${team.id} (${team.puntaje})")
                                teams2 = true
                            }
                        }
                        3 -> {
                            if (teams3) {
                                pos3 = pos3.plus("\n${team.id} (${team.puntaje})")
                            } else {
                                pos3 = pos3.plus("${team.id} (${team.puntaje})")
                                teams3 = true
                            }
                        }
                    }
                }
                root?.tv_pos1?.text = pos1
                root?.tv_pos2?.text = pos2
                root?.tv_pos3?.text = pos3
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun yourTeams() {
        val myRef = referenceDatabase("equipos")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var cont = 1
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val team = datasnapshot.value as Map<*, *>
                    if (isUserInTeam(team, cont)) {
                        ++cont
                        if (cont == 3) {
                            break
                        }
                    }
                }
                root?.progressBar?.let { hideProgressBar(it) }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }

    private fun isUserInTeam(
        team: Map<*, *>,
        cont: Int
    ): Boolean {
        val keys = team.keys
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        for (i in keys) {
            if (i == userID) {
                if (cont == 1) {
                    tv_mis_equipos.visibility = View.VISIBLE
                    linear1.visibility = View.VISIBLE
                    val data = team[i] as Map<*, *>
                    val id = data["nombre"] as String
                    root?.tv_team1?.let { root?.tv_posTeam1?.let { it1 -> loadData(id, it, it1) } }
                } else {
                    linear2.visibility = View.VISIBLE
                    val data = team[i] as Map<*, *>
                    val id = data["nombre"] as String
                    root?.tv_team2?.let { root?.tv_posTeam2?.let { it1 -> loadData(id, it, it1) } }
                }
                return true
            }
        }
        return false
    }

    private fun loadData(
        id: String,
        textView1: TextView,
        textView2: TextView
    ) {
        val myRef = referenceDatabase("ranking")
        val postListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    val team = datasnapshot.getValue(Ranking::class.java)
                    if (team?.id == id) {
                        textView1.text = "${team.id} (${team.puntaje})"
                        textView2.text = team.puesto.toString()
                    }
                }
            }
        }
        myRef.addListenerForSingleValueEvent(postListener)
    }
}