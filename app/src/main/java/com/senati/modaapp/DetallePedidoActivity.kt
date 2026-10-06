package com.senati.modaapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.modaapp.data.dao.PedidoDao
import com.senati.modaapp.data.dao.ResultadoAtencion
import com.senati.modaapp.data.model.Pedido
import com.senati.modaapp.databinding.ActivityDetallePedidoBinding

class DetallePedidoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_PEDIDO = "extra_id_pedido"
    }

    private lateinit var binding: ActivityDetallePedidoBinding
    private lateinit var pedidoDao: PedidoDao
    private var idPedido: Int = -1
    private var pedidoActual: Pedido? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedidoDao = PedidoDao(this)
        idPedido = intent.getIntExtra(EXTRA_ID_PEDIDO, -1)

        binding.btnBackDetallePedido.setOnClickListener {
            finish()
        }

        binding.rvDetalleItems.layoutManager = LinearLayoutManager(this)

        cargarDatos()
    }

    private fun cargarDatos() {
        if (idPedido <= 0) {
            Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val pedido = pedidoDao.obtenerPorId(idPedido)
        if (pedido == null) {
            Toast.makeText(this, "Pedido no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        pedidoActual = pedido

        binding.tvTituloDetallePedido.text = getString(R.string.title_detalle_pedido, pedido.id)
        val nombreCompleto = "${pedido.nombresCliente} ${pedido.apellidosCliente}".trim()
        binding.tvDetalleCliente.text = nombreCompleto
        binding.tvDetalleTelefono.text = getString(R.string.detalle_pedido_telefono_label, pedido.telefonoCliente)
        binding.tvDetalleFecha.text = getString(R.string.detalle_pedido_fecha_label, pedido.fecha)

        if (!pedido.fechaAtencion.isNullOrBlank()) {
            binding.tvDetalleFechaAtencion.visibility = View.VISIBLE
            binding.tvDetalleFechaAtencion.text = getString(R.string.detalle_pedido_fecha_atencion_label, pedido.fechaAtencion)
        } else {
            binding.tvDetalleFechaAtencion.visibility = View.GONE
        }

        binding.tvDetalleEstado.text = pedido.estado
        binding.tvDetalleTotal.text = getString(R.string.reportes_monto_format, pedido.total)

        // Cargar lista de items
        val detalles = pedidoDao.obtenerDetalles(idPedido)
        binding.rvDetalleItems.adapter = DetallePedidoAdapter(detalles)

        // Botón Marcar como atendido
        if (pedido.estado.equals("PENDIENTE", ignoreCase = true)) {
            binding.btnDetalleMarcarAtendido.visibility = View.VISIBLE
            binding.btnDetalleMarcarAtendido.text = getString(R.string.btn_marcar_atendido_format, pedido.id)
            binding.btnDetalleMarcarAtendido.setOnClickListener {
                confirmarAtencion()
            }
        } else {
            binding.btnDetalleMarcarAtendido.visibility = View.GONE
        }

        // Botón Llamar al cliente
        binding.btnDetalleLlamarCliente.setOnClickListener {
            llamarCliente(pedido.telefonoCliente)
        }
    }

    private fun confirmarAtencion() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_confirmar_atender_titulo)
            .setMessage(getString(R.string.dialog_confirmar_atender_mensaje, idPedido))
            .setPositiveButton(R.string.btn_confirmar_atender) { _, _ ->
                ejecutarAtencion()
            }
            .setNegativeButton(R.string.dialog_btn_cancelar, null)
            .show()
    }

    private fun ejecutarAtencion() {
        when (val resultado = pedidoDao.atender(idPedido)) {
            is ResultadoAtencion.Exito -> {
                Toast.makeText(
                    this,
                    getString(R.string.toast_pedido_atendido, idPedido),
                    Toast.LENGTH_LONG
                ).show()
                setResult(RESULT_OK)
                cargarDatos()
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

    private fun llamarCliente(telefono: String) {
        if (telefono.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$telefono")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Teléfono no disponible", Toast.LENGTH_SHORT).show()
        }
    }
}
