package com.example.aiish

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecureTokenManager(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore")
    private val keyAlias = "your_key_alias"
    private val sharedPreferences: SharedPreferences

    init {
        keyStore.load(null)
        sharedPreferences = context.getSharedPreferences("SecurePrefs", Context.MODE_PRIVATE)
    }

    @SuppressLint("SuspiciousIndentation")
    fun saveToken(token: String) {
        Globals.token=token;
        println("xsssssssssssssssssssssssssssxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        println(token)
       // val cipher = getCipher()
//        val encryptedToken = cipher?.doFinal(token.toByteArray())
        val tokenWithoutBearer = token.replace("Bearer ", "")

//        if (encryptedToken != null) {
      //      val encodedToken = Base64.encodeToString(encryptedToken, Base64.DEFAULT)

            sharedPreferences.edit().putString("secure_token", tokenWithoutBearer).apply()
     //   }
    }

    fun loadToken(): String? {

        val encodedToken = sharedPreferences.getString("secure_token", null)
        return encodedToken;

//        if (encodedToken != null) {
//            println("yeeeeeeeeeeeeeeeeeeeeeeeessssssssssssssssdcdc")
//            val cipher = getCipher()
//            val encryptedToken = Base64.decode(encodedToken, Base64.DEFAULT)
//            val decryptedToken = cipher?.doFinal(encryptedToken)
//
//            return decryptedToken?.toString(Charsets.UTF_8)
//        }

        return null
    }

    @SuppressLint("SuspiciousIndentation")
    fun saveId(id: String) {
        Globals.deviceId=id;
        println("ID Saved: $id")
        // Additional logic for encryption if needed
        sharedPreferences.edit().putString("secure_id", id).apply()
    }

    fun loadId(): String? {
        val storedId = sharedPreferences.getString("secure_id", null)
        println("Loaded ID: $storedId")
        // Additional logic for decryption if needed
        return storedId
    }

    @SuppressLint("SuspiciousIndentation")
    fun saveEmail(email: String) {
        Globals.email=email;
        println("Email Saved: $email")
        // Additional logic for encryption if needed
        sharedPreferences.edit().putString("secure_email", email).apply()
    }

    fun loadEmail(): String? {
        val storedEmail = sharedPreferences.getString("secure_email", null)
        println("Loaded Email: $storedEmail")
        // Additional logic for decryption if needed
        return storedEmail
    }


    private fun getCipher(): Cipher? {
        try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val key = keyGenerator.generateKey()

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)

            return cipher
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}
