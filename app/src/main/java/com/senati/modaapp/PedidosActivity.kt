package com.senati.modaapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.modaapp.data.dao.PedidoDao
import com.senati.modaapp.data.dao.ResultadoAtencion
import com.senati.modaapp.data.model.Pedido
import com.senati.modaapp.databinding.ActivityPedidosBinding

class PedidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidosBinding
    private lateinit var pedidoDao: PedidoDao
    private lateinit var adapter: PedidosAdapter
    private var estadoActual = "PENDIENTE"
    private var pedidoSeleccionado: Pedido? = null

    private val detalleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cargarPedidos()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedidoDao = PedidoDao(this)

        binding.btnBackPedidos.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupTabs()
        setupAcciones()
        cargarPedidos()
    }

    private fun setupRecyclerView() {
        binding.rvPedidos.layoutManager = LinearLayoutManager(this)
        adapter = PedidosAdapter(
            listaPedidos = emptyList(),
            onPedidoSelected = { pedido ->
                pedidoSeleccionado = pedido
                actualizarBotonAtender(pedido)
            },
            onPedidoClickDetalle = { pedido ->
                abrirDetalle(pedido)
            }
        )
        binding.rvPedidos.adapter = adapter
    }

    private fun setupTabs() {
        binding.btnTabPendientes.setOnClickListener {
            if (estadoActual != "PENDIENTE") {
                estadoActual = "PENDIENTE"
                actualizarEstiloTabs()
                cargarPedidos()
            }
        }

        binding.btnTabAtendidos.setOnClickListener {
            if (estadoActual != "ATENDIDO") {
                estadoActual = "ATENDIDO"
                actualizarEstiloTabs()
                cargarPedidos()
            }
        }
    }

    private fun actualizarEstiloTabs() {
        if (estadoActual == "PENDIENTE") {
            binding.btnTabPendientes.setBackgroundResource(R.drawable.bg_button_primary)
            binding.btnTabPendientes.setTextColor(Color.WHITE)
            binding.btnTabAtendidos.setBackgroundColor(Color.TRANSPARENT)
            binding.btnTabAtendidos.setTextColor(getColor(R.color.text_primary))
            binding.panelAccionesPedido.visibility = View.VISIBLE
        } else {
            binding.btnTabAtendidos.setBackgroundResource(R.drawable.bg_button_primary)
            binding.btnTabAtendidos.setTextColor(Color.WHITE)
            binding.btnTabPendientes.setBackgroundColor(Color.TRANSPARENT)
            binding.btnTabPendientes.setTextColor(getColor(R.color.text_primary))
            // En atendidos solo mostramos el botón de llamar si se requiere o mantenemos oculto el de marcar
            binding.btnMarcarAtendido.visibility = View.GONE
        }
    }

    private fun setupAcciones() {
        binding.btnMarcarAtendido.setOnClickListener {
            val pedido = pedidoSeleccionado ?: adapter.obtenerPedidoSeleccionado()
            if (pedido != null) {
                confirmarAtencion(pedido)
            }
        }

        binding.btnLlamarCliente.setOnClickListener {
            val pedido = pedidoSeleccionado ?: adapter.obtenerPedidoSeleccionado()
            if (pedido != null && pedido.telefonoCliente.isNotBlank()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${pedido.telefonoCliente}")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No hay teléfono para llamar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarPedidos() {
        val lista = pedidoDao.listarPorEstado(estadoActual)
        adapter.actualizarLista(lista)

        if (lista.isEmpty()) {
            binding.tvEmptyPedidos.visibility = View.VISIBLE
            binding.tvEmptyPedidos.text = if (estadoActual == "PENDIENTE") {
                getString(R.string.empty_pedidos_pendientes)
            } else {
                getString(R.string.empty_pedidos_atendidos)
            }
            binding.panelAccionesPedido.visibility = View.GONE
            pedidoSeleccionado = null
        } else {
            binding.tvEmptyPedidos.visibility = View.GONE
            binding.panelAccionesPedido.visibility = View.VISIBLE
            pedidoSeleccionado = lista[0]
            actualizarBotonAtender(lista[0])
        }
    }

    private fun actualizarBotonAtender(pedido: Pedido) {
        if (estadoActual == "PENDIENTE") {
            binding.btnMarcarAtendido.visibility = View.VISIBLE
            binding.btnMarcarAtendido.text = getString(R.string.btn_marcar_atendido_format, pedido.id)
        } else {
            binding.btnMarcarAtendido.visibility = View.GONE
        }
    }

    private fun abrirDetalle(pedido: Pedido) {
        val intent = Intent(this, DetallePedidoActivity::class.java).apply {
            putExtra(DetallePedidoActivity.EXTRA_ID_PEDIDO, pedido.id)
        }
        detalleLauncher.launch(intent)
    }

    private fun confirmarAtencion(pedido: Pedido) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_confirmar_atender_titulo)
            .setMessage(getString(R.string.dialog_confirmar_atender_mensaje, pedido.id))
            .setPositiveButton(R.string.btn_confirmar_atender) { _, _ ->
                ejecutarAtencion(pedido)
            }
            .setNegativeButton(R.string.dialog_btn_cancelar, null)
            .show()
    }

    private fun ejecutarAtencion(pedido: Pedido) {
        when (val resultado = pedidoDao.atender(pedido.id)) {
            is ResultadoAtencion.Exito -> {
                Toast.makeText(
                    this,
                    getString(R.string.toast_pedido_atendido, pedido.id),
                    Toast.LENGTH_LONG
                ).show()
                cargarPedidos()
            }
            is ResultadoAtencion.Error -> {
                Toast.makeText(
                    this,
                    getString(R.string.error_stock_insuficiente_format, resultado.mensaje),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPedidos()
    }
}
