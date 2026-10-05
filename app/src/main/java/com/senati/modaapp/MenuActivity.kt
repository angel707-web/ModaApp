package com.senati.modaapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senati.modaapp.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserHeader()
        setupNavigation()
    }

    private fun setupUserHeader() {
        val userName = intent.getStringExtra(LoginActivity.EXTRA_USER_NAME) ?: "admin"
        val userRole = intent.getStringExtra(LoginActivity.EXTRA_USER_ROLE) ?: "ADMIN"
        binding.tvGreetingAdmin.text = getString(R.string.greeting_admin_format, userName)
        binding.tvPendingOrders.text = getString(R.string.user_role_format, userRole)
    }

    private fun setupNavigation() {
        // Opción Ropa
        binding.cardRopa.setOnClickListener {
            startActivity(Intent(this, RopaActivity::class.java))
        }

        // Opción Pedidos
        binding.cardPedidos.setOnClickListener {
            startActivity(Intent(this, PedidosActivity::class.java))
        }

        // Opción Clientes
        binding.cardClientes.setOnClickListener {
            startActivity(Intent(this, ClientesActivity::class.java))
        }

        // Opción Reportes
        binding.cardReportes.setOnClickListener {
            startActivity(Intent(this, ReportesActivity::class.java))
        }

        // Salir: Regresar a LoginActivity
        binding.btnSalir.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
