package com.example.wallpaper;

import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView; // Correct import
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WallpaperAdapter wallpaperAdapter;
    private static final String API_KEY = "5WUXc0yy9DlX3OoC8uzUynz4uu4tWWtDVHkGvacKBfqVBtmPCG7sycm6";
    private static final String BASE_URL = "https://api.pexels.com/v1/";
    private int currentPage = 1;
    private boolean isLoading = false;
    private SearchView searchView;
    private String query; // Declare query variable
    private List<Photo> photoList; // List to hold the photos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView);

        photoList = new ArrayList<>(); // Initialize the photo list
        loadPhotos(null, currentPage);
        setStatusBarColorToWhite();
        setupSearchView();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentPage = 1; // Reset to the first page for a new search
                loadPhotos(query, currentPage); // Load photos based on query
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optionally, you can implement real-time search as the user types
                return false;
            }
        });
    }

    private void setStatusBarColorToWhite() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.white));

            // To make the status bar text dark (visible on a light background)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void loadPhotos(String query, int page) {
        isLoading = true; // Set loading state
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PexelsApi pexelsApi = retrofit.create(PexelsApi.class);
        Call<PhotoResponse> call;

        // If a query is provided, search for photos
        if (query != null && !query.isEmpty()) {
            this.query = query; // Save query for pagination
            call = pexelsApi.searchPhotos(API_KEY, query, page, 20);
        } else {
            call = pexelsApi.getCuratedPhotos(API_KEY, page, 20);
        }

        call.enqueue(new Callback<PhotoResponse>() {
            @Override
            public void onResponse(Call<PhotoResponse> call, Response<PhotoResponse> response) {
                isLoading = false; // Reset loading state

                if (response.isSuccessful() && response.body() != null) {
                    List<Photo> photos = response.body().getPhotos();
                    if (photos != null && !photos.isEmpty()) {
                        if (currentPage == 1) {
                            photoList.clear(); // Clear the list if it's a new search
                        }
                        photoList.addAll(photos); // Add new photos to the list

                        if (wallpaperAdapter == null) {
                            wallpaperAdapter = new WallpaperAdapter(MainActivity.this, photoList);
                            recyclerView.setAdapter(wallpaperAdapter);
                        } else {
                            wallpaperAdapter.notifyDataSetChanged(); // Notify the adapter of changes
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PhotoResponse> call, Throwable t) {
                isLoading = false; // Reset loading state
                Toast.makeText(MainActivity.this, "Failed to load wallpapers", Toast.LENGTH_SHORT).show();
            }
        });

        // Pagination logic for loading more images
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == photoList.size() - 1) {
                    currentPage++;
                    loadPhotos(query, currentPage); // Pass the query along with the page
                }
            }
        });
    }
}


     class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder> {
        private List<Photo> photos;
        private Context context;
         private List<Photo> photoList;
         private Handler handler = new Handler(Looper.getMainLooper());



         WallpaperAdapter(Context context, List<Photo> photos) {
            this.context = context;
            this.photos = photos;
        }

        @NonNull
        @Override
        public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item, parent, false);
            return new WallpaperViewHolder(view);
        }

         @Override
         public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
             Photo photo = photos.get(position);
             String imageUrl = photo.getSrc().getLarge();
             Log.d("SRC", photo.getSrc().toString());

             // Load image using Picasso
             Picasso.get().load(imageUrl).into(holder.imageView);

             // Set onClick listener for the imageView to set wallpaper
             holder.setWallpaperButton.setOnClickListener(v -> {
                 // Load the bitmap from the URL and set as wallpaper
                 new Thread(() -> {
                     try {
                         // Load large image for wallpaper
                         Bitmap bitmap = Picasso.get().load(imageUrl).get();
                         WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                         wallpaperManager.setBitmap(bitmap);

                         // Use handler to post the Toast message back to the main thread
                         handler.post(() -> Toast.makeText(context, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show());
                     } catch (IOException e) {
                         handler.post(() -> Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_SHORT).show());
                         e.printStackTrace();
                     }
                 }).start();
             });

             // Download button functionality
             holder.downloadButton.setOnClickListener(v -> DownloadImage(imageUrl));

             // Share button functionality
             holder.shareButton.setOnClickListener(v -> shareImage(imageUrl));

             // Set onClick listener for the imageView to open FullScreenActivity
             holder.imageView.setOnClickListener(v -> {
                 Intent intent = new Intent(context, FullScreenActivity.class);
                 intent.putExtra("imageUrl", imageUrl); // Pass the image URL to the new activity
                 context.startActivity(intent);
             });
         }




         private void DownloadImage(String imageUrl) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
            request.setTitle("Downloading High-Quality Image");
            request.setDescription("High-resolution image is being downloaded");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Save the high-quality image in the Pictures directory
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "high_quality_image.jpg");

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(context, "Downloaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Fail to Download.", Toast.LENGTH_SHORT).show();
            }
        }


        private void shareImage(String imageUrl) {
            // First, download the image to a file
            Picasso.get().load(imageUrl).into(new com.squareup.picasso.Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        // Save the image to the Pictures directory
                        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "shared_image.jpg");
                        FileOutputStream outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();

                        // Get URI for the saved file using FileProvider
                        Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

                        // Create intent to share the image
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/jpeg");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this wallpaper!");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Start the share intent
                        context.startActivity(Intent.createChooser(shareIntent, "Share Image"));

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Failed to share image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Toast.makeText(context, "Failed to load image for sharing", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // You can show a progress indicator while loading the image if necessary
                }
            });
        }


        @Override
        public int getItemCount() {
            return photos.size();
        }

        public void addPhotos(List<Photo> newPhotos) {
            photos.addAll(newPhotos);
            notifyDataSetChanged();
        }

        class WallpaperViewHolder extends RecyclerView.ViewHolder {
            public ImageButton downloadButton;
            public ImageButton shareButton;
            public ImageButton setWallpaperButton;
            ImageView imageView;

            WallpaperViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.wallpaperImage);
                downloadButton = itemView.findViewById(R.id.downloadButton);
                shareButton = itemView.findViewById(R.id.shareButton);
                setWallpaperButton = itemView.findViewById(R.id.setWallpaperButton);
            }
        }
    }

