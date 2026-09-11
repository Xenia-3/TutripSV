package com.tutripsv.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.tutripsv.app.R
import com.tutripsv.app.databinding.ActivityRegisterBinding
import com.tutripsv.app.utils.SesionManager
import com.tutripsv.app.utils.Validaciones

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sesion: SesionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sesion = SesionManager(this)

        binding.btnRegistrar.setOnClickListener { validarYRegistrar() }
        binding.tvIrLogin.setOnClickListener { finish() }
    }

    private fun validarYRegistrar() {
        val nombre = binding.etNombre.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmar = binding.etConfirmar.text.toString().trim()

        binding.tilNombre.error = null
        binding.tilCorreo.error = null
        binding.tilPassword.error = null
        binding.tilConfirmar.error = null

        if (Validaciones.esCampoVacio(nombre)) {
            binding.tilNombre.error = getString(R.string.error_nombre_vacio)
            return
        }
        if (Validaciones.esCampoVacio(correo)) {
            binding.tilCorreo.error = getString(R.string.error_correo_vacio)
            return
        }
        if (!Validaciones.esCorreoValido(correo)) {
            binding.tilCorreo.error = getString(R.string.error_correo_invalido)
            return
        }
        if (Validaciones.esCampoVacio(password)) {
            binding.tilPassword.error = getString(R.string.error_password_vacio)
            return
        }
        if (!Validaciones.esPasswordValida(password)) {
            binding.tilPassword.error = getString(R.string.error_password_corta)
            return
        }
        if (password != confirmar) {
            binding.tilConfirmar.error = getString(R.string.error_passwords_no_coinciden)
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegistrar.isEnabled = false

        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val perfil = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    user?.updateProfile(perfil)?.addOnCompleteListener {
                        sesion.guardarSesion(user?.uid ?: "", correo, nombre)
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegistrar.isEnabled = true
                        Toast.makeText(
                            this,
                            getString(R.string.exito_registro),
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegistrar.isEnabled = true
                    binding.tilCorreo.error = getString(R.string.error_registro)
                }
            }
    }
}