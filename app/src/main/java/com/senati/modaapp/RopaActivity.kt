package com.senati.modaapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senati.modaapp.databinding.ActivityRopaBinding

class RopaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRopaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRopaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackRopa.setOnClickListener {
            finish()
        }
        binding.btnVolverMenuRopa.setOnClickListener {
            finish()
        }
    }
}
