package com.senati.modaapp

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.senati.modaapp.data.model.Ropa
import com.senati.modaapp.databinding.ItemCatalogoBinding
import java.io.File
import java.util.Locale

class CatalogoAdapter(
    private var listaRopa: List<Ropa> = emptyList(),
    private val onAgregarCarrito: ((Ropa) -> Unit)? = null
) : RecyclerView.Adapter<CatalogoAdapter.CatalogoViewHolder>() {

    fun actualizarLista(nuevaLista: List<Ropa>) {
        listaRopa = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogoViewHolder {
        val binding = ItemCatalogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatalogoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatalogoViewHolder, position: Int) {
        holder.bind(listaRopa[position])
    }

    override fun getItemCount(): Int = listaRopa.size

    inner class CatalogoViewHolder(private val binding: ItemCatalogoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ropa: Ropa) {
            binding.tvCatalogoModelo.text = ropa.modelo
            binding.tvCatalogoDetalle.text = "Talla ${ropa.talla} · ${ropa.color}"
            binding.tvCatalogoPrecio.text = String.format(Locale.getDefault(), "S/ %.2f", ropa.precio)

            // Cargar imagen de la prenda
            var fotoCargada = false
            if (!ropa.foto.isNullOrEmpty()) {
                val file = File(ropa.foto)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        binding.ivCatalogoFoto.setImageBitmap(bitmap)
                        binding.ivCatalogoFoto.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        fotoCargada = true
                    }
                }
            }

            if (!fotoCargada) {
                binding.ivCatalogoFoto.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                when (ropa.idCategoria) {
                    2 -> binding.ivCatalogoFoto.setImageResource(R.drawable.ic_pants)
                    3 -> binding.ivCatalogoFoto.setImageResource(R.drawable.ic_dress)
                    else -> binding.ivCatalogoFoto.setImageResource(R.drawable.ic_tshirt)
                }
            }

            binding.btnAgregarAlCarrito.setOnClickListener {
                if (onAgregarCarrito != null) {
                    onAgregarCarrito.invoke(ropa)
                } else {
                    Toast.makeText(binding.root.context, "Agregado al carrito: ${ropa.modelo}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
