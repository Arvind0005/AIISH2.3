package com.example.aiish

import WebAppInterface
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
//import com.google.android.material.navigation.NavigationView
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.provider.Settings
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bugfender.sdk.Bugfender
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.aiish.ui.theme.AIISHTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.Objects
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    var language: String = "en-US"
    var sourcelanguage = "TAMIL"
    var selectedLanguage = "Choose language";
    private lateinit var webView: WebView
    private var progressDialog: ProgressDialog? = null
    var ReadyFlag = false;
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null
    private lateinit var interpretButton: Button
    private lateinit var fileName: String;
    private val maxWordCount = 50;
    private val REQUEST_CODE_SPEECH_INPUT = 1
    private var remainingWordCount = 10
    private var Transcribetext = "";
    private var currentWordCount = 0
    private var isTablet = true;
    private var deviceId = "";
    private val SMS_PERMISSION_REQUEST_CODE = 123
    private var userEmail = "";
    private lateinit var secureTokenManager: SecureTokenManager
    private lateinit var token: String;
    private lateinit var fileHandler: FileHandler

    fun getServerUrl(context: Context): String {
        // You can retrieve the URL from strings.xml or any other configuration file.
        return context.getString(R.string.server_url)
    }

    fun writeToFile(
        filename:String
    ) {
        var params: Map<String, String> = mapOf();


        params = mapOf(
            "content" to fileHandler.readFromFile(filename),
            "token" to token.toString(),
            "customer_id" to "10016",
            "device_id" to deviceId,
            "gmail_id" to userEmail
        )

        fileHandler.createAndWriteToFile("INFO " + "mainActivity " + "writeToFile", fileName);
        deviceId = secureTokenManager.loadId().toString();
        userEmail = secureTokenManager.loadEmail().toString()
        token = secureTokenManager.loadToken().toString();
        val serverUrl = getServerUrl(this)
        println(params.toString());
        fileHandler.createAndWriteToFile(
            "INFO " +
                    "write to file " + params.toString(),
            fileName
        );
        val requestUrl = "$serverUrl/write-to-file/$filename"

        mRequestQueue = Volley.newRequestQueue(this)

        mStringRequest = object : StringRequest(
            Method.POST, requestUrl.toString(),
            { response ->
                println("writeToFile response $response")
                fileHandler.createAndWriteToFile(
                    "INFO " +
                            "writeToFile response" + response.toString(),
                    fileName
                );

                val gson = Gson()
                val jsonObject: JsonObject = gson.fromJson(response, JsonObject::class.java)
                val status = jsonObject.getAsJsonPrimitive("status")?.asString;
                val message = jsonObject.getAsJsonPrimitive("message")?.asString;
                println(jsonObject)
                if (status == "<ERROR>") {
                    println("write to file error " + message)
                    fileHandler.createAndWriteToFile(
                        "CRITICAL " +
                                "write to file response Error " + response,
                        fileName
                    );
                }

            },
            { error ->
                fileHandler.createAndWriteToFile(
                    "CRITICAL " +
                            "write to file response Error " + error.printStackTrace(),
                    fileName
                );
                Log.i(ContentValues.TAG, "Error :" + error.networkResponse)
            }) {
            override fun getParams(): Map<String, String?> {
                return params
            }
        }

        mRequestQueue!!.add(mStringRequest)
    }

    var options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.KANNADA)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()
    var englishGermanTranslator = Translation.getClient(options)
    val profanityList = Globals.profanityList

    private var mRequestQueue: RequestQueue? = null
    private var mLogsRequestQueue: RequestQueue? = null
    private var mStringRequest: StringRequest? = null
    lateinit var mGoogleSignInClient: GoogleSignInClient

    val sessionMsg = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val sms = intent.extras?.getString("session_message")
            if (sms == "ready") {
                fileHandler.createAndWriteToFile("INFO "+"sessionMsg " + "ready", fileName);


                if (remainingWordCount > 0) {
                    fileHandler.createAndWriteToFile("INFO "+
                        "sessionMsg " + "remainingWordCount>0",
                        fileName
                    );

                    ReadyFlag = true;
                    var text = sanitizeText(Transcribetext)
                    if (text.isNotEmpty()) {
                        fileHandler.createAndWriteToFile("INFO "+"sessionMsg $text", fileName);

                        var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
                        tv_Speech_to_text?.setText(text);
                        tv_Speech_to_text?.setTextColor(Color.parseColor("#808080"))
                        tv_Speech_to_text?.visibility = View.VISIBLE;
                        val jsCode = "sendMessage(\"${Transcribetext}\")";
                        println(jsCode);
                        webView.evaluateJavascript(jsCode, null)
                    }
                }
            }
        }
    }

    private fun getProfilePicUri(): String? {
        Bugfender.d("MainActivity", "getProfilePicUri");
        fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "getProfilePicUri", fileName);

        fileHandler.createAndWriteToFile("INFO "+"MainActivity" + "getProfilePicUri", fileName)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        Bugfender.d("MainActivity", sharedPreferences.getString("PROFILE_PIC_URI", null));
        fileHandler.createAndWriteToFile("INFO "+
            "getProfilePicUri " + sharedPreferences.getString(
                "PROFILE_PIC_URI",
                null
            ), fileName
        );
        return sharedPreferences.getString("PROFILE_PIC_URI", null)
    }

    fun openDrawer() {
        Bugfender.d("MainActivity", "openDrawer");
        fileHandler.createAndWriteToFile("INFO "+"getProfilePicUri " + "openDrawer", fileName);
        val drawerLayout = findViewById<DrawerLayout>(R.id.my_drawer_layout)
        drawerLayout.openDrawer(GravityCompat.END)
    }

    private fun recogniserImage(myId: Int, imageUri: Uri?) {
        Bugfender.d("MainActivity", "recogniserImage");

        Bugfender.d("recogniserImage", imageUri.toString());
        try {
        } catch (e: IOException) {
            Bugfender.d("recogniserImage", "catch");
            Bugfender.e("recogniserImage", e.toString());
            Bugfender.d("recogniserImage", "Exception");
            Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun sessionAlert(context: Context) {
        fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "sessionAlert", fileName);
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.session_timeout_alert, null)

        val alertDialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        // Set click listener for the "OK" button
        view.findViewById<Button>(R.id.okButton).setOnClickListener {
            Bugfender.d("showLogoutAlert", "Yes");
            fileHandler.createAndWriteToFile("INFO "+"showLogoutAlert " + "Yes", fileName);
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, Signin_page::class.java)
                Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
                startActivity(intent)
                finish()
            }
            alertDialog.dismiss()
            // Add any logic you need to perform when the user clicks "OK"
        }

        alertDialog.show()
    }

    fun getContentFromResponse(responseJson: String): String? {
        try {
            // Parse the JSON response
            val jsonResponse = JSONObject(responseJson)

            // Get the message object from the response
            val messageObject = jsonResponse.getJSONObject("data").getJSONObject("message")

            // Get the content field from the message object
            val content = messageObject.getString("content")

            return content
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }



    fun sendGPTTranslationRequest(
        context: Context,
        content: String,
        params: Map<String, String?>,
        callback: (String) -> Unit
    ) {
        val url = "https://ltswebapp.centralindia.cloudapp.azure.com/get_translation"
        println("params"+params.toString());
        fileHandler.createAndWriteToFile("INFO "+
            "sendGPTTranslationRequest " + params.toString(),
            fileName
        );

        mRequestQueue = Volley.newRequestQueue(this)

        mStringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                fileHandler.createAndWriteToFile("INFO "+"Scan " + "Not English", fileName);

                fileHandler.createAndWriteToFile("INFO "+
                    "sendGPTTranslationRequest " + response.toString(),
                    fileName
                );

                val gson = Gson()
                val jsonObject: JsonObject = gson.fromJson(response, JsonObject::class.java)
                val translatedText = jsonObject.getAsJsonPrimitive("translated")?.asString;
                val status = jsonObject.getAsJsonPrimitive("status")?.asString;
                val message = jsonObject.getAsJsonPrimitive("message")?.asString;
                println("response_object")
                println(jsonObject)
                if(status=="<ERROR>")
                {
                    fileHandler.createAndWriteToFile("CRITICAL "+
                            "sendGPTTranslationRequest Error " + response,
                        fileName
                    );
                    Toast.makeText(this, "Unfortunate Error occurred : $message", Toast.LENGTH_LONG).show()
                }
                if (translatedText != null) {
                    fileHandler.createAndWriteToFile("INFO "+
                        "sendGPTTranslationRequest " + translatedText.toString(),
                        fileName
                    );
                    callback(translatedText)
                }
            },
            { error ->
                println("error_object"+error)
                fileHandler.createAndWriteToFile("CRITICAL "+
                    "sendGPTTranslationRequest Error " + error.printStackTrace(),
                    fileName
                );
                callback("Unexpected error please check your internet connection and try again!")
                Log.i(ContentValues.TAG, "Error :" + error.networkResponse)
            }) {
            override fun getParams(): Map<String, String?> {
                return params
            }
        }

        mRequestQueue!!.add(mStringRequest)
    }


    private fun getData(url: String) {

        fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "getData $url", fileName);
        // RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this)

        // String Request initialized
        mStringRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                // This code will be executed upon a successful response

                Bugfender.d("getData", "respoonse from Server: $response");
                fileHandler.createAndWriteToFile("INFO "+
                    "getData " + "respoonse from Server: ${response.toString()}",
                    fileName
                );
                val gson = Gson()
                val jsonObject: JsonObject = gson.fromJson(response, JsonObject::class.java)
                val status = jsonObject.getAsJsonPrimitive("status")?.asString;
                val message = jsonObject.getAsJsonPrimitive("message")?.asString;
                println(jsonObject)
                if(status=="<ERROR>")
                {
                    sessionAlert(this@MainActivity);
                    Toast.makeText(this, "Unfortunate Error occurred : $message", Toast.LENGTH_LONG).show()
                }

            },
            { error ->

                Bugfender.d("getData", "error from Server: $error");
                fileHandler.createAndWriteToFile("ERROR "+
                    "getData " + "error from Server: ${error.printStackTrace()}",
                    fileName
                );

            }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                val statusCode = response.statusCode
                if (statusCode.toString() == "401") {
                    fileHandler.createAndWriteToFile("ERROR "+
                            "sessionAlert " + "401error from Server: sessionAlert}",
                        fileName
                    );
                    sessionAlert(this@MainActivity);
                }
                return super.parseNetworkResponse(response)
            }
        }

        mRequestQueue!!.add(mStringRequest)
    }


    fun sanitizeText(text: String): String {
        Bugfender.d("MainActivity", "sanitizeText");
        Bugfender.d("sanitizeText", text);
        fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "sanitizeText", fileName);
        fileHandler.createAndWriteToFile("INFO "+"sanitizeText_in " + text, fileName);

        var text0 = text.lowercase(Locale.ROOT)
        var sanitizedText = text0
        for (word in profanityList) {
            val replacement = "*".repeat(word.length)
            sanitizedText = sanitizedText.replace(Regex("(?i)\\b$word\\b"), replacement)
            sanitizedText = sanitizedText.replace(Regex("(?i)\\n$word\\n"), replacement)
        }
        Bugfender.d("sanitizeText", sanitizedText);
        fileHandler.createAndWriteToFile("INFO "+"sanitizeText_out " + sanitizedText, fileName);
        return sanitizedText
    }

    private val handlerfile = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            writeToFile(fileName)
            fileHandler.deleteFile(fileName);
            handlerfile.postDelayed(this, 900000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        token = secureTokenManager.loadToken().toString()
        deviceId = secureTokenManager.loadId().toString();
        userEmail = secureTokenManager.loadEmail().toString()
        registerReceiver(sessionMsg, IntentFilter("session_message"),RECEIVER_EXPORTED)
        handlerfile.postDelayed(runnable, 60000) // Start the runnable immediately
    }

    override fun onPause() {
        super.onPause()
        token = secureTokenManager.loadToken().toString()
        deviceId = secureTokenManager.loadId().toString();
        userEmail = secureTokenManager.loadEmail().toString()
        unregisterReceiver(sessionMsg);
        handlerfile.removeCallbacks(runnable) // Stop the runnable when the activity is paused
    }
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        // Save token to savedInstanceState
//        outState.putString("token", token)
//        outState.putString("deviceId", deviceId)
//        outState.putString("userEmail", userEmail)
//    }

    val usageLog = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val Vocab_viewed = intent.extras?.getString("vocab_viewed")
            val category_name = intent.extras?.getString("category_name")
            val word = intent.extras?.getString("word")
            fileHandler.createAndWriteToFile("INFO usageLog"+
                    "Vocab_viewed: " + Vocab_viewed.toString()+"\ncustomer_id: "+category_name.toString()+"\nword: "+word.toString()
                ,fileName
            );
            ///
//            getData("https://trrain4-web.letstalksign.org/app_log?mode=text_opened&language=$selectedLanguage&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token");

            getData("https://ltswebapp.centralindia.cloudapp.azure.com/app_log?mode=vocab_viewed&vocab_category=$category_name&vocab_word=$word&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token");
        }
    }
    fun getScreenDimensions(context: Context): Pair<Int, Int> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        return Pair(width, height)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config: Configuration = resources.configuration
        secureTokenManager = SecureTokenManager(this)
        deviceId = secureTokenManager.loadId().toString();
        userEmail = secureTokenManager.loadEmail().toString()
        token = secureTokenManager.loadToken().toString();
        fileHandler = FileHandler(this)
        secureTokenManager = SecureTokenManager(this)
        // Example usage:
        val data = "app is in background"
        fileName = "$deviceId.txt"
        val screenSize = getScreenDimensions(this)
        val screenWidth = screenSize.first
        val screenHeight = screenSize.second
        println("Screen width: $screenWidth, Screen height: $screenHeight")
//        fileHandler.deleteFile(fileName);
//        fileHandler.deleteFile(fileName);

        fileHandler.createAndWriteToFile("INFO "+data, fileName)
      //  writeToFile(fileName);

//        val isDeleted0 = fileHandler.deleteFile("example.txt")
//        println(isDeleted0);
        val fileContent = fileHandler.readFromFile(fileName)

        val file = File(this.filesDir, fileName);
        if (savedInstanceState != null) {
            // Restore token from savedInstanceState
            token = savedInstanceState.getString("token", "")
            deviceId = savedInstanceState.getString("deviceId", "")
            userEmail = savedInstanceState.getString("userEmail", "")
        } else {
            // Initialize token if savedInstanceState is null
            token = secureTokenManager.loadToken().toString()
            deviceId = secureTokenManager.loadId().toString();
            userEmail = secureTokenManager.loadEmail().toString()
        }

        fileHandler.createAndWriteToFile("INFO "+"content1 ", fileName);
//        Handler().postDelayed({
        fileHandler.createAndWriteToFile("INFO "+"content2 ", fileName);

        registerReceiver(sessionMsg, IntentFilter("session_message"),RECEIVER_EXPORTED)
        registerReceiver(usageLog, IntentFilter("usageLog"))
      //
        if (config.smallestScreenWidthDp >= 600) {
            isTablet=true;
            setContentView(com.example.aiish.R.layout.mainactivity_tablet)
        }
        else {
            isTablet=false;
            setContentView(R.layout.mainactivity)
        }
        var orientationButton = findViewById<Button>(R.id.orientationChangeButton)
        orientationButton.setOnClickListener {
            // Change orientation to landscape
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        if (isTablet) {
            val text_button: ImageView = findViewById(R.id.webview_text_ic)
            val text_title: TextView = findViewById(R.id.texttitle)
            val scan_button: ImageView = findViewById(R.id.webview_scan_ic)
            val scan_title: TextView = findViewById(R.id.scanTitle)
            fun dpToPx(dp: Int): Int {
                val density = resources.displayMetrics.density
                return (dp * density).toInt()
            }

            val mic_button: ImageView = findViewById(R.id.webview_mic_ic)
            val mic_title: TextView = findViewById(R.id.speakTitle)

            fileHandler.createAndWriteToFile("MainActivity " + "isTablet", fileName);
            text_title.setTextSize(25F)
            text_button.layoutParams.height = dpToPx(55);
            text_button.layoutParams.width = dpToPx(55);
            mic_button.layoutParams.height = dpToPx(55) // Increase height by 10dp
            mic_button.layoutParams.width = dpToPx(55)
            mic_title.setTextSize(25F)
            scan_title.setTextSize(25F)
            scan_button.layoutParams.height = dpToPx(55)
            scan_button.layoutParams.width = dpToPx(55);

        }
        val scale = resources.displayMetrics.density
        var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)
        val scanButton = findViewById<ImageView>(R.id.webview_scan_ic);
        val languages = arrayOf(
            "English",
            "Kannada",
             "Hindi",
        )
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1056955407882-m2fv7ko571ndsu9bsh2irnbnb6354gb1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        var sadapter: ArrayAdapter<String>;
        // Create an ArrayAdapter using the string array and a default spinner layout
        if (isTablet) {
            sadapter = ArrayAdapter(this, R.layout.spinner_iteam_tablet, languages);
        } else {
            sadapter = ArrayAdapter(this, R.layout.spinner_iteam, languages);
        }
        var sortedAppInfos = mutableListOf<AppInfo>()
        // Specify the layout to use when the list of choices appears
        val adapter = MyArrayAdapter(this, R.id.iteamlistview, sortedAppInfos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        languageSpinner.adapter = sadapter


        // Set up the OnItemSelectedListener
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Get the selected item from the spinner
                selectedLanguage = languages[position]


                if (selectedLanguage == "English") {
                    language = "en-us";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                } else if (selectedLanguage == "Kannada") {
                    language = "kn-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.KANNADA)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                }
                else if (selectedLanguage == "Hindi") {
                    language = "Hi-IN";
                    options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.HINDI)
                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                        .build()
                }
                englishGermanTranslator = Translation.getClient(options)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Handle the case where nothing is selected (optional)
            }
        }
        var profilePicUriString = intent.getStringExtra("profilepic");
        var profilePicUri = "";
        if (getProfilePicUri() != null)
            profilePicUri = Uri.parse(getProfilePicUri()).toString();
        val imageViewProfilePicture = findViewById<ImageView>(R.id.profile_ic)
        if (profilePicUri != null) {
            Bugfender.d("MainActivity", "profilePicUri");
            fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "profilePicUri", fileName);
            Bugfender.d("profilePicUri", profilePicUri);
            fileHandler.createAndWriteToFile("INFO "+"profilePicUri " + profilePicUri, fileName);
            Glide.with(this)
                .load(profilePicUri)
                .placeholder(R.drawable.ic_profile) // Placeholder image while loading
                .error(R.drawable.ic_profile) // Error image if loading fails
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageViewProfilePicture)
        }
        imageViewProfilePicture.setOnClickListener {
            Bugfender.d("MainActivity", "imageViewProfilePicture");
            fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "imageViewProfilePicture", fileName);
            openDrawer();
        }

        webView = findViewById(R.id.micView)

        // Enable JavaScript in the WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Load the URL
        val userurl = "https://app.aiish.letstalksign.org/page-app-aiish.html"
//        webView.webViewClient = MyWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.userAgentString = "useragentstring"
        webView.getSettings().setSupportMultipleWindows(true);

        webView.settings.javaScriptCanOpenWindowsAutomatically = true

        webView.settings.domStorageEnabled = true

        webView.loadUrl(userurl)
        // webView.loadUrl("file:///android_asset/webview.html");


        webView.clearCache(true) // Clears the cache, including disk and memory caches.
        webView.clearFormData()  // Clears any stored form data in the WebView.
        webView.clearHistory()
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                fileHandler.createAndWriteToFile("INFO "+
                    "WebView Console.log " + consoleMessage.sourceId() + " " + consoleMessage.lineNumber() + " " + consoleMessage.message(),
                    fileName
                );
                Log.d(
                    "WebView Console.log",
                    consoleMessage.sourceId() + " " + consoleMessage.lineNumber() + " " + consoleMessage.message()
                );
                return true
            }
        }

        webView.addJavascriptInterface(WebAppInterface(this), "AndroidInterface")

        val webviewTitle: TextView = findViewById(R.id.title_webview_text);
        val iv_mic: ImageView = findViewById<ImageView>(R.id.webview_mic_ic)
        val text_button: ImageView = findViewById(R.id.webview_text_ic);
        val text_title: TextView = findViewById(R.id.texttitle);
        tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
        if (!isTablet) {
            fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "!isTablet", fileName);
            iv_mic.layoutParams.width = (35 * scale + 0.5f).toInt();
            text_button.layoutParams.width = (35 * scale + 0.5f).toInt();
            scanButton.layoutParams.width = (35 * scale + 0.5f).toInt();
        }
        text_button.setOnClickListener {

            Bugfender.d("MainActivity ", "text_button")
            fileHandler.createAndWriteToFile("INFO "+"MainActivity " + "text_button", fileName);

            getData("https://ltswebapp.centralindia.cloudapp.azure.com/app_log?mode=text_opened&language=$selectedLanguage&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token");
            webviewTitle.text = "Entered Text:";
            val text_button: ImageView = findViewById(R.id.webview_text_ic)
            val text_title: TextView = findViewById(R.id.texttitle)
            val scan_button: ImageView = findViewById(R.id.webview_scan_ic)
            val scan_title: TextView = findViewById(R.id.scanTitle)
            fun dpToPx(dp: Int): Int {
                val density = resources.displayMetrics.density
                return (dp * density).toInt()
            }

            val mic_button: ImageView = findViewById(R.id.webview_mic_ic)
            val mic_title: TextView = findViewById(R.id.speakTitle)

            if (isTablet) {
                fileHandler.createAndWriteToFile("MainActivity " + "isTablet", fileName);
                text_title.setTextSize(30F)
                text_button.layoutParams.height = dpToPx(65);
                text_button.layoutParams.width = dpToPx(65);
                mic_button.layoutParams.height = dpToPx(55) // Increase height by 10dp
                mic_button.layoutParams.width = dpToPx(55)
                mic_title.setTextSize(25F)
                scan_title.setTextSize(25F)
                scan_button.layoutParams.height = dpToPx(55)
                scan_button.layoutParams.width = dpToPx(55);

            } else {
                fileHandler.createAndWriteToFile("MainActivity " + "notisTablet", fileName);
                text_title.setTextSize(18F)
                text_button.layoutParams.height = dpToPx(42);
                text_button.layoutParams.width = dpToPx(42);
                mic_button.layoutParams.height = dpToPx(35) // Increase height by 10dp
                mic_button.layoutParams.width = dpToPx(35)
                mic_title.setTextSize(15F)
                scan_title.setTextSize(15F)
                scan_button.layoutParams.height = dpToPx(35)
                scan_button.layoutParams.width = dpToPx(35);
            }
            tv_Speech_to_text.setText("Click the text button above to type text and initiate interpretation.")
            showPopupWithEditText("", "Text to Interpret")
        }
        iv_mic?.let { micButton ->
            micButton.setOnClickListener(View.OnClickListener {
                getData("https://ltswebapp.centralindia.cloudapp.azure.com/app_log?mode=audio_opened&language=$selectedLanguage&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token")
                Bugfender.d("MainActivity", "micButton");
                fileHandler.createAndWriteToFile("MainActivity " + "micButton", fileName);

                val text_button: ImageView = findViewById(R.id.webview_text_ic)
                val text_title: TextView = findViewById(R.id.texttitle)
                val scan_button: ImageView = findViewById(R.id.webview_scan_ic)
                val scan_title: TextView = findViewById(R.id.scanTitle)
                fun dpToPx(dp: Int): Int {
                    val density = resources.displayMetrics.density
                    return (dp * density).toInt()
                }

                val mic_button: ImageView = findViewById(R.id.webview_mic_ic)
                val mic_title: TextView = findViewById(R.id.speakTitle)

                if (isTablet) {
                    fileHandler.createAndWriteToFile("micButton " + "isTablet", fileName);

                    text_title.setTextSize(25F)
                    text_button.layoutParams.height = dpToPx(55);
                    text_button.layoutParams.width = dpToPx(55);
                    mic_button.layoutParams.height = dpToPx(65) // Increase height by 10dp
                    mic_button.layoutParams.width = dpToPx(65)
                    mic_title.setTextSize(30F)
                    scan_title.setTextSize(25F)
                    scan_button.layoutParams.height = dpToPx(55)
                    scan_button.layoutParams.width = dpToPx(55);

                } else {
                    fileHandler.createAndWriteToFile("micButton " + "notisTablet", fileName);

                    text_title.setTextSize(15F)
                    text_button.layoutParams.height = dpToPx(35);
                    text_button.layoutParams.width = dpToPx(35);
                    mic_button.layoutParams.height = dpToPx(42) // Increase height by 10dp
                    mic_button.layoutParams.width = dpToPx(42)
                    mic_title.setTextSize(18F)
                    scan_title.setTextSize(15F)
                    scan_button.layoutParams.height = dpToPx(35)
                    scan_button.layoutParams.width = dpToPx(35);
                }
                webviewTitle.setText("Spoken Text:")
                tv_Speech_to_text.setText("Click the mic button above perform speach to sign interpretation.")
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)

                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
                } catch (e: Exception) {
                    println(e.toString())
                }

            })
        }

        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Bugfender.d("Scan", "Camera activity launcher");
            Bugfender.d("ScanResult", result.toString());
            Bugfender.d("ScanResult", result.resultCode.toString() + imageUri.toString());

            if (result.resultCode == Activity.RESULT_OK) {
                // Bugfender.d("ImageOK",imageUri.toString());
                imageUri?.let { recogniserImage(0, it) };
                // Handle the result here
                // The captured image is usually available via the 'imageUri' property
            } else {
                Bugfender.e("ScanError", result.toString());
            }
        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.my_drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            Bugfender.d("MainActivity", "navigationView.setNavigationItemSelectedListener");
            fileHandler.createAndWriteToFile(
                "MainActivity " + "navigationView.setNavigationItemSelectedListener",
                fileName
            );
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    openAccessibilitySettings();
                    fileHandler.createAndWriteToFile(
                        "MainActivity " + "openAccessibilitySettings",
                        fileName
                    )
                    // Handle settings click
                    // Add your logic here
                    true
                }

                R.id.nav_logout -> {
                    fileHandler.createAndWriteToFile(
                        "MainActivity " + "showLogoutAlert();",
                        fileName
                    )
                    showLogoutAlert();

                    // Handle logout click
                    // Add your logic here
                    true
                }

                else -> false
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Bugfender.d("MainActivity", "onSaveInstanceState")
        outState.putString("token", token)
        outState.putString("deviceId", deviceId)
        outState.putString("userEmail", userEmail)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Bugfender.d("ImageUriff", imageUri.toString())
        token = savedInstanceState.getString("token").toString()
        deviceId = savedInstanceState.getString("deviceId").toString()
        userEmail = savedInstanceState.getString("userEmail").toString()
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            Bugfender.d("MainActivity", "onActivityResult")
            fileHandler.createAndWriteToFile("MainActivity " + "onActivityResult();", fileName)
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text)
                tv_Speech_to_text!!.visibility = View.VISIBLE
                if (selectedLanguage != "English") {
                    fileHandler.createAndWriteToFile(
                        "onActivityResult() " + "selectedLanguage!=\"English\";",
                        fileName
                    )
                    val progressDialog = ProgressDialog(this@MainActivity)
                    progressDialog.setMessage("Translating...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    token = secureTokenManager.loadToken().toString();
                    deviceId = secureTokenManager.loadId().toString();
                    userEmail = secureTokenManager.loadEmail().toString()


                    var params: Map<String, String> = mapOf();
                    if(selectedLanguage=="Hindi")
                    {
                        params = mapOf(
                            "token" to token.toString(),
                            "maxWordCount" to "50",
                            "fromLang" to "Hindi",
                            "toLang" to "english",
                            "textToTranslate" to " " + Objects.requireNonNull(result)?.get(0),
                            "customer_id" to "10016",
                            "device_id" to deviceId,
                            "gmail_id" to userEmail
                        )
                    }
                    else if(selectedLanguage=="Kannada")
                    {
                        params = mapOf(
                            "token" to token.toString(),
                            "maxWordCount" to "50",
                            "fromLang" to "Kannada",
                            "toLang" to "english",
                            "textToTranslate" to " " + Objects.requireNonNull(result)?.get(0),
                            "customer_id" to "10016",
                            "device_id" to deviceId,
                            "gmail_id" to userEmail
                        )
                    }

                    // Make the translation request
                    fileHandler.createAndWriteToFile(
                        "onActivityResult() selectedLanguage!=\"English\\ " + params.toString(),
                        fileName
                    )
                    //     sendTranslationRequest(this," " + Objects.requireNonNull(result)?.get(0),params);
                    sendGPTTranslationRequest(
                        this,
                        " " + Objects.requireNonNull(result)?.get(0),
                        params
                    ) { translatedText ->
                        // Dismiss the progress dialog
                        progressDialog.dismiss()

                        if (translatedText == "Translation failed" || translatedText == "Unexpected error please check your internet connection and try again!") {
                            Toast.makeText(this@MainActivity, translatedText, Toast.LENGTH_LONG)
                                .show()
                        } else {
                            if (translatedText != null) {
                                fileHandler.createAndWriteToFile(
                                    "onActivityResult() selectedLanguage!=\"English\\ " + translatedText,
                                    fileName
                                )
                                showPopupWithEditText(translatedText, "Recognised Text")
                            }
                        }
                    }

                } else {
                    fileHandler.createAndWriteToFile(
                        "onActivityResult() else " + Objects.requireNonNull(
                            result
                        )?.get(0), fileName
                    )
                    showPopupWithEditText(
                        " " + Objects.requireNonNull(result)?.get(0),
                        "Recognised Text"
                    )
                }

                // Show loading progress dialog

            }
        }
    }


    private fun openAccessibilitySettings() {
        Bugfender.d("MainActivity", "openNormalSettings")
        fileHandler.createAndWriteToFile("MainActivity " + "openNormalSettings", fileName)
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivityForResult(intent, 1)
    }

    private fun showLogoutAlert() {
        Bugfender.d("MainActivity", "showLogoutAlert");
        fileHandler.createAndWriteToFile("MainActivity " + "showLogoutAlert", fileName)
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            Bugfender.d("showLogoutAlert", "Yes");
            fileHandler.createAndWriteToFile("showLogoutAlert " + "Yes", fileName)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, Signin_page::class.java)
                Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
                getData("https://ltswebapp.centralindia.cloudapp.azure.com/app_log?mode=logout&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token");
                startActivity(intent)
                finish()
            }
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            Bugfender.d("showLogoutAlert", "Cancel");
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun showProgressDialog() {
        Bugfender.d("MainActivity", "showProgressDialog");
        fileHandler.createAndWriteToFile("MainActivity " + "showProgressDialog", fileName)
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Downloading translation model please wait this may take few seconds...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    private fun dismissProgressDialog() {
        Bugfender.d("MainActivity", "dismissProgressDialog");
        fileHandler.createAndWriteToFile("MainActivity " + "dismissProgressDialog", fileName)
        progressDialog?.dismiss()
        progressDialog = null
    }

    fun translatetoEnglish(msg: CharSequence, callback: (String) -> Unit) {
        showProgressDialog()

        englishGermanTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                dismissProgressDialog()

                englishGermanTranslator.translate(msg.toString())
                    .addOnSuccessListener { translatedText ->
                        println("Translation success: $translatedText")
                        callback(translatedText)
                    }
                    .addOnFailureListener { exception: Exception ->
                        println("Translation failure: $exception")
                        callback("Translation failed")
                    }
            }
            .addOnFailureListener { exception: Exception ->
                dismissProgressDialog()
                println("Model download failure: $exception")
                callback("Model download failed")
            }
    }

    fun isAscii(char: Char): Boolean {
        val codePoint = char.toInt()
        return (codePoint in 97..122) || (codePoint in 65..90)
    }
    fun isSpecialCharacter(char: Char): Boolean {
        return char in setOf(',', '.', '?', '!', ';', ':', '-', '_', '/', '\\', '(', ')', '[', ']', '{', '}', '<', '>', '|', '*', '&', '^', '%', '$', '#', '@', '~', '`', '"', '\'', '+', '=', ' ', '\t', '\n', '\r') || char in '0'..'9'
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    private fun showPopupWithEditText(initialText: CharSequence, Title: String) {
        Bugfender.d("MainActivity", "showPopupWithEditText");
        fileHandler.createAndWriteToFile("INFO" + "MainActivity " + "showPopupWithEditText", fileName);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        val builder = AlertDialog.Builder(this)
        val layout = layoutInflater.inflate(R.layout.popup_layout, null)
        interpretButton = layout.findViewById<Button>(R.id.interpretButton);
        val languagetext: TextView = layout.findViewById(R.id.type_title);
        var editText = layout.findViewById<EditText>(R.id.popupEditText)
        var title = layout.findViewById<TextView>(R.id.titlePopup);
        var remainingWords = layout.findViewById<TextView>(R.id.remainingWordCount);

        val cancelButton = layout.findViewById<Button>(R.id.cancelButton);
        var titletext = languagetext.text;
        var ogtext = titletext;
        println("jkdsvhjbfhjbvfhbjbfvdjhbdjhdbhbf " + initialText)
        if (initialText == "") {
            interpretButton.isEnabled = false;
            println("sdbvhjbschsbhdbdhbdcnvjfvj");
        }
        var containsForeignWord = false
        fun containsNonAlphanumeric(text: String): Boolean {
            for (char in text) {
                if (!char.isLetterOrDigit() && !char.isWhitespace()) {
                    return true
                }
            }
            return false
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val englishNote = layout.findViewById<TextView>(R.id.EnglishNote);
                // Calculate the current word count
                val words = charSequence?.trim()?.split(Regex("\\s+"))
                currentWordCount = words?.size ?: 0

                // Update the remaining word count
                remainingWordCount = maxWordCount - currentWordCount
                // Assuming you have a TextView to display the remaining count
                // You can replace R.id.remainingWordCount with the actual ID of your TextView
                remainingWords.text = "RemainingWords: $remainingWordCount"

                // Enable/disable the interpret button based on the word count
                if (remainingWordCount < 0) {
                    interpretButton.isEnabled = false

                    //interpretButton.setBackgroundColor("")
                    //ReadyFlag = false
                }
                if (interpretButton.text == "Interpret" && remainingWordCount >= 0) {
                    interpretButton.isEnabled = true
                    englishNote.visibility = View.GONE;

                }
                if (charSequence?.length == 0 || charSequence.isNullOrBlank()) {
                    interpretButton.isEnabled = false
                }
                if (selectedLanguage == "Kannada" && Title == "Text to Interpret") {
                    if (words?.isNotEmpty() == true) {
                        for (word in words) {
                            for (char in word) {
                                if (!isSpecialCharacter(char) && isAscii(char)) {
                                    englishNote.setText("Note: Kannada language is chosen. Please type Kannada sentences.")
                                    englishNote.visibility = View.VISIBLE;
                                    interpretButton.isEnabled = false
                                    break;
                                }
                            }
                        }
                    }
                } else if (selectedLanguage == "Hindi" && Title == "Text to Interpret") {
                    if (words?.isNotEmpty() == true) {
                        for (word in words) {
                            for (char in word) {
                                if (!isSpecialCharacter(char) && isAscii(char)) {
                                    englishNote.setText("Note: Hindi language is chosen. Please type Gindi sentences.")
                                    englishNote.visibility = View.VISIBLE;
                                    interpretButton.isEnabled = false
                                    break;
                                }
                            }
                        }
                    }
                } else if (selectedLanguage == "English" && Title == "Text to Interpret") {
                    if (words?.isNotEmpty() == true) {
                        for (word in words) {
                            for (char in word) {
                                if (!isSpecialCharacter(char) && !isAscii(char)) {
                                    englishNote.setText("Note:English language is chosen. Please type English sentences.")
                                    englishNote.visibility = View.VISIBLE;
                                    interpretButton.isEnabled = false
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        if (selectedLanguage == "English") {
            languagetext.visibility = View.GONE;
            editText.setImeHintLocales(LocaleList(Locale("en", "us")))
        } else if (selectedLanguage == "Tamil") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "vanakam for " + "வணக்கம். This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("ta", "IN")))
        } else if (selectedLanguage == "Telugu") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Halō for " + "హలో. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("te", "IN")))
        } else if (selectedLanguage == "Kannada") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Namaskara for " + "ನಮಸ್ಕಾರ. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("kn", "IN")))
        } else if (selectedLanguage == "Malayalam") {
            editText.setImeHintLocales(LocaleList(Locale("ml", "IN")))
        } else if (selectedLanguage == "Hindi") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "namaste for " + "नमस्ते. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("hi", "IN")))
        } else if (selectedLanguage == "Gujarati") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "namaste for " + "નમસ્તે. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("gu", "IN")))
        } else if (selectedLanguage == "Marathi") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Namaskāra for " + "नमस्कार. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("mr", "IN")))
        } else if (selectedLanguage == "Bengali") {
            titletext = ogtext;
            languagetext.visibility = View.VISIBLE;
            titletext =
                titletext.toString() + "i.e " + "Hyālō for " + "হ্যালো. This feature only works on selected keybords like Gboard."
            languagetext.setText(titletext);
            editText.setImeHintLocales(LocaleList(Locale("bn", "IN")))
        }


        title.setText(Title)
        //editText.setText("");
        if (initialText != "")
            editText.setText(sanitizeText(initialText.toString()))

        interpretButton.setText("Interpret")

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {


            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })




        builder.setView(layout)
        val dialog = builder.create();
        dialog.show();
        interpretButton.setOnClickListener {
            deviceId = secureTokenManager.loadId().toString();
            userEmail = secureTokenManager.loadEmail().toString()
            token = secureTokenManager.loadToken().toString();
            fileHandler.createAndWriteToFile("INFO" +"MainActivity " + "interpretButton", fileName);
            if (Title == "Scanned Text") {
                getData("https://ltswebapp.centralindia.cloudapp.azure.com/app_log?mode=scan_interpreted&language=english&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token")
            } else if (Title == "Text to Interpret") {
                getData("https://ltswebapp.centralindia.cloudapp.azure.com/app_log?mode=text_interpreted&language=$selectedLanguage&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token")
            } else if (Title == "Recognised Text") {
                getData("https://ltswebapp.centralindia.cloudapp.azure.com/app_log?mode=audio_interpreted&language=$selectedLanguage&customer_id=10016&device_id=$deviceId&gmail_id=$userEmail&token=$token")
            }
            if (selectedLanguage != "English" && Title == "Text to Interpret") {
                fileHandler.createAndWriteToFile(
                    "MainActivity " + "selectedLanguage != \"English\" && Title==\"Text to Interpret\"",
                    fileName
                );
                val progressDialog = ProgressDialog(this@MainActivity)
                progressDialog.setMessage("Translating...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                deviceId = secureTokenManager.loadId().toString();
                userEmail = secureTokenManager.loadEmail().toString()
                token = secureTokenManager.loadToken().toString();
                var params: Map<String, String> = mapOf();
                if (selectedLanguage == "Kannada") {
                    params = mapOf(
                        "token" to token.toString(),
                        "maxWordCount" to "50",
                        "fromLang" to "Kannada",
                        "toLang" to "english",
                        "textToTranslate" to editText.text.toString(),
                        "customer_id" to "10016",
                        "device_id" to deviceId,
                        "gmail_id" to userEmail
                    )
                } else if (selectedLanguage == "Hindi") {
                    params = mapOf(
                        "token" to token.toString(),
                        "maxWordCount" to "50",
                        "fromLang" to "Hindi",
                        "toLang" to "english",
                        "textToTranslate" to editText.text.toString(),
                        "customer_id" to "10016",
                        "device_id" to deviceId,
                        "gmail_id" to userEmail
                    )

                }
                fileHandler.createAndWriteToFile("INFO "+"selectedLanguage " + params.toString(), fileName);
                sendGPTTranslationRequest(this, editText.text.toString(), params) { text ->
                    progressDialog.dismiss();
                    Transcribetext = text
                    fileHandler.createAndWriteToFile(
                        "sendGPTTranslationRequestTrans " + Transcribetext.toString(),
                        fileName
                    );
                    println("translated text" + text);
                    Bugfender.d("MainAcEdittextPopup", text.toString());
                    var ftext = text
                    ftext = ftext.replace("\n", "");
                    ftext = ftext.replace("\b", "");
                    var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
                    if (!ReadyFlag) {
                        tv_Speech_to_text?.setText("Please wait the model is loading...!")
                        val redColor = "0xFF0000" // Red color in hexadecimal
                        tv_Speech_to_text?.setTextColor(Color.parseColor(redColor))
                    }
                    else {
                        tv_Speech_to_text?.setText(ftext);
                        tv_Speech_to_text?.setTextColor(Color.parseColor("#808080"))
                        tv_Speech_to_text?.visibility = View.VISIBLE;

                        //val jsCode = "sendMessage('${ftext}');"
                        val jsCode = "sendMessage(\"${ftext}\")";
                        println(jsCode);
                        webView.evaluateJavascript(jsCode, null)
                    }
                }
            }
            else {
                Bugfender.d("EditText", editText.text.toString());

                fileHandler.createAndWriteToFile("INFO "+"EditText " + editText.text.toString(), fileName);
                println(editText.text);
                Transcribetext = editText.text.toString();
                var ftext = editText.text.toString()
                ftext = ftext.replace("\n", "");
                ftext = ftext.replace("\b", "");
                var tv_Speech_to_text = findViewById<TextView>(R.id.webview_text);
                var sanitized_text = "";
                println("wefhibgufhdvbhfdbhjbhjsd");
                sanitized_text = sanitizeText(ftext)
                if (!ReadyFlag) {
                    Transcribetext = sanitized_text;
                    fileHandler.createAndWriteToFile(
                        "EditText " + Transcribetext.toString(),
                        fileName
                    );
                    tv_Speech_to_text?.setText("Please wait the model is loading...!")
                    tv_Speech_to_text?.setTextColor(Color.parseColor("#FF0000"))
                    tv_Speech_to_text?.visibility = View.VISIBLE;
                } else {

                    sanitized_text = sanitizeText(ftext)
                    fileHandler.createAndWriteToFile(
                        "EditText " + sanitized_text.toString(),
                        fileName
                    )
                    tv_Speech_to_text?.setTextColor(Color.parseColor("#808080"))
                    tv_Speech_to_text?.setText(sanitized_text);
                    tv_Speech_to_text?.visibility = View.VISIBLE;
                }
                if (ReadyFlag) {
                    if (sanitized_text != ftext.toLowerCase()) {

                        fileHandler.createAndWriteToFile("INFO "+
                            "sanitized_text!=ftext.toLowerCase() " + sanitized_text.toString(),
                            fileName
                        )

                        var errortext = "Exclude inappropriate words for interpretation."
                        val jsCode = "sendMessage(\"${errortext}\")";

                        webView.evaluateJavascript(jsCode, null)
                        dialog.dismiss()
                    } else {
                        fileHandler.createAndWriteToFile("INFO "+"showpopup " + sanitized_text, fileName)

                        val jsCode = "sendMessage(\"${sanitized_text}\")";

                        webView.evaluateJavascript(jsCode, null)
                        dialog.dismiss()
                    }
                } else {
                    if (sanitized_text != ftext.toLowerCase()) {

                        fileHandler.createAndWriteToFile("INFO "+
                            "sanitized_text " + sanitized_text,
                            fileName
                        )
                        fileHandler.createAndWriteToFile("INFO "+"ftext " + ftext, fileName)

                        var errortext = "Exclude inappropriate words for interpretation."
                        Transcribetext = "Exclude inappropriate words for interpretation."
                        dialog.dismiss()
                    } else {
                        Transcribetext = sanitized_text
                        fileHandler.createAndWriteToFile("INFO "+
                            "showpopup else Transcribetext" + Transcribetext,
                            fileName
                        )
//
                        dialog.dismiss()
                    }
                }

                // Do something with the new text
                dialog.dismiss()
            }
            dialog.dismiss()
        }
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            fun showLanguageNotSupportedDialog(context: Context) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                val builder = AlertDialog.Builder(this)
                val layout = layoutInflater.inflate(R.layout.language_support_alert, null)
                val okButoon: Button = layout.findViewById(R.id.okButton);
                builder.setView(layout)
                val dialog = builder.create();
                okButoon.setOnClickListener {
                    dialog.dismiss();
                }
                dialog.show();
            }
        }
}
