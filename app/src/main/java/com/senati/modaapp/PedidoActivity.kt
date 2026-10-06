package com.senati.modaapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.senati.modaapp.data.Carrito
import com.senati.modaapp.data.dao.ClienteDao
import com.senati.modaapp.data.dao.PedidoDao
import com.senati.modaapp.data.dao.UsuarioDao
import com.senati.modaapp.data.model.Cliente
import com.senati.modaapp.data.model.ItemCarrito
import com.senati.modaapp.databinding.ActivityPedidoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidoBinding
    private lateinit var clienteDao: ClienteDao
    private lateinit var pedidoDao: PedidoDao
    private lateinit var usuarioDao: UsuarioDao

    private var clienteExistente: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clienteDao = ClienteDao(this)
        pedidoDao = PedidoDao(this)
        usuarioDao = UsuarioDao(this)

        setupResumenPedido()
        setupListeners()
    }

    private fun setupResumenPedido() {
        val totalPrendas = Carrito.obtenerCantidadTotal()
        val totalPrecio = Carrito.obtenerTotal()

        binding.tvResumenCantidad.text = getString(R.string.resumen_prendas_format, totalPrendas)
        binding.tvResumenTotal.text = String.format(Locale.getDefault(), "S/ %.2f", totalPrecio)
    }

    private fun setupListeners() {
        binding.btnBackPedido.setOnClickListener {
            finish()
        }

        binding.etTelefono.doAfterTextChanged { s ->
            binding.tilTelefono.error = null
            val telefono = s?.toString()?.trim().orEmpty()

            if (telefono.length == 9) {
                verificarTelefono(telefono)
            } else {
                clienteExistente = null
                binding.tvSaludoCliente.visibility = View.GONE
                binding.layoutClienteNuevo.visibility = View.GONE
            }
        }

        binding.etNombres.doAfterTextChanged { binding.tilNombres.error = null }
        binding.etApellidos.doAfterTextChanged { binding.tilApellidos.error = null }

        binding.btnConfirmarPedido.setOnClickListener {
            confirmarPedido()
        }
    }

    private fun verificarTelefono(telefono: String) {
        val cliente = clienteDao.buscarPorTelefono(telefono)
        if (cliente != null) {
            clienteExistente = cliente
            binding.tvSaludoCliente.text = getString(R.string.saludo_cliente_format, cliente.nombres)
            binding.tvSaludoCliente.visibility = View.VISIBLE
            binding.layoutClienteNuevo.visibility = View.GONE
        } else {
            clienteExistente = null
            binding.tvSaludoCliente.visibility = View.GONE
            binding.layoutClienteNuevo.visibility = View.VISIBLE
        }
    }

    private fun confirmarPedido() {
        val telefono = binding.etTelefono.text?.toString()?.trim().orEmpty()

        if (telefono.length != 9) {
            binding.tilTelefono.error = "Ingresa un número de 9 dígitos"
            return
        }

        var idClienteFinal = clienteExistente?.id ?: 0
        var nombresCliente = clienteExistente?.nombres.orEmpty()
        var apellidosCliente = clienteExistente?.apellidos.orEmpty()

        if (clienteExistente == null) {
            val nombres = binding.etNombres.text?.toString()?.trim().orEmpty()
            val apellidos = binding.etApellidos.text?.toString()?.trim().orEmpty()

            var hayError = false
            if (nombres.isEmpty()) {
                binding.tilNombres.error = "Nombres requeridos"
                hayError = true
            }
            if (apellidos.isEmpty()) {
                binding.tilApellidos.error = "Apellidos requeridos"
                hayError = true
            }

            if (hayError) return

            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val nuevoCliente = Cliente(
                telefono = telefono,
                nombres = nombres,
                apellidos = apellidos,
                fechaRegistro = fechaActual
            )
            val idInsertado = clienteDao.insertar(nuevoCliente)
            if (idInsertado <= 0) {
                Toast.makeText(this, "Error al registrar cliente", Toast.LENGTH_SHORT).show()
                return
            }
            idClienteFinal = idInsertado.toInt()
            nombresCliente = nombres
            apellidosCliente = apellidos
        }

        val itemsCarrito = Carrito.obtenerItems()
        if (itemsCarrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val totalPedido = Carrito.obtenerTotal()
        val copiaItems = itemsCarrito.toList()

        val idPedido = pedidoDao.registrar(idClienteFinal, itemsCarrito)

        if (idPedido > 0) {
            Carrito.vaciar()
            Toast.makeText(
                this,
                getString(R.string.toast_pedido_registrado, idPedido),
                Toast.LENGTH_LONG
            ).show()

            // HU-10: Diálogo interactivo para envío de WhatsApp a cliente y a tienda
            mostrarDialogoWhatsApp(idPedido, telefono, nombresCliente, apellidosCliente, copiaItems, totalPedido)
        } else {
            Toast.makeText(this, "Error al procesar el pedido", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * HU-10: Envío de WhatsApp al cliente y al administrador
     */
    private fun mostrarDialogoWhatsApp(
        idPedido: Long,
        telefonoCliente: String,
        nombres: String,
        apellidos: String,
        items: List<ItemCarrito>,
        total: Double
    ) {
        // Mensaje al cliente
        val mensajeCliente = buildString {
            appendLine("ModaApp · Pedido #$idPedido")
            appendLine("Hola $nombres, recibimos tu pedido:")
            for (item in items) {
                appendLine("• ${item.cantidad} ${item.ropa.modelo} ${item.ropa.talla} ${item.ropa.color}")
            }
            appendLine(String.format(Locale.getDefault(), "Total: S/ %.2f", total))
            append("Estado: PENDIENTE")
        }

        // Mensaje a la tienda / admin
        val telefonoAdmin = usuarioDao.obtenerTelefonoAdmin()
        val mensajeAdmin = buildString {
            appendLine("ModaApp · Pedido #$idPedido")
            appendLine("Nuevo pedido de $nombres $apellidos (Tel: $telefonoCliente):")
            for (item in items) {
                appendLine("• ${item.cantidad} ${item.ropa.modelo} ${item.ropa.talla} ${item.ropa.color}")
            }
            appendLine(String.format(Locale.getDefault(), "Total: S/ %.2f", total))
            append("Estado: PENDIENTE")
        }

        val opciones = arrayOf("Enviar a mi WhatsApp", "Avisar a la tienda", "Finalizar")

        AlertDialog.Builder(this)
            .setTitle("Pedido #$idPedido Registrado")
            .setItems(opciones) { dialog, which ->
                when (which) {
                    0 -> {
                        abrirWhatsApp(telefonoCliente, mensajeCliente)
                    }
                    1 -> {
                        abrirWhatsApp(telefonoAdmin, mensajeAdmin)
                    }
                    2 -> {
                        dialog.dismiss()
                        finish()
                    }
                }
            }
            .setCancelable(false)
            .setPositiveButton("Cerrar") { _, _ ->
                finish()
            }
            .show()
    }

    private fun abrirWhatsApp(telefono: String, mensaje: String) {
        try {
            val uri = Uri.parse("https://wa.me/51$telefono?text=" + Uri.encode(mensaje))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // HU-10 CA3: Manejo si WhatsApp no está instalado sin cerrar la app
            Toast.makeText(this, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
