package com.senati.modaapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.modaapp.data.dao.ReporteDao
import com.senati.modaapp.data.model.ClienteConPedidos
import com.senati.modaapp.databinding.ActivityClientesBinding

class ClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientesBinding
    private lateinit var reporteDao: ReporteDao
    private lateinit var adapter: ClientesAdapter
    private var queryActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reporteDao = ReporteDao(this)

        binding.btnBackClientes.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupSearch()
        cargarClientes()
    }

    private fun setupRecyclerView() {
        binding.rvClientes.layoutManager = LinearLayoutManager(this)
        adapter = ClientesAdapter(emptyList()) { cliente ->
            mostrarOpcionesCliente(cliente)
        }
        binding.rvClientes.adapter = adapter
    }

    private fun setupSearch() {
        binding.etBuscarCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                queryActual = s?.toString()?.trim() ?: ""
                cargarClientes()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun cargarClientes() {
        // HU-12 CA3: Nombres, apellidos, teléfono y número de pedidos (COUNT + GROUP BY)
        val clientes = reporteDao.clientesConPedidos(queryActual)
        adapter.actualizarLista(clientes)

        if (clientes.isEmpty()) {
            binding.tvEmptyClientes.visibility = View.VISIBLE
            binding.rvClientes.visibility = View.GONE
        } else {
            binding.tvEmptyClientes.visibility = View.GONE
            binding.rvClientes.visibility = View.VISIBLE
        }
    }

    private fun mostrarOpcionesCliente(cliente: ClienteConPedidos) {
        val nombreCompleto = "${cliente.nombres} ${cliente.apellidos}".trim()
        val opciones = arrayOf(
            "Llamar al cliente (${cliente.telefono})",
            "Enviar WhatsApp"
        )

        AlertDialog.Builder(this)
            .setTitle(nombreCompleto)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${cliente.telefono}")
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        try {
                            val uri = Uri.parse("https://wa.me/51${cliente.telefono}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton(R.string.dialog_btn_cancelar, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        cargarClientes()
    }
}
