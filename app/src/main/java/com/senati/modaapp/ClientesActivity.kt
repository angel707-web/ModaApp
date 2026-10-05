package com.senati.modaapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senati.modaapp.databinding.ActivityClientesBinding

class ClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackClientes.setOnClickListener {
            finish()
        }
        binding.btnVolverMenuClientes.setOnClickListener {
            finish()
        }
    }
}
