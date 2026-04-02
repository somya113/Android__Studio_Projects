package com.ques_4_ass;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        ImageView ivDetail = findViewById(R.id.ivDetail);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPath = findViewById(R.id.tvPath);
        TextView tvSize = findViewById(R.id.tvSize);
        TextView tvDate = findViewById(R.id.tvDate);
        Button btnDelete = findViewById(R.id.btnDelete);

        String imageUriString = getIntent().getStringExtra("image_uri");
        if (imageUriString == null) {
            Toast.makeText(this, "Invalid image", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imageUri = Uri.parse(imageUriString);
        String name = getIntent().getStringExtra("image_name");
        long size = getIntent().getLongExtra("image_size", 0L);
        long lastModified = getIntent().getLongExtra("image_last_modified", 0L);

        ivDetail.setImageURI(imageUri);
        tvName.setText("Name: " + (name == null ? "Unknown" : name));
        tvPath.setText("Path: " + imageUri.toString());
        tvSize.setText("Size: " + size + " bytes");
        tvDate.setText("Date Taken: " + formatDate(lastModified));

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private String formatDate(long millis) {
        if (millis <= 0) {
            return "Unknown";
        }
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(millis));
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", this::deleteImage)
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage(DialogInterface dialogInterface, int which) {
        DocumentFile imageDoc = DocumentFile.fromSingleUri(this, imageUri);
        if (imageDoc != null && imageDoc.delete()) {
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }
}
