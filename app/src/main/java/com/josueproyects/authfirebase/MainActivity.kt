package com.josueproyects.authfirebase

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.josueproyects.authfirebase.databinding.ActivityMainBinding
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var user: FirebaseUser? = null
    private lateinit var profileUpdates: UserProfileChangeRequest
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == RESULT_OK){ //ver si existe un usuario Autenticado
            /*val user = FirebaseAuth.getInstance().currentUser*/ //variable para el user Auth
            if (user != null){
                verificarEmail()
                Toast.makeText(this,"Bienvenido.",Toast.LENGTH_SHORT).show()
                showData()
            }
        }else{
            if (response == null) { //el usuario al pulsado hacia atras
                Toast.makeText(this,"Hasta pronto.",Toast.LENGTH_SHORT).show()
                finish() //cierra la app
            }else{
                response.error?.let { //si un error no es nulo hara...
                    if (it.errorCode == ErrorCodes.NO_NETWORK) {//no hay red
                        Toast.makeText(this,"Sin red.",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Codigo del error: ${it.errorCode}",
                            Toast.LENGTH_SHORT).show()
                        showAlert()
                        onBackPressed()
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater ) //inicializar binding
        setContentView(binding.root)
        user = FirebaseAuth.getInstance().currentUser

        confAuth()
        listener()
    }

    private fun confAuth() {
        title = getString(R.string.title_Auth)

         user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            /*val email = FirebaseAuth.getInstance().currentUser?.email.toString()
            val provider = ProviderType.BASIC
            showHome(email,provider)*/
                try {
                    showData()
                }catch (e: FirebaseAuthRecentLoginRequiredException){
                    AuthUI.getInstance().signOut(this)
                }
        } else {
            /*binding.btnSignUp.setOnClickListener {
                with(binding){
                    if (etEmail.text!!.isNotEmpty() && etPassword.text!!.isNotEmpty()) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                            etEmail.text.toString(),
                            etPassword.text.toString()
                        )
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    showHome(it.result?.user?.email!!, ProviderType.BASIC)
                                } else {
                                    showAlert()
                                }
                            }
                    }
                }
            }

            binding.btnLogIn.setOnClickListener {
                with(binding){
                    if (etEmail.text!!.isNotEmpty() && etPassword.text!!.isNotEmpty()) {
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                            etEmail.text.toString(),
                            etPassword.text.toString()
                        )
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    showHome(it.result?.user?.email!!, ProviderType.BASIC)
                                } else {
                                    showAlert()
                                }
                            }
                    }
                }
            }*/

            val providers = arrayListOf(//son los proveedores
                AuthUI.IdpConfig.EmailBuilder().build(), //email
                AuthUI.IdpConfig.GoogleBuilder().build(),//google
                AuthUI.IdpConfig.AnonymousBuilder().build(),//anonimo
                /*AuthUI.IdpConfig.PhoneBuilder().build()*/) //numero de telefono

            resultLauncher.launch( //hace la instancia donde se agregan a los proveedores
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)//automaticamente te envia a la seccion de login si esta en true
                    .build())

        }
    }

    private fun verificarEmail(){
        user?.sendEmailVerification()!!
            .addOnSuccessListener {  }
            .addOnFailureListener {  }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_sign_out -> {//cerrar la sesión
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this,"Sesión terminada.",Toast.LENGTH_SHORT).show()
                        confAuth()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){//si es exitoso la vista se quitara
                        }else{
                            Toast.makeText(this,"No se pudo cerrar la sesión.",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            R.id.action_delete_account ->{
                deleteAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.title_error))
        builder.setMessage(getString(R.string.message_error))
        builder.setPositiveButton(getString(R.string.accept),null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /*private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra(getString(R.string.key_email), email)
            putExtra(getString(R.string.key_provider), provider.name)
        }
        startActivity(homeIntent)
    }*/

    private fun showData(){


        with(binding){
            etEmail.setText(user!!.email.toString())
            etName.setText(user!!.displayName)
            etId.setText(user!!.uid)
            etUrlPhoto.setText(user!!.photoUrl.toString())
            if (user!!.photoUrl == null) etUrlPhoto.setText("")
            //etPhone.setText(user.currentUser!!.phoneNumber.toString())
            cbEmailVerified.isChecked = user!!.isEmailVerified
            containerMain.visibility = View.VISIBLE
            loadImage(user!!.photoUrl.toString())
        }
    }

    private fun listener(){
        with(binding) {
            btnUpdate.setOnClickListener {
                if (validateFields(etName,etUrlPhoto,/*etEmail*/)){
                    /*var profileCount = 0
                    var passCount = 0*/
                    //actualizar nombre y foto
                        profileUpdates = userProfileChangeRequest {
                            displayName = etName.text.toString()
                            photoUri = Uri.parse(etUrlPhoto.text.toString())
                            //user.currentUser?.phoneNumber = binding.etPhone.toString()
                        }


                    try {
                        user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    /*profileCount = 1
                                    Toast.makeText(this@MainActivity,profileCount,Toast.LENGTH_SHORT).show()*/
                                    Toast.makeText(this@MainActivity,
                                        getString(R.string.edit_user_message_success),Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(this@MainActivity,
                                        getString(R.string.edit_user_message_error),Toast.LENGTH_LONG).
                                    show()
                                }
                            }
                    }catch (e: NullPointerException) {
                        Toast.makeText(this@MainActivity,
                            getString(R.string.edit_user_message_error),Toast.LENGTH_LONG).
                        show()
                    }
                    /*//actualizar email
                    user.currentUser!!.updateEmail(binding.etEmail.text.toString())
                        .addOnCompleteListener { task ->
                            if (successTask(task.isSuccessful)){

                            }
                        }*/

                    /*//actualizar contraseña
                    if(etPassword.text!!.isNotEmpty()){
                        user.currentUser!!.updatePassword(etPassword.text.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    passCount = 2
                                }
                            }
                    }else passCount = 2
                    when (profileCount + passCount) {
                        3 -> {
                            snackbar("si")
                            //snackbar("${R.string.edit_user_message_success}")
                        }
                        1 -> {
                            snackbar("no 1")
                            //snackbar("${R.string.edit_user_message_error_profile}")
                        }
                        2 -> {
                            snackbar("no 2")
                            //snackbar("${R.string.edit_user_message_error_pass}")
                        }
                    }*/

                }else{
                    Snackbar.make(binding.root,
                        R.string.edit_user_message_valid,
                        Snackbar.LENGTH_SHORT).show()
                }
            }
            etUrlPhoto.addTextChangedListener {
                validateFields(etUrlPhoto)
                loadImage(etUrlPhoto.text.toString())
            }
            etName.addTextChangedListener { validateFields(etName) }
            /*etEmail.addTextChangedListener { validateFields(etEmail) }*/
        }
    }

    private fun deleteAccount(){
        try {
            user!!.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        confAuth()
                    }else Toast.makeText(this,
                        getString(R.string.user_message_error_delete_account),Toast.LENGTH_LONG)
                        .show()
                }
        }catch (e: NullPointerException){
            Toast.makeText(this,
                getString(R.string.user_message_error_delete_account),Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun loadImage(url: String){
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(binding.imgPhoto)
    }

    private fun validateFields(vararg textFields: TextInputEditText): Boolean{
        var isValid = true

        for (textField in textFields){
            if (textField.text.toString().trim().isEmpty()){
                textField.error = getString(R.string.helper_required)
                textField.requestFocus()
                isValid = false
            }else textField.error = null
        }
        return isValid
    }


}