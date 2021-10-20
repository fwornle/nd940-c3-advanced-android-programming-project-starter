package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding


class DetailActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding & layout inflation
        binding = ActivityDetailBinding.inflate(layoutInflater)
        binding.root.apply { setContentView(this) }

        setSupportActionBar(binding.toolbar)

        // RETURN to main screen
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // fetch parameters which are communicated as part of the intent that got us here...
        val selUrl: String = intent.getStringExtra(MainActivity.REPO_URL_KEY).toString()

        // adjust text view
        binding.includes.tvRepoName.text = selUrl

    }

}
