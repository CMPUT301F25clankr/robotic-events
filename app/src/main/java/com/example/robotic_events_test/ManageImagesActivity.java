package com.example.robotic_events_test;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;


/** lets the admin see all event images that were uploaded and allows for deletion*/
public class ManageImagesActivity extends AppCompatActivity {

    private static final String TAG = "ManageImagesActivity";
    private FirebaseFirestore db;
    private RecyclerView imagesRecyclerView;
    private ImageAdapter adapter;
    private List<ImageItem> imageItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_images);

        db = FirebaseFirestore.getInstance();

        //Toolbar
        MaterialToolbar toolbar = findViewById(R.id.manageImagesToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //recyclerView
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        adapter = new ImageAdapter(this, imageItems);
        imagesRecyclerView.setAdapter(adapter);

        fetchEventImages();
        setupDeleteListener();
    }


    //getting all event image urls that are not null
    private void fetchEventImages() {
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imageItems.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String imageUrl = document.getString("imageUrl");
                    // Check if imageUrl is not null and not empty
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        String eventId = document.getId();
                        imageItems.add(new ImageItem(eventId, imageUrl));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
                Toast.makeText(this, "Error loading images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //on clicking delete set image url to null
    private void setupDeleteListener() {
        adapter.setOnDeleteClickListener(position -> {
            ImageItem itemToDelete = imageItems.get(position);
            String eventId = itemToDelete.getEventId();

            // Update db to set null image url
            db.collection("events").document(eventId)
                    .update("imageUrl", null)
                    .addOnSuccessListener(aVoid -> {
                        // On success, remove from list and notify adapter
                        imageItems.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, imageItems.size());
                        Toast.makeText(ManageImagesActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting image reference", e);
                        Toast.makeText(ManageImagesActivity.this, "Failed to remove image", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
