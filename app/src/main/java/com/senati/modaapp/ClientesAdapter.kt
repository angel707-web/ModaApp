package com.senati.modaapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.modaapp.data.model.ClienteConPedidos
import com.senati.modaapp.databinding.ItemClienteBinding

class ClientesAdapter(
    private var listaClientes: List<ClienteConPedidos>,
    private val onClienteClick: (ClienteConPedidos) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    fun actualizarLista(nuevaLista: List<ClienteConPedidos>) {
        listaClientes = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(listaClientes[position])
    }

    override fun getItemCount(): Int = listaClientes.size

    inner class ClienteViewHolder(private val binding: ItemClienteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClienteConPedidos) {
            val context = binding.root.context
            val nombreCompleto = "${item.nombres} ${item.apellidos}".trim()
            binding.tvClienteNombreCompleto.text = nombreCompleto

            // Formatear teléfono ej. 987 654 321
            val telFormateado = if (item.telefono.length == 9) {
                "${item.telefono.substring(0, 3)} ${item.telefono.substring(3, 6)} ${item.telefono.substring(6)}"
            } else {
                item.telefono
            }
            binding.tvClienteTelefono.text = telFormateado

            // Conteo de pedidos
            val pedidosTexto = if (item.numPedidos == 1) {
                context.getString(R.string.cliente_conteo_pedidos_singular)
            } else {
                context.getString(R.string.cliente_conteo_pedidos_plural, item.numPedidos)
            }
            binding.tvClienteNumPedidosChip.text = pedidosTexto

            binding.cardClienteItem.setOnClickListener {
                onClienteClick(item)
            }
        }
    }
}
