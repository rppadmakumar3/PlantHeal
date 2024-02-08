package com.example.meteors.plantheal;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import io.kommunicate.KmConversationBuilder;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KmCallback;

public class BotActivity extends AppCompatActivity {

    private String appId = "11f755e6babb014d23d8862aa634c39ca"; // Replace with your Kommunicate App ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);

        // Initialize the Kommunicate SDK
        Kommunicate.init(this, appId);
        // Create a new conversation
        Button startChatButton = findViewById(R.id.start_chat_button);
        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KmConversationBuilder conversationBuilder = new KmConversationBuilder(BotActivity.this);
                conversationBuilder.launchConversation(new KmCallback() {
                    @Override
                    public void onSuccess(Object message) {
                        Log.d("Conversation", "Success : " + message);
                    }

                    @Override
                    public void onFailure(Object error) {
                        Log.e("Conversation", "Error : " + error);
                    }
                });
            }
        });
    }
}