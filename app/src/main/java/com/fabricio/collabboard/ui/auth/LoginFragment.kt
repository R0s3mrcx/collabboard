package com.fabricio.collabboard.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fabricio.collabboard.R
import com.fabricio.collabboard.model.User
import com.fabricio.collabboard.utils.Validators
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_login_to_feed)
            return
        }

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val tvError = view.findViewById<TextView>(R.id.tvError)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!Validators.isValidEmail(email)) {
                tvError.text = "Please enter a valid email"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (!Validators.isValidPassword(password)) {
                tvError.text = "Password must be at least 6 characters"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    findNavController().navigate(R.id.action_login_to_feed)
                }
                .addOnFailureListener {
                    tvError.text = "Login failed: ${it.message}"
                    tvError.visibility = View.VISIBLE
                }
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!Validators.isValidEmail(email)) {
                tvError.text = "Please enter a valid email"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (!Validators.isValidPassword(password)) {
                tvError.text = "Password must be at least 6 characters"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid
                    val user = User(
                        uid = uid,
                        displayName = email.substringBefore("@"),
                        email = email,
                        university = "My University",
                        skills = "Android, Kotlin"
                    )
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            findNavController().navigate(R.id.action_login_to_feed)
                        }
                }
                .addOnFailureListener {
                    tvError.text = "Register failed: ${it.message}"
                    tvError.visibility = View.VISIBLE
                }
        }
    }
}

