package com.example.vdgps

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class AdminOrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ordersAdapter: OrdersAdapter
    private lateinit var fabAddOrder: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()
    private val ordersList = mutableListOf<Orden>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_orders, container, false)

        // Vincular vistas
        recyclerView = view.findViewById(R.id.recyclerViewOrders)
        fabAddOrder = view.findViewById(R.id.fabAddOrder)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = OrdersAdapter(ordersList) { order ->
            showOrderDialog(order) // Llama al diálogo con los datos de la orden seleccionada
        }
        recyclerView.adapter = ordersAdapter

        // Configurar FAB
        fabAddOrder.setOnClickListener { showOrderDialog(null) }

        // Cargar órdenes
        loadOrders()

        return view
    }

    private fun loadOrders() {
        db.collection("ordenes").get()
            .addOnSuccessListener { snapshot ->
                ordersList.clear() // Limpiar la lista antes de agregar los nuevos datos
                Log.d("AdminOrdersFragment", "Snapshot de Firestore: ${snapshot.documents.size} documentos encontrados.")

                if (snapshot.isEmpty) {
                    Log.d("Firestore", "No hay órdenes disponibles.")
                } else {
                    for (document in snapshot.documents) {
                        val order = document.toObject(Orden::class.java)?.apply {
                            id = document.id  // Asignamos el ID del documento a la orden
                        }

                        if (order != null) {
                            ordersList.add(order) // Agregamos la orden a la lista
                        } else {
                            Log.e("AdminOrdersFragment", "No se pudo mapear la orden: ${document.id}")
                        }
                    }
                    Log.d("AdminOrdersFragment", "Órdenes cargadas: $ordersList") // Verificar los datos cargados
                    ordersAdapter.notifyDataSetChanged() // Notificar al adaptador que se actualizó la lista
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al cargar órdenes: ${e.message}", e)
            }
    }


    private fun showOrderDialog(order: Orden?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_order_form, null)
        val tituloEditText = dialogView.findViewById<EditText>(R.id.etTitulo)
        val descripcionEditText = dialogView.findViewById<EditText>(R.id.etDescripcion)

        // Precargar datos si se está editando
        order?.let {
            tituloEditText.setText(it.titulo)
            descripcionEditText.setText(it.descripcion)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (order == null) "Agregar Orden" else "Editar Orden")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = tituloEditText.text.toString()
                val descripcion = descripcionEditText.text.toString()

                if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                    val newOrder = Orden(
                        id = order?.id ?: UUID.randomUUID().toString(),
                        titulo = titulo,
                        descripcion = descripcion,
                        inicio = order?.inicio ?: Timestamp.now(),
                        final = order?.final ?: Timestamp.now(),
                        estado = order?.estado ?: "pendiente",
                        usuarioId = order?.usuarioId ?: "user_123"
                    )
                    saveOrder(newOrder)
                } else {
                    Toast.makeText(requireContext(), "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveOrder(order: Orden) {
        val orderData = hashMapOf(
            "titulo" to order.titulo,
            "descripcion" to order.descripcion,
            "inicio" to order.inicio,
            "final" to order.final,
            "estado" to order.estado,
            "usuarioId" to order.usuarioId
        )

        if (order.id.isEmpty()) {
            // Agregar nueva orden
            db.collection("ordenes").add(orderData)
                .addOnSuccessListener { documentReference ->
                    Log.d("AdminOrdersFragment", "Orden agregada con ID: ${documentReference.id}")
                    loadOrders()
                }
                .addOnFailureListener { e ->
                    Log.e("AdminOrdersFragment", "Error al agregar orden: ${e.message}", e)
                }
        } else {
            // Actualizar orden existente
            db.collection("ordenes").document(order.id).set(orderData)
                .addOnSuccessListener {
                    Log.d("AdminOrdersFragment", "Orden actualizada con ID: ${order.id}")
                    loadOrders()
                }
                .addOnFailureListener { e ->
                    Log.e("AdminOrdersFragment", "Error al actualizar orden: ${e.message}", e)
                }
        }
    }
}