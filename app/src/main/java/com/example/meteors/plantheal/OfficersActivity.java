package com.example.meteors.plantheal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.HashMap;
import java.util.Map;

public class OfficersActivity extends AppCompatActivity {

    private RecyclerView mOfficersRecyclerView;
    private FirebaseFirestore mFirestore;
    private OfficersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officers);

        // Create a new collection and document in Firestore
        mFirestore = FirebaseFirestore.getInstance();
        Map<String, Object> officer = new HashMap<>();
        officer.put("name", "Raja");
        officer.put("location", "Dindigul");
        officer.put("is_available", true);
        officer.put("phoneNumber", "+916380802971");
        mFirestore.collection("officers").document("officer_1").set(officer);

        mFirestore = FirebaseFirestore.getInstance();
        Map<String, Object> officer1 = new HashMap<>();
        officer.put("name", "Guru");
        officer.put("location", "Dindigul");
        officer.put("is_available", true);
        officer.put("phoneNumber", "+919025274161");
        mFirestore.collection("officers").document("officer_2").set(officer);



        mOfficersRecyclerView = findViewById(R.id.recycler_view_officers);

        // Set up the RecyclerView
        mOfficersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the data from Firestore
        Query query = mFirestore.collection("officers").whereEqualTo("is_available", true);
        FirestoreRecyclerOptions<Officer> options = new FirestoreRecyclerOptions.Builder<Officer>()
                .setQuery(query, Officer.class)
                .build();
        mAdapter = new OfficersAdapter(options);
        mOfficersRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private class OfficersAdapter extends FirestoreRecyclerAdapter<Officer, OfficersAdapter.ViewHolder> {

        public OfficersAdapter(FirestoreRecyclerOptions<Officer> options) {
            super(options);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.officer_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        protected void onBindViewHolder(ViewHolder holder, int position, Officer officer) {
            holder.mNameTextView.setText(officer.getName());
            // holder.mLocationTextView.setText(officer.getLocation());
            final String phoneNumber = officer.getPhoneNumber();
            holder.mChatImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text= Hi!";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    holder.itemView.getContext().startActivity(i);
                }
            });
            holder.mAudioCallImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text= Hi!";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    holder.itemView.getContext().startActivity(i);
                }
            });
            holder.mVideoCallImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text= Hi!";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    holder.itemView.getContext().startActivity(i);
                }
            });
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mNameTextView;
            private TextView mLocationTextView;
            private TextView mChatImageView;
            private TextView mAudioCallImageView;
            private TextView mVideoCallImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                mNameTextView = itemView.findViewById(R.id.officer_name);
                mChatImageView = itemView.findViewById(R.id.chat_icon);
                mAudioCallImageView = itemView.findViewById(R.id.audio_call_icon);
                mVideoCallImageView = itemView.findViewById(R.id.video_call_icon);
            }
        }
    }
}


