package com.ques_2_ass;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private enum MediaType { NONE, AUDIO, VIDEO }

    private VideoView videoView;
    private EditText etUrl;
    private MediaPlayer mediaPlayer;
    private Uri currentAudioUri;
    private Uri currentVideoUri;
    private MediaType currentMediaType = MediaType.NONE;

    private final ActivityResultLauncher<String[]> audioPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                currentAudioUri = uri;
                currentMediaType = MediaType.AUDIO;
                prepareAudioPlayer();
                toast("Audio file selected");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        videoView = findViewById(R.id.videoView);
        etUrl = findViewById(R.id.etUrl);
        Button btnOpenFile = findViewById(R.id.btnOpenFile);
        Button btnOpenUrl = findViewById(R.id.btnOpenUrl);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnRestart = findViewById(R.id.btnRestart);

        videoView.setMediaController(new MediaController(this));

        btnOpenFile.setOnClickListener(v -> audioPickerLauncher.launch(new String[]{"audio/*"}));

        btnOpenUrl.setOnClickListener(v -> openVideoFromUrl());
        btnPlay.setOnClickListener(v -> playMedia());
        btnPause.setOnClickListener(v -> pauseMedia());
        btnStop.setOnClickListener(v -> stopMedia());
        btnRestart.setOnClickListener(v -> restartMedia());
    }

    private void openVideoFromUrl() {
        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            toast("Please enter a video URL");
            return;
        }
        releaseAudioPlayer();
        currentVideoUri = Uri.parse(url);
        currentMediaType = MediaType.VIDEO;
        videoView.setVideoURI(currentVideoUri);
        videoView.requestFocus();
        toast("Video URL loaded");
    }

    private void prepareAudioPlayer() {
        if (currentAudioUri == null) return;
        releaseAudioPlayer();
        try {
            mediaPlayer = MediaPlayer.create(this, currentAudioUri);
            if (mediaPlayer == null) {
                toast("Unable to load audio");
            }
        } catch (Exception e) {
            toast("Error opening audio file");
            mediaPlayer = null;
        }
    }

    private void playMedia() {
        if (currentMediaType == MediaType.AUDIO) {
            if (mediaPlayer == null && currentAudioUri != null) {
                prepareAudioPlayer();
            }
            if (mediaPlayer != null) {
                mediaPlayer.start();
            } else {
                toast("Open an audio file first");
            }
            return;
        }

        if (currentMediaType == MediaType.VIDEO) {
            if (currentVideoUri == null) {
                toast("Open a video URL first");
                return;
            }
            if (videoView.getDuration() <= 0) {
                videoView.setVideoURI(currentVideoUri);
            }
            videoView.start();
            return;
        }

        toast("Open a file or URL first");
    }

    private void pauseMedia() {
        if (currentMediaType == MediaType.AUDIO) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } else if (currentMediaType == MediaType.VIDEO) {
            if (videoView.isPlaying()) {
                videoView.pause();
            }
        }
    }

    private void stopMedia() {
        if (currentMediaType == MediaType.AUDIO) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                prepareAudioPlayer();
            }
        } else if (currentMediaType == MediaType.VIDEO) {
            if (currentVideoUri != null) {
                videoView.stopPlayback();
                videoView.setVideoURI(currentVideoUri);
            }
        }
    }

    private void restartMedia() {
        if (currentMediaType == MediaType.AUDIO) {
            if (mediaPlayer == null && currentAudioUri != null) {
                prepareAudioPlayer();
            }
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            }
        } else if (currentMediaType == MediaType.VIDEO) {
            if (currentVideoUri == null) return;
            videoView.seekTo(0);
            videoView.start();
        }
    }

    private void releaseAudioPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAudioPlayer();
    }
}