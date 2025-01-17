package hr.foi.msgappmongodb.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import hr.foi.msgappmongodb.DataClass.ApiService
import hr.foi.msgappmongodb.DataClass.getApiService
import hr.foi.msgappmongodb.Helpers.loggedInUsername
import hr.foi.msgappmongodb.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserListActivity : AppCompatActivity() {
    private lateinit var userListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userListView = findViewById(R.id.userListView)

        fetchUsers()

        userListView.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = parent.getItemAtPosition(position) as String

             // Get this from your app's login system or shared preferences
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("loggedInUsername", loggedInUsername)
                putExtra("chatUsername", selectedUser)
            }
            startActivity(intent)
        }
    }
    private fun fetchUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val apiService = getApiService()
            try {
                val users = apiService.getAllUsers()
                val usernames = users.map { it.username } // Assuming 'username' is a field in the User model
                val adapter = ArrayAdapter(this@UserListActivity, android.R.layout.simple_list_item_1, usernames)
                userListView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}