package com.ques_4_ass;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private final List<ImageItem> imageItems = new ArrayList<>();
    private RecyclerView rvImages;
    private TextView tvGalleryFolder;
    private Uri folderUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        rvImages = findViewById(R.id.rvImages);
        tvGalleryFolder = findViewById(R.id.tvGalleryFolder);

        String folderUriString = getIntent().getStringExtra("folder_uri");
        if (folderUriString == null) {
            Toast.makeText(this, "No folder selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        folderUri = Uri.parse(folderUriString);
        tvGalleryFolder.setText("Folder: " + folderUri);

        rvImages.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadImages();
    }

    private void loadImages() {
        imageItems.clear();
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder == null || !folder.canRead()) {
            Toast.makeText(this, "Cannot read selected folder", Toast.LENGTH_SHORT).show();
            return;
        }

        for (DocumentFile file : folder.listFiles()) {
            if (file.isFile() && file.getType() != null && file.getType().startsWith("image/")) {
                imageItems.add(
                        new ImageItem(
                                file.getUri(),
                                file.getName() == null ? "Unknown" : file.getName(),
                                file.length(),
                                file.lastModified()
                        )
                );
            }
        }

        ImageAdapter adapter = new ImageAdapter(imageItems, this::openDetails);
        rvImages.setAdapter(adapter);
    }

    private void openDetails(ImageItem imageItem) {
        Intent intent = new Intent(this, ImageDetailActivity.class);
        intent.putExtra("image_uri", imageItem.uri.toString());
        intent.putExtra("image_name", imageItem.name);
        intent.putExtra("image_size", imageItem.size);
        intent.putExtra("image_last_modified", imageItem.lastModified);
        startActivity(intent);
    }
}
