package com.ques_4_ass;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String PREFS = "photo_prefs";
    public static final String KEY_FOLDER_URI = "selected_folder_uri";

    private TextView tvSelectedFolder;
    private Uri selectedFolderUri;
    private Uri tempCameraUri;

    private ActivityResultLauncher<Intent> folderPickerLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvSelectedFolder = findViewById(R.id.tvSelectedFolder);
        Button btnChooseFolder = findViewById(R.id.btnChooseFolder);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        Button btnOpenGallery = findViewById(R.id.btnOpenGallery);

        setupLaunchers();
        loadSavedFolderUri();

        btnChooseFolder.setOnClickListener(v -> chooseFolder());
        btnTakePhoto.setOnClickListener(v -> startCaptureFlow());
        btnOpenGallery.setOnClickListener(v -> openGallery());
    }

    private void setupLaunchers() {
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri treeUri = result.getData().getData();
                        if (treeUri != null) {
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                            selectedFolderUri = treeUri;
                            getSharedPreferences(PREFS, MODE_PRIVATE)
                                    .edit()
                                    .putString(KEY_FOLDER_URI, treeUri.toString())
                                    .apply();
                            tvSelectedFolder.setText(treeUri.toString());
                        }
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        saveCapturedImageIntoSelectedFolder();
                    } else {
                        Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void loadSavedFolderUri() {
        String uriString = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_FOLDER_URI, null);
        if (uriString != null) {
            selectedFolderUri = Uri.parse(uriString);
            tvSelectedFolder.setText(selectedFolderUri.toString());
        } else {
            tvSelectedFolder.setText(R.string.no_folder_selected);
        }
    }

    private void chooseFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        folderPickerLauncher.launch(intent);
    }

    private void startCaptureFlow() {
        if (selectedFolderUri == null) {
            Toast.makeText(this, "Please choose a folder first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }
        launchCamera();
    }

    private void launchCamera() {
        try {
            File imagesDir = new File(getCacheDir(), "images");
            if (!imagesDir.exists() && !imagesDir.mkdirs()) {
                Toast.makeText(this, "Unable to create cache directory", Toast.LENGTH_SHORT).show();
                return;
            }
            File tempFile = new File(imagesDir, "capture_" + System.currentTimeMillis() + ".jpg");
            tempCameraUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    tempFile
            );
            takePictureLauncher.launch(tempCameraUri);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to launch camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCapturedImageIntoSelectedFolder() {
        if (selectedFolderUri == null || tempCameraUri == null) {
            return;
        }
        try {
            DocumentFile targetFolder = DocumentFile.fromTreeUri(this, selectedFolderUri);
            if (targetFolder == null || !targetFolder.canWrite()) {
                Toast.makeText(this, "Cannot write to selected folder", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date()) + ".jpg";
            DocumentFile createdImage = targetFolder.createFile("image/jpeg", fileName);
            if (createdImage == null) {
                Toast.makeText(this, "Could not create file in folder", Toast.LENGTH_SHORT).show();
                return;
            } else {
                createdImage.getUri();
            }

            ContentResolver resolver = getContentResolver();
            try (InputStream inputStream = resolver.openInputStream(tempCameraUri);
                 OutputStream outputStream = resolver.openOutputStream(createdImage.getUri())) {
                if (inputStream == null || outputStream == null) {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }

            Toast.makeText(this, "Image saved to selected folder", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed saving photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        if (selectedFolderUri == null) {
            Toast.makeText(this, "Please choose a folder first", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra("folder_uri", selectedFolderUri.toString());
        startActivity(intent);
    }
}