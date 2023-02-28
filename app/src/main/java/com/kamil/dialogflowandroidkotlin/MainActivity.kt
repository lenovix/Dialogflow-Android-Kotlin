package com.kamil.dialogflowandroidkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.*
import com.kamil.dialogflowandroidkotlin.adapters.ChatAdapter
import com.kamil.dialogflowandroidkotlin.models.Message
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private var messageList: ArrayList<Message> = ArrayList()

    private val TAG = "mainactivity"
    private lateinit var chatAdapter: ChatAdapter

    // dialogflow
    private var sessionsClient: SessionsClient? = null
    private var sessionName: SessionName? = null
    private val uuid = UUID.randomUUID().toString()

    private val chatView: RecyclerView = findViewById(R.id.chatView)
    private val btnSend: ImageButton = findViewById(R.id.btnSend)
    private val editMessage: EditText = findViewById(R.id.editMessage)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // initial bot config
        setUpBot()
    }

    private fun sendMessageToList(message: String) {
        // send message to the bot
        val input = QueryInput.newBuilder()
            .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build()

        GlobalScope.launch {
            sendMessageInBg(input)
        }
    }

    private suspend fun sendMessageInBg(queryInput: QueryInput){
        withContext(Default){
            try {
                val detectIntentRequest = DetectIntentRequest.newBuilder()
                    .setSession(sessionName.toString())
                    .setQueryInput(queryInput)
                    .build()
                val result = sessionsClient?.detectIntent(detectIntentRequest)
                if (result != null){
                    runOnUiThread{
                        updateIU(result)
                    }
                }
            }catch (e: java.lang.Exception){
                Log.d(TAG, "doInBackground: "+ e.message)
                e.printStackTrace()
            }
        }
    }

    private fun updateIU(response: DetectIntentResponse){
        val botReply: String = response.queryResult.fulfillmentText
        if (botReply.isNotEmpty()){
            addMessageToList(botReply, true)
        }else{
            Toast.makeText(this, "some went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpBot(){
        // initialize the dialogflow
        try{
            val stream = this.resources.openRawResource(R.raw.credential)
            val credentials: GoogleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
            val projectId: String = (credentials as ServiceAccountCredentials).projectId
            val settingsBuilder: SessionsSettings.Builder = SessionsSettings.newBuilder()
            val sessionsSettings: SessionsSettings = settingsBuilder.setCredentialsProvider(
                FixedCredentialsProvider.create(credentials)
            ).build()
            sessionsClient = SessionsClient.create(sessionsSettings)
            sessionName = SessionName.of(projectId,uuid)
            Log.d(TAG, "projectId: $projectId")
        }catch (e: java.lang.Exception){
            Log.d(TAG, "setUpBot: setUpBpt: "+e.message)
        }
    }

    private fun addMessageToList(message: String, isReceived: Boolean) {
        // handle UI change
        messageList.add(Message(message, isReceived))
        editMessage.setText("")
        chatAdapter.notifyDataSetChanged()
        chatView.layoutManager?.scrollToPosition(messageList.size - 1)
    }
}