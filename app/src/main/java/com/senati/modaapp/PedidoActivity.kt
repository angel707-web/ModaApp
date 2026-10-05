package com.senati.modaapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.senati.modaapp.data.Carrito
import com.senati.modaapp.data.dao.ClienteDao
import com.senati.modaapp.data.dao.PedidoDao
import com.senati.modaapp.data.model.Cliente
import com.senati.modaapp.databinding.ActivityPedidoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPedidoBinding
    private lateinit var clienteDao: ClienteDao
    private lateinit var pedidoDao: PedidoDao

    private var clienteExistente: Cliente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clienteDao = ClienteDao(this)
        pedidoDao = PedidoDao(this)

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

        // HU-09 CA1 & CA2: Detección automática al completar los 9 dígitos del teléfono
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
            // HU-09 CA1: Cliente ya registrado
            clienteExistente = cliente
            binding.tvSaludoCliente.text = getString(R.string.saludo_cliente_format, cliente.nombres)
            binding.tvSaludoCliente.visibility = View.VISIBLE
            binding.layoutClienteNuevo.visibility = View.GONE
        } else {
            // HU-09 CA2: Número nuevo
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

        if (clienteExistente == null) {
            // Validar campos de cliente nuevo
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

            // Registrar nuevo cliente en SQLite
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
        }

        // HU-09 CA3: Registro transaccional del pedido y detalle
        val itemsCarrito = Carrito.obtenerItems()
        if (itemsCarrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val idPedido = pedidoDao.registrar(idClienteFinal, itemsCarrito)

        if (idPedido > 0) {
            // HU-09 CA3 & CA4: Carrito se vacía y se muestra Toast Pedido #N registrado
            Carrito.vaciar()
            Toast.makeText(
                this,
                getString(R.string.toast_pedido_registrado, idPedido),
                Toast.LENGTH_LONG
            ).show()

            // Cerrar y retornar
            finish()
        } else {
            Toast.makeText(this, "Error al procesar el pedido", Toast.LENGTH_SHORT).show()
        }
    }
}
