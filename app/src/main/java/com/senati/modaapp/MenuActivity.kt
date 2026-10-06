package com.senati.modaapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senati.modaapp.data.SessionManager
import com.senati.modaapp.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupUserHeader()
        setupNavigation()
    }

    private fun setupUserHeader() {
        val userName = intent.getStringExtra(LoginActivity.EXTRA_USER_NAME) ?: sessionManager.obtenerUsuario()
        binding.tvGreetingAdmin.text = getString(R.string.greeting_admin_format, userName)
        actualizarContadorPendientes()
    }

    private fun actualizarContadorPendientes() {
        val pendientes = com.senati.modaapp.data.dao.ReporteDao(this).contarPedidos("PENDIENTE")
        binding.tvPendingOrders.text = if (pendientes > 0) {
            "Tienes $pendientes pedidos pendientes"
        } else {
            "No tienes pedidos pendientes"
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorPendientes()
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

        // HU-13 CA2: Salir borra la sesión recordada y regresa a LoginActivity
        binding.btnSalir.setOnClickListener {
            sessionManager.cerrarSesion()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
