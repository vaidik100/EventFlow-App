package com.example.eventflow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eventflow.network.ImgurApiService;
import com.example.eventflow.network.ImgurResponse;
import com.example.eventflow.network.RetrofitClient;
import com.example.eventflow.util.BitmapUtils;
import com.example.eventflow.util.QRCodeGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EventDetailActivity extends AppCompatActivity {

    private final String TAG = "EVENT_DETAIL";
    private ImageView eventImage;
    private TextView textTitle, textDateTime, textLocation;
    private Button btnBuyTicket;
    private String eventId, name, date, time, location, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventImage = findViewById(R.id.eventDetailImage);
        textTitle = findViewById(R.id.textDetailTitle);
        textDateTime = findViewById(R.id.textDetailDateTime);
        textLocation = findViewById(R.id.textDetailLocation);
        btnBuyTicket = findViewById(R.id.btnBuyTicket);

        try {
            eventId = getIntent().getStringExtra("eventId");
            name = getIntent().getStringExtra("name");
            date = getIntent().getStringExtra("date");
            time = getIntent().getStringExtra("time");
            location = getIntent().getStringExtra("location");
            imageUrl = getIntent().getStringExtra("imageUrl");

            if (eventId == null || name == null || date == null || time == null || location == null || imageUrl == null) {
                Log.e(TAG, "Missing event data in intent");
                Toast.makeText(this, "Incomplete event details", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Event received: " + name + ", " + date + ", " + time);

            textTitle.setText(name);
            textDateTime.setText(date + " - " + time);
            textLocation.setText(location);
            Glide.with(this).load(imageUrl).into(eventImage);

        } catch (Exception e) {
            Log.e(TAG, "Error loading event details: " + e.getMessage());
            Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBuyTicket.setOnClickListener(v -> buyTicket());
    }

    private void buyTicket() {
        Log.d(TAG, "Buy Ticket button clicked");

        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String ticketId = UUID.randomUUID().toString();

            // 1. Create QR code content
            String qrContent = "ticketId:" + ticketId + "|eventId:" + eventId + "|userId:" + userId;

            // 2. Generate QR code bitmap
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrContent, 400);

            // 3. Save bitmap as file
            File qrFile = BitmapUtils.saveBitmapToCache(this, qrBitmap, "qr_" + ticketId + ".png");

            // 4. Upload QR image to Imgur
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), qrFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", qrFile.getName(), requestFile);


            ImgurApiService apiService = RetrofitClient.getInstance().create(ImgurApiService.class);
            Call<ImgurResponse> call = apiService.uploadImage(body);

            call.enqueue(new Callback<ImgurResponse>() {
                @Override
                public void onResponse(Call<ImgurResponse> call, Response<ImgurResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        String qrUrl = response.body().data.link;

                        // 5. Save ticket data to Firestore
                        Map<String, Object> ticket = new HashMap<>();
                        ticket.put("ticketId", ticketId);
                        ticket.put("userId", userId);
                        ticket.put("eventId", eventId);
                        ticket.put("eventName", name);
                        ticket.put("date", date);
                        ticket.put("time", time);
                        ticket.put("location", location);
                        ticket.put("qrUrl", qrUrl);
                        ticket.put("timestamp", System.currentTimeMillis());

                        FirebaseFirestore.getInstance()
                                .collection("tickets")
                                .document(ticketId)
                                .set(ticket)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Ticket booked successfully with QR: " + qrUrl);
                                    Toast.makeText(EventDetailActivity.this, "Ticket booked! 🎟️", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Firestore save failed: " + e.getMessage());
                                    Toast.makeText(EventDetailActivity.this, "Failed to save ticket", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Log.e(TAG, "Imgur upload failed: " + response.message());
                        Toast.makeText(EventDetailActivity.this, "Failed to upload QR image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ImgurResponse> call, Throwable t) {
                    Log.e(TAG, "Imgur upload error: " + t.getMessage());
                    Toast.makeText(EventDetailActivity.this, "QR upload failed", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException | WriterException e) {
            Log.e(TAG, "QR Generation Error: " + e.getMessage());
            Toast.makeText(this, "QR code generation failed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Ticket booking failed: " + e.getMessage());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

}
