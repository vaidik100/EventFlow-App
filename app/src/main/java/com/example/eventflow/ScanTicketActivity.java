package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanTicketActivity extends AppCompatActivity {

    private static final String TAG = "SCAN_TICKET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan QR Code");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            Log.d(TAG, "QR scanned: " + result.getContents());

            // Parse ticketId from result
            String raw = result.getContents();
            String ticketId = extractTicketId(raw);

            if (ticketId != null) {
                Intent intent = new Intent(this, VerifyTicketActivity.class);
                intent.putExtra("ticketId", ticketId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid QR format", Toast.LENGTH_SHORT).show();
                finish();
            }

        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String extractTicketId(String content) {
        try {
            for (String part : content.split("\\|")) {
                if (part.startsWith("ticketId:")) {
                    return part.split("ticketId:")[1];
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing ticket ID: " + e.getMessage());
        }
        return null;
    }
}
