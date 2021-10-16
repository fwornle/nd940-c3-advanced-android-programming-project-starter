package com.udacity

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
    }

}
