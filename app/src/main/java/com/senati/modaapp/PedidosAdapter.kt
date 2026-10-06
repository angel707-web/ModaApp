package com.senati.modaapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.senati.modaapp.data.model.Pedido
import com.senati.modaapp.databinding.ItemPedidoBinding

class PedidosAdapter(
    private var listaPedidos: List<Pedido>,
    private val onPedidoSelected: (Pedido) -> Unit,
    private val onPedidoClickDetalle: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    private var selectedPosition = 0

    fun actualizarLista(nuevaLista: List<Pedido>) {
        listaPedidos = nuevaLista
        selectedPosition = 0
        notifyDataSetChanged()
        if (nuevaLista.isNotEmpty()) {
            onPedidoSelected(nuevaLista[0])
        }
    }

    fun obtenerPedidoSeleccionado(): Pedido? {
        return if (listaPedidos.isNotEmpty() && selectedPosition in listaPedidos.indices) {
            listaPedidos[selectedPosition]
        } else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        holder.bind(listaPedidos[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = listaPedidos.size

    inner class PedidoViewHolder(private val binding: ItemPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: Pedido, isSelected: Boolean) {
            val context = binding.root.context

            // Título: #58 · Lucía Torres
            val nombreCorto = if (pedido.apellidosCliente.isNotBlank()) {
                val primerApellido = pedido.apellidosCliente.split(" ").firstOrNull() ?: ""
                "${pedido.nombresCliente} $primerApellido".trim()
            } else {
                pedido.nombresCliente
            }
            binding.tvPedidoCliente.text = context.getString(R.string.pedido_titulo_item_format, pedido.id, nombreCorto)

            // Chip de estado
            if (pedido.estado.equals("PENDIENTE", ignoreCase = true)) {
                binding.tvPedidoEstadoChip.text = context.getString(R.string.chip_pendiente)
                binding.tvPedidoEstadoChip.setTextColor(ContextCompat.getColor(context, R.color.primary))
                binding.tvPedidoEstadoChip.setBackgroundResource(R.drawable.bg_chip_categoria)
            } else {
                binding.tvPedidoEstadoChip.text = context.getString(R.string.chip_atendido)
                binding.tvPedidoEstadoChip.setTextColor(Color.parseColor("#2E7D32"))
                binding.tvPedidoEstadoChip.setBackgroundColor(Color.parseColor("#E8F5E9"))
            }

            // Resumen: fecha formateada · X prendas · S/ Total
            val prendasTexto = if (pedido.cantidadPrendas == 1) {
                context.getString(R.string.pedido_una_prenda)
            } else {
                context.getString(R.string.pedido_multiples_prendas_format, pedido.cantidadPrendas)
            }

            // Extraer fecha y hora corta ej. 04/10 10:15
            val fechaCorta = try {
                if (pedido.fecha.length >= 16) {
                    val parts = pedido.fecha.split(" ")
                    val dParts = parts[0].split("-")
                    val mes = if (dParts.size >= 2) dParts[1] else ""
                    val dia = if (dParts.size >= 3) dParts[2] else ""
                    val hora = if (parts.size >= 2) parts[1].substring(0, 5) else ""
                    "$dia/$mes $hora"
                } else pedido.fecha
            } catch (e: Exception) {
                pedido.fecha
            }

            binding.tvPedidoResumen.text = context.getString(
                R.string.pedido_resumen_item_format,
                fechaCorta,
                prendasTexto,
                pedido.total
            )

            // Resaltar borde si está seleccionado
            if (isSelected) {
                binding.cardPedidoItem.strokeColor = ContextCompat.getColor(context, R.color.primary)
                binding.cardPedidoItem.strokeWidth = 4
            } else {
                binding.cardPedidoItem.strokeColor = ContextCompat.getColor(context, R.color.card_border)
                binding.cardPedidoItem.strokeWidth = 2
            }

            binding.cardPedidoItem.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onPedidoSelected(pedido)
            }

            binding.cardPedidoItem.setOnLongClickListener {
                onPedidoClickDetalle(pedido)
                true
            }
        }
    }
}
