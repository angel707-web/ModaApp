package com.senati.modaapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.senati.modaapp.data.model.StockPrenda
import com.senati.modaapp.databinding.ItemStockReporteBinding
import kotlin.math.max

class StockReporteAdapter(
    private var listaStock: List<StockPrenda>
) : RecyclerView.Adapter<StockReporteAdapter.StockViewHolder>() {

    private var maxCantidad = 30

    fun actualizarLista(nuevaLista: List<StockPrenda>) {
        listaStock = nuevaLista
        maxCantidad = max(30, nuevaLista.maxOfOrNull { it.cantidad } ?: 30)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val binding = ItemStockReporteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(listaStock[position])
    }

    override fun getItemCount(): Int = listaStock.size

    inner class StockViewHolder(private val binding: ItemStockReporteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StockPrenda) {
            val context = binding.root.context
            binding.tvStockModelo.text = item.modelo
            binding.tvStockCantidad.text = item.cantidad.toString()

            binding.pbStockBar.max = maxCantidad
            binding.pbStockBar.progress = item.cantidad

            // HU-12 CA2: Las de 3 o menos aparecen resaltadas (en rojo)
            if (item.cantidad <= 3) {
                binding.pbStockBar.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_stock_alerta)
                binding.tvStockCantidad.setTextColor(Color.parseColor("#D32F2F"))
            } else {
                binding.pbStockBar.progressDrawable = ContextCompat.getDrawable(context, R.drawable.bg_progress_stock_normal)
                binding.tvStockCantidad.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
        }
    }
}
