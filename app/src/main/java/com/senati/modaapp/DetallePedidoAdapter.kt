package com.senati.modaapp

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.modaapp.data.model.DetallePedido
import com.senati.modaapp.databinding.ItemDetallePedidoBinding

class DetallePedidoAdapter(
    private val items: List<DetallePedido>
) : RecyclerView.Adapter<DetallePedidoAdapter.DetalleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
        val binding = ItemDetallePedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetalleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DetalleViewHolder(private val binding: ItemDetallePedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetallePedido) {
            val context = binding.root.context
            binding.tvModeloDetalle.text = item.modeloRopa
            binding.tvVariantesDetalle.text = "Talla: ${item.tallaRopa} · Color: ${item.colorRopa}"
            binding.tvCantPrecioDetalle.text = "${item.cantidad} x S/ ${String.format("%.2f", item.precioUnit)} = S/ ${String.format("%.2f", item.subtotal)}"

            // Foto
            if (item.fotoRopa.isNotBlank()) {
                try {
                    val bytes = Base64.decode(item.fotoRopa, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        binding.ivPrendaDetalle.setImageBitmap(bitmap)
                    } else {
                        binding.ivPrendaDetalle.setImageResource(R.drawable.ic_hanger)
                    }
                } catch (e: Exception) {
                    binding.ivPrendaDetalle.setImageResource(R.drawable.ic_hanger)
                }
            } else {
                binding.ivPrendaDetalle.setImageResource(R.drawable.ic_hanger)
            }
        }
    }
}
