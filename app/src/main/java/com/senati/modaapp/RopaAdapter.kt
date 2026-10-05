package com.senati.modaapp

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.modaapp.data.model.Ropa
import com.senati.modaapp.databinding.ItemRopaBinding
import java.io.File
import java.util.Locale

class RopaAdapter(
    private var listaRopa: List<Ropa> = emptyList(),
    private val onItemClick: ((Ropa) -> Unit)? = null
) : RecyclerView.Adapter<RopaAdapter.RopaViewHolder>() {

    fun actualizarLista(nuevaLista: List<Ropa>) {
        listaRopa = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RopaViewHolder {
        val binding = ItemRopaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RopaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RopaViewHolder, position: Int) {
        holder.bind(listaRopa[position])
    }

    override fun getItemCount(): Int = listaRopa.size

    inner class RopaViewHolder(private val binding: ItemRopaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ropa: Ropa) {
            binding.tvItemModelo.text = ropa.modelo
            binding.tvItemDetalle.text = "${ropa.nombreCategoria} · Talla ${ropa.talla} · ${ropa.color}"
            binding.tvItemPrecio.text = String.format(Locale.getDefault(), "S/ %.2f", ropa.precio)
            binding.tvItemCantidad.text = binding.root.context.getString(R.string.stock_format, ropa.cantidad)

            // Cargar miniatura de foto
            var fotoCargada = false
            if (!ropa.foto.isNullOrEmpty()) {
                val file = File(ropa.foto)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        binding.ivItemFoto.setImageBitmap(bitmap)
                        binding.ivItemFoto.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        fotoCargada = true
                    }
                }
            }

            if (!fotoCargada) {
                // Placeholder temático según la prenda
                binding.ivItemFoto.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                when (ropa.idCategoria) {
                    2 -> binding.ivItemFoto.setImageResource(R.drawable.ic_pants)
                    3 -> binding.ivItemFoto.setImageResource(R.drawable.ic_dress)
                    else -> binding.ivItemFoto.setImageResource(R.drawable.ic_tshirt)
                }
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(ropa)
            }
        }
    }
}
