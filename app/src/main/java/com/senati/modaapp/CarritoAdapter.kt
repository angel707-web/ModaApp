package com.senati.modaapp

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.modaapp.data.model.ItemCarrito
import com.senati.modaapp.databinding.ItemCarritoBinding
import java.io.File
import java.util.Locale

class CarritoAdapter(
    private var items: List<ItemCarrito> = emptyList(),
    private val onItemLongClick: (ItemCarrito) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    fun actualizarLista(nuevosItems: List<ItemCarrito>) {
        items = nuevosItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CarritoViewHolder(private val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemCarrito) {
            val ropa = item.ropa
            binding.tvItemCarritoTitulo.text = "${ropa.modelo} · ${ropa.talla}"
            binding.tvItemCarritoDetalle.text = String.format(
                Locale.getDefault(),
                "%s · %d x S/ %.2f",
                ropa.color,
                item.cantidad,
                ropa.precio
            )
            binding.tvItemCarritoSubtotal.text = String.format(Locale.getDefault(), "S/ %.2f", item.subtotal)

            // Cargar imagen de la prenda
            var fotoCargada = false
            if (!ropa.foto.isNullOrEmpty()) {
                val file = File(ropa.foto)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        binding.ivItemCarritoFoto.setImageBitmap(bitmap)
                        binding.ivItemCarritoFoto.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        fotoCargada = true
                    }
                }
            }

            if (!fotoCargada) {
                binding.ivItemCarritoFoto.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                when (ropa.idCategoria) {
                    2 -> binding.ivItemCarritoFoto.setImageResource(R.drawable.ic_pants)
                    3 -> binding.ivItemCarritoFoto.setImageResource(R.drawable.ic_dress)
                    else -> binding.ivItemCarritoFoto.setImageResource(R.drawable.ic_tshirt)
                }
            }

            // HU-08 CA3: Mantener presionado para quitar
            binding.root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }
}
