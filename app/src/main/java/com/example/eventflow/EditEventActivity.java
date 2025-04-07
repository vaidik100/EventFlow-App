package com.example.eventflow;

import static android.content.Intent.getIntent;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eventflow.network.ImgurApiService;
import com.example.eventflow.network.ImgurResponse;
import com.example.eventflow.network.RetrofitClient;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEventActivity extends AppCompatActivity {

    private static final String TAG = "EDIT_EVENT";

    private EditText editName, editDate, editTime, editLocation;
    private ImageView eventImage;
    private Button btnUpdate, btnPickImage;
    private Uri selectedImageUri = null;
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    eventImage.setImageURI(selectedImageUri);
                }
            });
    private String eventId, existingImageUrl;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        db = FirebaseFirestore.getInstance();

        editName = findViewById(R.id.editEventName);
        editDate = findViewById(R.id.editEventDate);
        editTime = findViewById(R.id.editEventTime);
        editLocation = findViewById(R.id.editEventLocation);
        eventImage = findViewById(R.id.eventImagePreview);
        btnUpdate = findViewById(R.id.btnUpdateEvent);
        btnPickImage = findViewById(R.id.btnPickImage);

        // Get passed event data
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        editName.setText(intent.getStringExtra("name"));
        editDate.setText(intent.getStringExtra("date"));
        editTime.setText(intent.getStringExtra("time"));
        editLocation.setText(intent.getStringExtra("location"));
        existingImageUrl = intent.getStringExtra("imageUrl");

        Glide.with(this).load(existingImageUrl).into(eventImage);

        editDate.setOnClickListener(v -> showDatePicker());
        editTime.setOnClickListener(v -> showTimePicker());

        btnPickImage.setOnClickListener(v -> pickImage());

        btnUpdate.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageAndUpdate();
            } else {
                updateEvent(existingImageUrl);
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = day + "/" + (month + 1) + "/" + year;
            editDate.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, min) -> {
            String time = String.format("%02d:%02d", hour, min);
            editTime.setText(time);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageAndUpdate() {
        try {
            File imageFile = getFileFromUri(selectedImageUri);
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), reqFile);

            ImgurApiService apiService = RetrofitClient.getInstance().create(ImgurApiService.class);
            Call<ImgurResponse> call = apiService.uploadImage(body);

            call.enqueue(new Callback<ImgurResponse>() {
                @Override
                public void onResponse(Call<ImgurResponse> call, Response<ImgurResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        String newImageUrl = response.body().data.link;
                        updateEvent(newImageUrl);
                    } else {
                        Toast.makeText(EditEventActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ImgurResponse> call, Throwable t) {
                    Toast.makeText(EditEventActivity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEvent(String imageUrl) {
        String name = editName.getText().toString().trim();
        String date = editDate.getText().toString().trim();
        String time = editTime.getText().toString().trim();
        String location = editLocation.getText().toString().trim();

        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("name", name);
        updatedEvent.put("date", date);
        updatedEvent.put("time", time);
        updatedEvent.put("location", location);
        updatedEvent.put("imageUrl", imageUrl);

        db.collection("events").document(eventId)
                .update(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private File getFileFromUri(Uri uri) throws Exception {
        InputStream input = getContentResolver().openInputStream(uri);
        File file = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > 0) {
            output.write(buffer, 0, len);
        }
        output.close();
        input.close();
        return file;
    }
}
