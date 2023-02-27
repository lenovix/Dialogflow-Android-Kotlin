package com.kamil.dialogflowandroidkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kamil.dialogflowandroidkotlin.adapters.ChatAdapter
import com.kamil.dialogflowandroidkotlin.models.Message

class MainActivity : AppCompatActivity() {
    private var messageList: ArrayList<Message> = ArrayList()

    private val TAG = "mainactivity"
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chatView: RecyclerView = findViewById(R.id.chatView)
        val btnSend: ImageButton = findViewById(R.id.btnSend)
        val editMessage: EditText = findViewById(R.id.editMessage)

        // setting adapter to recyclerview
        chatAdapter = ChatAdapter(this, messageList)
        chatView.adapter = chatAdapter

        // onclick listener to update the list and call dialogflow
        btnSend.setOnClickListener{
            val message: String = editMessage.text.toString()
            if (message.isNotEmpty()){
                addMessageToList(message, false)
                sendMessageToList(message)
            }else{
                Toast.makeText(this, "Please enter text!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessageToList(message: String) {
        // send message to the bot
    }

    private fun setUpBot(){

    }

    private fun addMessageToList(message: String, isReceived: Boolean) {
        // handle UI change
    }
}