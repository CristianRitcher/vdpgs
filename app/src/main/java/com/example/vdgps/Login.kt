package com.example.vdgps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonReg: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonReg = findViewById(R.id.btn_login)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        buttonReg.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa corréo electrónico y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Autenticación exitosa
                        val user = auth.currentUser
                        user?.let {
                            val userEmail = it.email
                            // Consulta el usuario por correo electrónico
                            firestore.collection("usuarios")
                                .whereEqualTo("correo", userEmail)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        // Se encontró el usuario
                                        for (document in documents) {
                                            val rol = document.getLong("rol")
                                            when (rol) {
                                                0L -> { // Trabajador
                                                    val intent = Intent(this, WorkerActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                1L -> { // Admin
                                                    val intent = Intent(this, AdminActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                else -> {
                                                    Toast.makeText(this, "Rol desconocido", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    } else {
                                        // Usuario no encontrado
                                        Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Login", "Error al buscar usuario en Firestore", e)
                                    Toast.makeText(this, "Error al verificar el rol", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Si la autenticación falla
                        Log.w("Login", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Autenticación fallida.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
