package com.example.vdgps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
class OrdersAdapter(
    private val ordersList: List<Orden>,
    private val onEditClick: (Orden) -> Unit // Callback para manejar clics en el botón de editar
) : RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder>() {

    // ViewHolder para cada ítem de la lista
    class OrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tituloTextView: TextView = itemView.findViewById(R.id.tvOrderTitulo)
        val descripcionTextView: TextView = itemView.findViewById(R.id.tvOrderDescripcion)
        val editButton: ImageButton = itemView.findViewById(R.id.btnEditOrder)
    }

    // Crear el ViewHolder para cada ítem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrdersViewHolder(view)
    }

    // Configurar los datos para cada ítem del RecyclerView
    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = ordersList[position];

        // Mostrar información de depuración en Logcat
        Log.d("OrdersAdapter", "Order at position $position: ${order.titulo}, ${order.descripcion}");

        // Configura los datos del elemento
        holder.tituloTextView.text = order.titulo;
        holder.descripcionTextView.text = order.descripcion;

        // Configura el clic del botón de edición
        holder.editButton.setOnClickListener { onEditClick(order) };
    }

    // Devolver el número de ítems en la lista
    override fun getItemCount(): Int = ordersList.size;
}