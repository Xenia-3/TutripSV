package com.tutripsv.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tutripsv.app.databinding.ActivityLoginBinding
import com.tutripsv.app.utils.SesionManager
import com.tutripsv.app.utils.Validaciones

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sesion: SesionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sesion = SesionManager(this)

        if (auth.currentUser != null) {
            irAlCatalogo()
            return
        }

        binding.btnLogin.setOnClickListener { validarYLogin() }
        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validarYLogin() {
        val correo = binding.etCorreo.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilCorreo.error = null
        binding.tilPassword.error = null

        if (Validaciones.esCampoVacio(correo)) {
            binding.tilCorreo.error = getString(com.tutripsv.app.R.string.error_correo_vacio)
            return
        }
        if (!Validaciones.esCorreoValido(correo)) {
            binding.tilCorreo.error = getString(com.tutripsv.app.R.string.error_correo_invalido)
            return
        }
        if (Validaciones.esCampoVacio(password)) {
            binding.tilPassword.error = getString(com.tutripsv.app.R.string.error_password_vacio)
            return
        }
        if (!Validaciones.esPasswordValida(password)) {
            binding.tilPassword.error = getString(com.tutripsv.app.R.string.error_password_corta)
            return
        }

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    sesion.guardarSesion(
                        user?.uid ?: "",
                        user?.email ?: correo,
                        user?.displayName ?: ""
                    )
                    Toast.makeText(
                        this,
                        getString(com.tutripsv.app.R.string.exito_login),
                        Toast.LENGTH_SHORT
                    ).show()
                    irAlCatalogo()
                } else {
                    binding.tilPassword.error =
                        getString(com.tutripsv.app.R.string.error_credenciales)
                }
            }
    }

    private fun irAlCatalogo() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}