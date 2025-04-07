package com.example.eventflow;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventflow.network.ImgurApiService;
import com.example.eventflow.network.ImgurResponse;
import com.example.eventflow.network.RetrofitClient;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CREATE_EVENT";

    private EditText editEventName, editEventDate, editEventTime, editEventLocation, editEventCapacity;
    private Button btnPublish, btnDelete, btnUploadImage;
    private ImageView eventImagePreview;
    private Uri imageUri;
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Log.d(TAG, "Image selected: " + imageUri.toString());
                        eventImagePreview.setImageURI(imageUri);
                    } else {
                        Log.e(TAG, "Image selection returned null URI.");
                        Toast.makeText(this, "Image selection failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Image selection cancelled or failed.");
                }
            }
    );
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createevent);

        Log.d(TAG, "Initializing UI components...");
        db = FirebaseFirestore.getInstance();

        editEventName = findViewById(R.id.editEventName);
        editEventDate = findViewById(R.id.editEventDate);
        editEventTime = findViewById(R.id.editEventTime);
        editEventLocation = findViewById(R.id.editEventLocation);
        editEventCapacity = findViewById(R.id.editEventCapacity);
        btnPublish = findViewById(R.id.btnPublish);
        btnDelete = findViewById(R.id.btnDelete);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        eventImagePreview = findViewById(R.id.eventImagePreview);

        ImageView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            Log.d(TAG, "Close button clicked.");
            finish();
        });

        editEventDate.setOnClickListener(v -> showDatePicker());
        editEventTime.setOnClickListener(v -> showTimePicker());

        btnUploadImage.setOnClickListener(v -> selectImage());
        btnPublish.setOnClickListener(v -> saveEventToFirestore());
        btnDelete.setOnClickListener(v -> {
            Log.d(TAG, "Delete button clicked - going back.");
            finish();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting READ_EXTERNAL_STORAGE permission.");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            Log.d(TAG, "Date picked: " + date);
            editEventDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute);
            Log.d(TAG, "Time picked: " + time);
            editEventTime.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void selectImage() {
        Log.d(TAG, "Opening image picker...");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveEventToFirestore() {
        Log.d(TAG, "Saving event to Firestore...");

        String eventName = editEventName.getText().toString().trim();
        String eventDate = editEventDate.getText().toString().trim();
        String eventTime = editEventTime.getText().toString().trim();
        String eventLocation = editEventLocation.getText().toString().trim();
        String eventCapacity = editEventCapacity.getText().toString().trim();

        if (eventName.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty() ||
                eventLocation.isEmpty() || eventCapacity.isEmpty() || imageUri == null) {
            Log.e(TAG, "Validation failed - Missing field(s).");
            Toast.makeText(this, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "Converting imageUri to temp file...");
            File imageFile = getFileFromUri(imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

            Log.d(TAG, "Uploading image to Imgur...");
            ImgurApiService apiService = RetrofitClient.getInstance().create(ImgurApiService.class);
            Call<ImgurResponse> call = apiService.uploadImage(body);

            call.enqueue(new Callback<ImgurResponse>() {
                @Override
                public void onResponse(Call<ImgurResponse> call, Response<ImgurResponse> response) {
                    Log.d(TAG, "Received Imgur response.");
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        String imageUrl = response.body().data.link;
                        Log.d(TAG, "Imgur image URL: " + imageUrl);

                        Map<String, Object> event = new HashMap<>();
                        event.put("name", eventName);
                        event.put("date", eventDate);
                        event.put("time", eventTime);
                        event.put("location", eventLocation);
                        event.put("capacity", eventCapacity);
                        event.put("imageUrl", imageUrl);
                        event.put("timestamp", System.currentTimeMillis());

                        db.collection("events")
                                .add(event)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Event saved to Firestore with ID: " + documentReference.getId());
                                    Toast.makeText(CreateEventActivity.this, "Event created!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Firestore save failed: " + e.getMessage());
                                    Toast.makeText(CreateEventActivity.this, "Error saving event", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Log.e(TAG, "Imgur upload failed: " + response.message());
                        Toast.makeText(CreateEventActivity.this, "Imgur upload failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ImgurResponse> call, Throwable t) {
                    Log.e(TAG, "Imgur upload error: " + t.getMessage());
                    t.printStackTrace();
                    Toast.makeText(CreateEventActivity.this, "Imgur upload failed", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Image conversion failed: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        Log.d(TAG, "Reading input stream from imageUri...");
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        inputStream.close();
        Log.d(TAG, "Temporary file created: " + tempFile.getAbsolutePath());
        return tempFile;
    }
}
