package com.example.aiish

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileHandler(private val context: Context) {

    fun createAndWriteToFile(data: String, fileName: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val dataWithTimestamp = "$timestamp: $data\n" // Add timestamp to the data

        val file = File(context.filesDir, fileName)

        // Check if the file exists
        if (file.exists()) {
            FileOutputStream(file, true).use { outputStream -> // Open file in append mode
                outputStream.write(dataWithTimestamp.toByteArray())
            }
        } else {
            file.createNewFile() // Create a new file if it doesn't exist
            FileOutputStream(file).use { outputStream ->
                outputStream.write(dataWithTimestamp.toByteArray())
            }
        }
    }



    // Example method to send the file to the server using Retrofit
     fun readFromFile(fileName: String): String {
        val file = File(context.filesDir, fileName)
        val stringBuilder = StringBuilder()
        if(file.exists())
        {
          //  file.readText()
            try {
                val inputStream = FileInputStream(file)
                inputStream.bufferedReader().useLines { lines ->
                    lines.forEach {
                        stringBuilder.append(it).append("\n")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        else{
            return "No Activity";
        }
        return stringBuilder.toString()
    }

    fun deleteFile(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            println("deleted");
            file.delete()
        } else {
            println("not deleted");
            false
        }
    }
}
