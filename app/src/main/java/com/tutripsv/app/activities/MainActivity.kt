package com.tutripsv.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tutripsv.app.R
import com.tutripsv.app.adapters.DestinoAdapter
import com.tutripsv.app.databinding.ActivityMainBinding
import com.tutripsv.app.models.Destino
import com.tutripsv.app.utils.SesionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sesion: SesionManager
    private lateinit var adapter: DestinoAdapter
    private val listaDestinos = mutableListOf<Destino>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sesion = SesionManager(this)

        if (auth.currentUser == null) {
            irAlLogin()
            return
        }

        configurarToolbar()
        configurarRecycler()
        configurarFab()
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            cargarDestinos()
        }
    }

    private fun configurarToolbar() {
        binding.toolbar.title = getString(R.string.catalogo_titulo)
        binding.toolbar.inflateMenu(R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_cerrar_sesion -> {
                    cerrarSesion()
                    true
                }
                else -> false
            }
        }
    }

    private fun configurarRecycler() {
        adapter = DestinoAdapter(
            listaDestinos,
            onEditar = { destino -> abrirEditar(destino) },
            onEliminar = { destino -> confirmarEliminar(destino) }
        )
        binding.rvDestinos.layoutManager = LinearLayoutManager(this)
        binding.rvDestinos.adapter = adapter
    }

    private fun configurarFab() {
        binding.fabAgregar.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
    }

    private fun cargarDestinos() {
        val usuarioId = auth.currentUser?.uid ?: return
        db.collection("destinos")
            .whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        getString(R.string.error_cargar_destinos),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }
                listaDestinos.clear()
                snapshot?.documents?.forEach { doc ->
                    val destino = doc.toObject(Destino::class.java)
                    if (destino != null) listaDestinos.add(destino)
                }
                adapter.actualizarLista(listaDestinos)
                binding.tvVacio.visibility =
                    if (listaDestinos.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun abrirEditar(destino: Destino) {
        val intent = Intent(this, AddEditActivity::class.java)
        intent.putExtra("destinoId", destino.id)
        startActivity(intent)
    }

    private fun confirmarEliminar(destino: Destino) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirmar_eliminar_titulo))
            .setMessage(getString(R.string.confirmar_eliminar_mensaje))
            .setPositiveButton(getString(R.string.si)) { _, _ ->
                eliminarDestino(destino)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun eliminarDestino(destino: Destino) {
        db.collection("destinos").document(destino.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.destino_eliminado),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.error_eliminar),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun cerrarSesion() {
        auth.signOut()
        sesion.cerrarSesion()
        Toast.makeText(
            this,
            getString(R.string.sesion_cerrada),
            Toast.LENGTH_SHORT
        ).show()
        irAlLogin()
    }

    private fun irAlLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}