package com.example.aiish
import android.R
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.google.gson.JsonObject


class Signin_page : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var rememberCheckBox: CheckBox
    val Req_Code: Int = 123
    private var progressDialog: ProgressDialog? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var secureTokenManager: SecureTokenManager

    private var mRequestQueue: RequestQueue? = null
    private var mStringRequest: StringRequest? = null

    private fun postData(url: String, params: Map<String, String?>) {
        println("yessssssssssssssssssssssssssssjbhvbdhbvhjbjdbhfg")

        // RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this)

        // String Request initialized
        mStringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                println("response got from server")
                val gson = Gson()
                val jsonObject: JsonObject = gson.fromJson(response, JsonObject::class.java)
                val id= jsonObject.getAsJsonPrimitive("token")?.asString;
                secureTokenManager.saveToken(id.toString());
            },
            { error ->
                println("errrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr SIgninactivity")
                println(error)
                Log.i(ContentValues.TAG, "Error :" + error.networkResponse)
            }) {
            override fun getParams(): Map<String, String?> {
                return params
            }
        }
        mRequestQueue!!.add(mStringRequest)
    }


//    private val serviceIntent by lazy { Intent(this, FloatingWindowGFG::class.java) }

//    override fun onResume() {
//        super.onResume()
//        startService(serviceIntent)
//    }

    override fun onPause() {
        super.onPause()
      //  stopService(serviceIntent)
    }

    private fun getAndroidId(context: Context): String {
        //Bugfender.d("MainActivity","getAndroidId");
        val id= Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID);
        secureTokenManager.saveId(id);
        return id;
//        Bugfender.d("getAndroidId",
//            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config: Configuration = resources.configuration
        println("sbhjcbshjbdsdsdjjvhjbfhjbj");
//        nt width = getWindowManager().getDefaultDisplay().getWidth();
//        int height = getWindowManager().getDefaultDisplay().getHeight()
        println(config.smallestScreenWidthDp);
        //setContentView(com.example.aiish.R.layout.signup_page)
        //config.smallestScreenWidthDp >= 600
       // if (config.smallestScreenWidthDp >= 600) {
      //      setContentView(com.example.aiish.R.layout.signup_tablet)
//            setContentView(R.layout.main_activity_tablet)
//        } else {
              setContentView(com.example.aiish.R.layout.signup_page)
//            setContentView(R.layout.main_activity)
//        }
      //  setContentView(R.layout.signup_page)
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        println("heelowidthandheight")
        println(screenWidth);
        println(screenHeight);

        val Google_signincard=findViewById<androidx.cardview.widget.CardView>(com.example.aiish.R.id.Google_signincard)
        val Google_signincard_layoutParams: ViewGroup.LayoutParams = Google_signincard.layoutParams
        Google_signincard_layoutParams.height =(screenWidth/6).toInt()
        Google_signincard_layoutParams.width=(screenWidth/1.28823).toInt()

        val signupTitle=findViewById<TextView>(com.example.aiish.R.id.signupTitle)
        signupTitle.setTextSize((screenHeight/76.333333).toFloat());
        val rememberCheckBoxl=findViewById<CheckBox>(com.example.aiish.R.id.rememberCheckBox)
        rememberCheckBoxl.setTextSize((screenWidth/77.14285714).toFloat());

        val lts_logo=findViewById<ImageView>(com.example.aiish.R.id.lts_logo)
        val layoutParams: ViewGroup.LayoutParams = lts_logo.layoutParams
        layoutParams.height = (screenWidth/1.8).toInt()
        layoutParams.width=(screenWidth/1.6615).toInt()

        val dvt_logo_lts_logo=findViewById<ImageView>(com.example.aiish.R.id.dvt_logo)
        val dvt_logo_layoutParams: ViewGroup.LayoutParams = dvt_logo_lts_logo.layoutParams
        dvt_logo_layoutParams.height = (screenWidth/5.39999).toInt()
        dvt_logo_layoutParams.width=(screenHeight/2.86250).toInt()

        val LinearLayout_Signin=findViewById<LinearLayout>(com.example.aiish.R.id.LinearLayout_Signin)
        val LinearLayout_Signin_layoutParams: ViewGroup.LayoutParams = LinearLayout_Signin.layoutParams
        LinearLayout_Signin_layoutParams.height = (screenHeight/1.27184).toInt()

        secureTokenManager = SecureTokenManager(this)
        rememberCheckBox = findViewById(com.example.aiish.R.id.rememberCheckBox)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val rememberUser = sharedPreferences.getBoolean("REMEMBER_USER", true)
        rememberCheckBox.isChecked = rememberUser
        progressDialog = ProgressDialog(this)
        var Signin=findViewById<ImageView>(com.example.aiish.R.id.Signin);
        val receivedIntent = intent
//        var sharedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT)
//        if (sharedText != null) {
//            val intent4 = Intent(this@Signin_page, FloatingWindowGFG::class.java);
//            intent4.putExtra("url", sharedText.toString());
//            startService(intent4);
//            finish();
//        };

        FirebaseApp.initializeApp(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1056955407882-m2fv7ko571ndsu9bsh2irnbnb6354gb1.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        Signin.setOnClickListener { view: View? ->
            var id=getAndroidId(this);
            Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            signInGoogle()
        }
    }
    fun onAppPause() {
        Toast.makeText(applicationContext, "App in background", Toast.LENGTH_LONG).show()

    }
    private fun signInGoogle() {
        progressDialog?.setMessage("Logging In...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("REMEMBER_USER", rememberCheckBox.isChecked)
        editor.apply()
    }

    // onActivityResult() function : this is where
    // we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                var id= getAndroidId(this);
                val userEmail = account?.email
                val userName = account?.displayName
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("UserEmail", userEmail);
                editor.putString("UserName",userName);
                editor.apply()
                if (userEmail != null) {
                    secureTokenManager.saveEmail(userEmail)
                };
                val url = "https://ltswebapp.centralindia.cloudapp.azure.com/app_login"
                val params= mapOf("customer_id" to "10016", "device_id" to id.toString(),"gmail_id" to userEmail, "full_name" to userName)
                postData(url, params)
                UpdateUI(account)
            }
        } catch (e: ApiException) {
            println("errrrrrrrrrrrrrrrrrror");
            println(e.toString());
//            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // this is where we update the UI after Google signin takes place
    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            progressDialog?.dismiss()
            if (task.isSuccessful) {

                val user = firebaseAuth.currentUser
                val profilePicUri = user?.photoUrl

                // Now you have the profile picture URI, you can use it as needed
                if (profilePicUri != null) {
                    println("yeeeeeeeeeeeeeeeeeeeeeeeeeeeeeessshelo");
                    println(profilePicUri);
                    saveProfilePicUri(profilePicUri.toString())
                    // Use a library like Picasso or Glide to load and display the image
                    // Example using Picasso (make sure to add the Picasso dependency to your app)
                    // Picasso.get().load(profilePicUri).into(imageViewProfilePicture)

                    // Example using Glide (make sure to add the Glide dependency to your app)
                    // Glide.with(this).load(profilePicUri).into(imageViewProfilePicture)
                }
//                SavedPreference.setEmail(this, account.email.toString())
//                SavedPreference.setUsername(this, account.displayName.toString())
                println("Noooooooooooooooooooooooooooooooooooooooolooooooo");
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("profilepic",profilePicUri.toString());
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this,"unexpected error occurred please try again",Toast.LENGTH_LONG).show();
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val rememberUser = sharedPreferences.getBoolean("REMEMBER_USER", true)

        if (GoogleSignIn.getLastSignedInAccount(this) != null && rememberUser) {
            startActivity(
                Intent(
                    this, MainActivity
                    ::class.java
                )
            )
            finish()
        }
    }
    private fun saveProfilePicUri(uri: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("PROFILE_PIC_URI", uri)
        editor.apply()
    }
}
