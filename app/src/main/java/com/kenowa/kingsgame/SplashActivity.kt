package com.kenowa.kingsgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*
import kotlin.concurrent.timerTask

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        animation()
        runTimer()
    }

    private fun animation() {
        val animacion: Animation = AnimationUtils.loadAnimation(this, R.anim.entrada)
        iv_team.animation = animacion
    }

    private fun runTimer() {
        val timer = Timer()
        timer.schedule(
            timerTask {
                goToLoginActivity()
            }, 1800
        )
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}