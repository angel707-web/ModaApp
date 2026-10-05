package com.senati.modaapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.senati.modaapp.data.dao.CategoriaDao
import com.senati.modaapp.data.dao.RopaDao
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
        catalogoAdapter = CatalogoAdapter()
        binding.rvCatalogo.apply {
            layoutManager = GridLayoutManager(this@CatalogoActivity, 2)
            adapter = catalogoAdapter
        }
    }

    private fun setupListeners() {
        // Regresar a la pantalla anterior (Login)
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Abrir Carrito de compras (flujo de cliente)
        binding.btnCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        // Chip "Todas"
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
    }

    private fun cargarPrendas() {
        // Cargar solo prendas disponibles (cantidad > 0)
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
}
