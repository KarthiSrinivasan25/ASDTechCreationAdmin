package com.ecommerce.asdtechcreationadmin.ui.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ecommerce.asdtechcreationadmin.R
import com.ecommerce.asdtechcreationadmin.databinding.ActivitySplashBinding
import com.ecommerce.asdtechcreationadmin.session.SessionManager
import com.ecommerce.asdtechcreationadmin.ui.dashboard.DashboardActivity
import com.ecommerce.asdtechcreationadmin.ui.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var player: ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide Action Bar
        supportActionBar?.hide()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()

        binding.playerView.player = player
        binding.playerView.useController = false
        binding.playerView.resizeMode =
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        val uri = Uri.parse("android.resource://$packageName/${R.raw.asd_tech_creation_logo}")

        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {

                    val isLoggedIn = SessionManager(this@SplashActivity).isLoggedIn()

                    val nextActivity = if (isLoggedIn) {
                        DashboardActivity::class.java
                    } else {
                        LoginActivity::class.java
                    }

                    startActivity(Intent(this@SplashActivity, nextActivity))
                    finish()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}