package com.senati.modaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.senati.modaapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // Limpiar errores al modificar texto
        binding.etUsuario.doAfterTextChanged {
            binding.tilUsuario.error = null
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }

        // Ingresar como administrador
        binding.btnLoginAdmin.setOnClickListener {
            validarLogin()
        }

        // Ver catálogo (cliente)
        binding.btnVerCatalogo.setOnClickListener {
            val intent = Intent(this, CatalogoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarLogin() {
        val usuario = binding.etUsuario.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        var hayError = false

        if (usuario.isEmpty()) {
            binding.tilUsuario.error = getString(R.string.error_usuario_requerido)
            hayError = true
        } else {
            binding.tilUsuario.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_requerida)
            hayError = true
        } else {
            binding.tilPassword.error = null
        }

        if (hayError) {
            return
        }

        // Validación de credenciales para Sprint 1
        if (usuario == "admin" && password == "1234") {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish() // Login se cierra: atrás no regresa al login
        } else {
            Toast.makeText(this, getString(R.string.toast_credenciales_incorrectas), Toast.LENGTH_SHORT).show()
        }
    }
}
