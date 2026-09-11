package com.tutripsv.app.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tutripsv.app.R
import com.tutripsv.app.databinding.ActivityAddEditBinding
import com.tutripsv.app.models.Destino
import com.tutripsv.app.utils.Validaciones

class AddEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var destinoId: String? = null
    private var esEdicion = false
    private var imagenSeleccionada: String = ""
    private var paisSeleccionado: String = ""

    private val imagenesDisponibles = listOf(
        "playa1", "playa2", "montana1", "montana2",
        "ciudad1", "ciudad2", "catarata1", "ruinas1"
    )

    private val nombresMostrar = listOf(
        "Playa 1", "Playa 2", "Montaña 1", "Montaña 2",
        "Ciudad 1", "Ciudad 2", "Catarata 1", "Ruinas 1"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        destinoId = intent.getStringExtra("destinoId")
        esEdicion = destinoId != null

        if (esEdicion) {
            binding.toolbar.title = getString(R.string.destino_titulo_editar)
            binding.btnGuardar.text = getString(R.string.destino_actualizar)
            cargarDestino()
        } else {
            binding.toolbar.title = getString(R.string.destino_titulo_crear)
            binding.btnGuardar.text = getString(R.string.destino_guardar)
        }

        configurarSpinner()
        configurarBotones()
    }

    private fun configurarSpinner() {
        val paises = resources.getStringArray(R.array.paises)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            paises
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPais.adapter = adapter

        binding.spinnerPais.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                paisSeleccionado = paises[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarBotones() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSeleccionarImagen.setOnClickListener {
            mostrarDialogoImagenes()
        }

        binding.btnGuardar.setOnClickListener { validarYGuardar() }
    }

    private fun mostrarDialogoImagenes() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una imagen")

        val vista = layoutInflater.inflate(R.layout.dialog_imagenes, null)
        builder.setView(vista)

        val iv1 = vista.findViewById<android.widget.ImageView>(R.id.iv1)
        val iv2 = vista.findViewById<android.widget.ImageView>(R.id.iv2)
        val iv3 = vista.findViewById<android.widget.ImageView>(R.id.iv3)
        val iv4 = vista.findViewById<android.widget.ImageView>(R.id.iv4)
        val iv5 = vista.findViewById<android.widget.ImageView>(R.id.iv5)
        val iv6 = vista.findViewById<android.widget.ImageView>(R.id.iv6)

        val imgs = listOf(iv1, iv2, iv3, iv4, iv5, iv6)
        val dialog = builder.create()

        imgs.forEachIndexed { index, imageView ->
            if (index < imagenesDisponibles.size) {
                val nombreRecurso = imagenesDisponibles[index]
                val idRecurso = resources.getIdentifier(
                    nombreRecurso, "drawable", packageName
                )
                if (idRecurso != 0) {
                    Glide.with(this).load(idRecurso).into(imageView)
                    imageView.setOnClickListener {
                        imagenSeleccionada = nombreRecurso
                        Glide.with(this).load(idRecurso).into(binding.ivImagen)
                        Toast.makeText(
                            this,
                            "Imagen seleccionada: ${nombresMostrar[index]}",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }
            }
        }

        builder.setNegativeButton(getString(R.string.cancelar), null)
        dialog.show()
    }

    private fun validarYGuardar() {
        val nombre = binding.etNombre.text.toString().trim()
        val precio = binding.etPrecio.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        binding.tilNombre.error = null
        binding.tilPais.error = null
        binding.tilPrecio.error = null
        binding.tilDescripcion.error = null

        if (Validaciones.esCampoVacio(nombre)) {
            binding.tilNombre.error = getString(R.string.error_nombre_destino_vacio)
            return
        }
        if (!Validaciones.esPaisValido(paisSeleccionado)) {
            binding.tilPais.error = getString(R.string.error_pais_vacio)
            return
        }
        if (Validaciones.esCampoVacio(precio)) {
            binding.tilPrecio.error = getString(R.string.error_precio_vacio)
            return
        }
        if (precio.toDoubleOrNull() == null) {
            binding.tilPrecio.error = getString(R.string.error_precio_invalido)
            return
        }
        if (!Validaciones.esPrecioValido(precio)) {
            binding.tilPrecio.error = getString(R.string.error_precio_mayor_cero)
            return
        }
        if (Validaciones.esCampoVacio(descripcion)) {
            binding.tilDescripcion.error = getString(R.string.error_descripcion_vacia)
            return
        }
        if (!Validaciones.esDescripcionValida(descripcion)) {
            binding.tilDescripcion.error = getString(R.string.error_descripcion_corta)
            return
        }
        if (imagenSeleccionada.isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.error_imagen_vacia),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = false

        val usuarioId = auth.currentUser?.uid ?: ""
        val destino = Destino(
            id = destinoId ?: "",
            nombre = nombre,
            pais = paisSeleccionado,
            precio = precio.toDouble(),
            descripcion = descripcion,
            imagenRuta = imagenSeleccionada,
            usuarioId = usuarioId
        )

        if (esEdicion) {
            db.collection("destinos").document(destinoId!!)
                .set(destino)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        getString(R.string.destino_actualizado),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGuardar.isEnabled = true
                    Toast.makeText(
                        this,
                        getString(R.string.error_actualizar),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            db.collection("destinos")
                .add(destino)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        getString(R.string.destino_guardado),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGuardar.isEnabled = true
                    Toast.makeText(
                        this,
                        getString(R.string.error_guardar),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun cargarDestino() {
        db.collection("destinos").document(destinoId!!)
            .get()
            .addOnSuccessListener { doc ->
                val destino = doc.toObject(Destino::class.java)
                if (destino != null) {
                    binding.etNombre.setText(destino.nombre)
                    binding.etPrecio.setText(destino.precio.toString())
                    binding.etDescripcion.setText(destino.descripcion)
                    imagenSeleccionada = destino.imagenRuta
                    paisSeleccionado = destino.pais

                    val paises = resources.getStringArray(R.array.paises)
                    val index = paises.indexOf(destino.pais)
                    if (index >= 0) binding.spinnerPais.setSelection(index)

                    val idRecurso = resources.getIdentifier(
                        destino.imagenRuta, "drawable", packageName
                    )
                    if (idRecurso != 0) {
                        Glide.with(this)
                            .load(idRecurso)
                            .into(binding.ivImagen)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.error_cargar_destinos),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}