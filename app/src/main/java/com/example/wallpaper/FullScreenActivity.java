package com.example.wallpaper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;

public class FullScreenActivity extends AppCompatActivity {

    private ZoomageView myZoomageView;
    private ImageButton backButton;
    private TextView backTextView;
    private String imageUrl; // URL for the original image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        myZoomageView = findViewById(R.id.myZoomageView);
        backButton = findViewById(R.id.backButton);
        backTextView = findViewById(R.id.backTextView);
        setStatusBarColorToWhite();

        // Get the image URL from the Intent
        imageUrl = getIntent().getStringExtra("imageUrl");
        if (imageUrl != null) {
            // Load the image using Picasso with error handling
            Picasso.get()
                    .load(imageUrl)
                    .into(myZoomageView);
        }

        // Set onClick listeners for back navigation
        View.OnClickListener backClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMainActivity();
            }
        };

        backButton.setOnClickListener(backClickListener);
        backTextView.setOnClickListener(backClickListener);
    }

    public void navigateToMainActivity() {
        Intent intent = new Intent(FullScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Optional: remove this activity from the back stack
    }

    private void setStatusBarColorToWhite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // Use lightGray color

            // To make the status bar text dark (visible on a light background)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); //
    }
}
