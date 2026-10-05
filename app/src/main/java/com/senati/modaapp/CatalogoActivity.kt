package com.senati.modaapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.senati.modaapp.data.Carrito
import com.senati.modaapp.data.dao.CategoriaDao
import com.senati.modaapp.data.dao.RopaDao
import com.senati.modaapp.data.model.Ropa
import com.senati.modaapp.databinding.ActivityCatalogoBinding

class CatalogoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatalogoBinding
    private lateinit var categoriaDao: CategoriaDao
    private lateinit var ropaDao: RopaDao
    private lateinit var catalogoAdapter: CatalogoAdapter

    private var categoriaSeleccionadaId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoriaDao = CategoriaDao(this)
        ropaDao = RopaDao(this)

        setupRecyclerView()
        setupListeners()
        cargarCategoriasChips()
    }

    private fun setupRecyclerView() {
        // HU-08 CA1: Al pulsar Agregar, diálogo para seleccionar cantidad respetando stock disponible
        catalogoAdapter = CatalogoAdapter { ropa ->
            mostrarDialogoSeleccionarCantidad(ropa)
        }
        binding.rvCatalogo.apply {
            layoutManager = GridLayoutManager(this@CatalogoActivity, 2)
            adapter = catalogoAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        binding.chipTodas.setOnClickListener {
            categoriaSeleccionadaId = null
            cargarPrendas()
        }
    }

    private fun cargarCategoriasChips() {
        val categorias = categoriaDao.listar()

        for (categoria in categorias) {
            val chip = Chip(this).apply {
                text = categoria.nombre
                isCheckable = true
                setOnClickListener {
                    categoriaSeleccionadaId = categoria.id
                    cargarPrendas()
                }
            }
            binding.chipGroupCategorias.addView(chip)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarPrendas()
        actualizarBadgeCarrito()
    }

    private fun actualizarBadgeCarrito() {
        val totalPrendas = Carrito.obtenerCantidadTotal()
        if (totalPrendas > 0) {
            binding.tvBadgeCount.text = totalPrendas.toString()
            binding.tvBadgeCount.visibility = View.VISIBLE
        } else {
            binding.tvBadgeCount.visibility = View.GONE
        }
    }

    private fun cargarPrendas() {
        val prendasDisponibles = ropaDao.listarDisponibles(categoriaSeleccionadaId)
        catalogoAdapter.actualizarLista(prendasDisponibles)

        if (prendasDisponibles.isEmpty()) {
            binding.tvEmptyCatalogo.visibility = View.VISIBLE
            binding.rvCatalogo.visibility = View.GONE
        } else {
            binding.tvEmptyCatalogo.visibility = View.GONE
            binding.rvCatalogo.visibility = View.VISIBLE
        }
    }

    /**
     * HU-08 CA1: Muestra selector de cantidad con límite de stock disponible ("Disponible: N")
     */
    private fun mostrarDialogoSeleccionarCantidad(ropa: Ropa) {
        if (ropa.cantidad <= 0) {
            Toast.makeText(this, "Prenda agotada", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(android.R.layout.select_dialog_item, null)
        val picker = NumberPicker(this).apply {
            minValue = 1
            maxValue = ropa.cantidad
            value = 1
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
            .setTitle(ropa.modelo)
            .setMessage(getString(R.string.dialog_stock_disponible_format, ropa.cantidad))
            .setView(picker)
            .setPositiveButton(getString(R.string.dialog_btn_agregar_carrito)) { _, _ ->
                val cantidadElegida = picker.value
                Carrito.agregar(ropa, cantidadElegida)
                actualizarBadgeCarrito()
                Toast.makeText(this, "Agregado: ${ropa.modelo} (x$cantidadElegida)", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.dialog_btn_cancelar), null)
            .show()
    }
}
