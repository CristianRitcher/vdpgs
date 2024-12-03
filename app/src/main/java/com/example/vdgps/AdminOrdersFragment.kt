package com.example.vdgps

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
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
        val spinnerEstado = dialogView.findViewById<Spinner>(R.id.spinnerEstado)
        val spinnerCorreo = dialogView.findViewById<Spinner>(R.id.spinnerCorreo)

        // Configurar Spinner de estado
        val estados = listOf("En curso", "Terminado", "Cancelado")
        val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados)
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = estadoAdapter

        // Precargar datos si se está editando
        order?.let {
            tituloEditText.setText(it.titulo)
            descripcionEditText.setText(it.descripcion)
            spinnerEstado.setSelection(estados.indexOf(it.estado))
        }

        // Cargar correos de usuarios desde Firestore
        db.collection("usuarios").get()
            .addOnSuccessListener { snapshot ->
                val correos = snapshot.documents.mapNotNull { it.getString("correo") }
                val correoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, correos)
                correoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCorreo.adapter = correoAdapter

                // Precargar correo seleccionado si es edición
                order?.let {
                    val selectedIndex = correos.indexOf(it.usuarioId)
                    if (selectedIndex >= 0) spinnerCorreo.setSelection(selectedIndex)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminOrdersFragment", "Error al cargar correos: ${e.message}", e)
            }

        AlertDialog.Builder(requireContext())
            .setTitle(if (order == null) "Agregar Orden" else "Editar Orden")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = tituloEditText.text.toString()
                val descripcion = descripcionEditText.text.toString()
                val estadoSeleccionado = spinnerEstado.selectedItem.toString()
                val correoSeleccionado = spinnerCorreo.selectedItem.toString()

                if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                    val newOrder = Orden(
                        id = order?.id ?: UUID.randomUUID().toString(),
                        titulo = titulo,
                        descripcion = descripcion,
                        inicio = order?.inicio ?: Timestamp.now(),
                        final = order?.final ?: Timestamp.now(),
                        estado = estadoSeleccionado,
                        usuarioId = correoSeleccionado
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
                    Toast.makeText(requireContext(), "Orden agregada exitosamente", Toast.LENGTH_SHORT).show()
                    loadOrders()
                }
                .addOnFailureListener { e ->
                    Log.e("AdminOrdersFragment", "Error al agregar orden: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error al guardar la orden", Toast.LENGTH_SHORT).show()

                }
        } else {
            // Actualizar orden existente
            db.collection("ordenes").document(order.id).set(orderData)
                .addOnSuccessListener {
                    Log.d("AdminOrdersFragment", "Orden actualizada con ID: ${order.id}")
                    Toast.makeText(requireContext(), "Orden actualizada exitosamente", Toast.LENGTH_SHORT).show()

                    loadOrders()
                }
                .addOnFailureListener { e ->
                    Log.e("AdminOrdersFragment", "Error al actualizar orden: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error al actualizar la orden", Toast.LENGTH_SHORT).show()
                }
        }
    }

}