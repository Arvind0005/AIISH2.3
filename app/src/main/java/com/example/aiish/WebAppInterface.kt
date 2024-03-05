import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebAppInterface(private val context: Context) {

    // This method can be called from JavaScript
       var response="";
    @JavascriptInterface
    fun usageLog(vocab_viewed: String,category_name: String,word: String) {
        println("USAGE_LOG_TESTING")
        println(vocab_viewed+category_name+word)
        val intent = Intent("usageLog")
        intent.putExtra("vocab_viewed", vocab_viewed)
        intent.putExtra("category_name", category_name)
        intent.putExtra("word", word)
        context.sendBroadcast(intent);
    }

    @JavascriptInterface
    fun sendDataToAndroid(data: String) {
        println("sendDataToAndroid() messgae"+data);
        val intent = Intent("session_message")
        intent.putExtra("session_message", data)
        context.sendBroadcast(intent);
        response=data;
    }
    @JavascriptInterface
    fun missingAnimations(word: String) {
        println("heeeeeeeeeeeeeeeeeeloooo-222")
        val intent = Intent("missingAnimations")
        intent.putExtra("missingAnimations", "ready")
        context.sendBroadcast(intent);
        response=word;
    }
}
