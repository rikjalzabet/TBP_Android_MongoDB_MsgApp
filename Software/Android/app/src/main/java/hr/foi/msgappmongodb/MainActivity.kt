package hr.foi.msgappmongodb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import hr.foi.msgappmongodb.Activity.UserListActivity
import hr.foi.msgappmongodb.DataClass.ApiService
import hr.foi.msgappmongodb.DataClass.User
import hr.foi.msgappmongodb.DataClass.getApiService
import hr.foi.msgappmongodb.Helpers.loggedInUsername
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()

            if (username.isNotEmpty()) {
                lifecycleScope.launch {
                    checkUsernameExistence(username)
                }
            } else {
                Toast.makeText(this, "Please enter a username!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkUsernameExistence(username: String) {
        lifecycleScope.launch {
            val apiService = getApiService()
            try {
                val response = apiService.getUserByUsername(username)
                if (response != null) {
                    Log.i("MongoDBMa12", "Username found")
                    Log.i("MongoDBMa12WW", "User: ${response.username}")
                    loggedInUsername=response.username
                    val intent = Intent(this@MainActivity, UserListActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.i("MongoDBMa12", "Username not found")
                }
            } catch (e: Exception) {
                Log.e("MongoDBMa12", "Error connecting to server: ${e.message}")
            }
        }
    }
}
