package com.senati.modaapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.senati.modaapp.data.dao.ReporteDao
import com.senati.modaapp.databinding.ActivityReportesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportesBinding
    private lateinit var reporteDao: ReporteDao
    private lateinit var adapter: StockReporteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reporteDao = ReporteDao(this)

        binding.btnBackReportes.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        cargarReportes()
    }

    private fun setupRecyclerView() {
        binding.rvStockReporte.layoutManager = LinearLayoutManager(this)
        adapter = StockReporteAdapter(emptyList())
        binding.rvStockReporte.adapter = adapter
    }

    private fun cargarReportes() {
        // 1. Nombre del mes actual en español
        val localeEs = Locale.forLanguageTag("es-PE")
        val formatoMes = SimpleDateFormat("MMMM", localeEs)
        val mesActual = formatoMes.format(Date()).lowercase(localeEs)
        binding.tvReportesLabelVendido.text = getString(R.string.reportes_label_vendido_mes, mesActual)

        // 2. HU-12 CA1: Monto vendido en pedidos atendidos
        val totalVendido = reporteDao.totalVendidoAtendidosMes()
        binding.tvReportesMontoVendido.text = getString(R.string.reportes_monto_format, totalVendido)

        // 3. HU-12 CA1: Número de pedidos atendidos y pendientes
        val atendidos = reporteDao.contarPedidos("ATENDIDO")
        val pendientes = reporteDao.contarPedidos("PENDIENTE")
        binding.tvReportesCountAtendidos.text = atendidos.toString()
        binding.tvReportesCountPendientes.text = pendientes.toString()

        // 4. HU-12 CA2: Stock por prenda con resaltado si stock <= 3
        val listaStock = reporteDao.stockPorPrenda()
        if (listaStock.isEmpty()) {
            binding.tvEmptyStock.visibility = View.VISIBLE
            binding.rvStockReporte.visibility = View.GONE
        } else {
            binding.tvEmptyStock.visibility = View.GONE
            binding.rvStockReporte.visibility = View.VISIBLE
            adapter.actualizarLista(listaStock)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarReportes()
    }
}
