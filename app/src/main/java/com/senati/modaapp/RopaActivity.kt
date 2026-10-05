package com.senati.modaapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.modaapp.data.dao.RopaDao
import com.senati.modaapp.databinding.ActivityRopaBinding

class RopaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRopaBinding
    private lateinit var ropaDao: RopaDao
    private lateinit var ropaAdapter: RopaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRopaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ropaDao = RopaDao(this)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        // HU-07 CA1: Al tocar una prenda se abre en modo edición
        ropaAdapter = RopaAdapter { ropa ->
            val intent = Intent(this, RegistrarRopaActivity::class.java).apply {
                putExtra(RegistrarRopaActivity.EXTRA_ID_ROPA, ropa.id)
            }
            startActivity(intent)
        }

        binding.rvRopa.apply {
            layoutManager = LinearLayoutManager(this@RopaActivity)
            adapter = ropaAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBackRopa.setOnClickListener {
            finish()
        }

        binding.fabNuevaPrenda.setOnClickListener {
            startActivity(Intent(this, RegistrarRopaActivity::class.java))
        }

        // HU-07 CA3: Búsqueda en tiempo real por modelo, marca o color
        binding.etBuscarRopa.doAfterTextChanged { texto ->
            cargarListaPrendas(texto?.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        cargarListaPrendas(binding.etBuscarRopa.text?.toString())
    }

    private fun cargarListaPrendas(filtro: String? = null) {
        val prendas = ropaDao.listar(filtro)
        ropaAdapter.actualizarLista(prendas)

        if (prendas.isEmpty()) {
            binding.tvEmptyRopa.visibility = View.VISIBLE
            binding.rvRopa.visibility = View.GONE
        } else {
            binding.tvEmptyRopa.visibility = View.GONE
            binding.rvRopa.visibility = View.VISIBLE
        }
    }
}
