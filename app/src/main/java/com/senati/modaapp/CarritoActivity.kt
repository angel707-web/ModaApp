package com.senati.modaapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.modaapp.data.Carrito
import com.senati.modaapp.data.model.ItemCarrito
import com.senati.modaapp.databinding.ActivityCarritoBinding
import java.util.Locale

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var carritoAdapter: CarritoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        cargarDatosCarrito()
    }

    private fun setupRecyclerView() {
        // HU-08 CA3: Mantén presionado para quitar prenda
        carritoAdapter = CarritoAdapter(Carrito.obtenerItems()) { item ->
            confirmarQuitarItem(item)
        }

        binding.rvCarrito.apply {
            layoutManager = LinearLayoutManager(this@CarritoActivity)
            adapter = carritoAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBackCarrito.setOnClickListener {
            finish()
        }

        // HU-09: Navegar al formulario de pedido
        binding.btnHacerPedido.setOnClickListener {
            if (!Carrito.estaVacio()) {
                startActivity(Intent(this, PedidoActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosCarrito()
    }

    private fun cargarDatosCarrito() {
        val items = Carrito.obtenerItems()
        carritoAdapter.actualizarLista(items)

        val total = Carrito.obtenerTotal()
        binding.tvTotalValue.text = String.format(Locale.getDefault(), "S/ %.2f", total)

        // HU-08 CA4: Carrito vacío
        if (items.isEmpty()) {
            binding.tvEmptyCarrito.visibility = View.VISIBLE
            binding.rvCarrito.visibility = View.GONE
            binding.tvHintQuitar.visibility = View.GONE
            binding.btnHacerPedido.isEnabled = false
            binding.btnHacerPedido.alpha = 0.5f
        } else {
            binding.tvEmptyCarrito.visibility = View.GONE
            binding.rvCarrito.visibility = View.VISIBLE
            binding.tvHintQuitar.visibility = View.VISIBLE
            binding.btnHacerPedido.isEnabled = true
            binding.btnHacerPedido.alpha = 1.0f
        }
    }

    private fun confirmarQuitarItem(item: ItemCarrito) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_confirmar_eliminar_titulo))
            .setMessage(getString(R.string.dialog_confirmar_quitar_item) + "\n\n${item.ropa.modelo}")
            .setPositiveButton(getString(R.string.dialog_btn_eliminar)) { _, _ ->
                Carrito.quitar(item)
                cargarDatosCarrito()
            }
            .setNegativeButton(getString(R.string.dialog_btn_cancelar), null)
            .show()
    }
}
