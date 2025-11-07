package com.example.robotic_events_test;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EventDetailActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ImageView image   = findViewById(R.id.detailImage);
        TextView title    = findViewById(R.id.detailTitle);
        TextView when     = findViewById(R.id.detailWhen);
        TextView where    = findViewById(R.id.detailWhere);
        TextView price    = findViewById(R.id.detailPrice);
//        TextView status   = findViewById(R.id.detailStatus);
        TextView category = findViewById(R.id.detailCategory);
        TextView org      = findViewById(R.id.detailOrganizer);
        TextView cap      = findViewById(R.id.detailCapacity);
        TextView desc     = findViewById(R.id.detailDescription);

        String id          = getIntent().getStringExtra("id");
        String t           = safe(getIntent().getStringExtra("title"));
        String d           = safe(getIntent().getStringExtra("description"));
        long dateTime      = getIntent().getLongExtra("dateTime", 0L);
        String loc         = safe(getIntent().getStringExtra("location"));
        String cat         = safe(getIntent().getStringExtra("category"));
        String organizerId = safe(getIntent().getStringExtra("organizerId"));
        int totalCapacity  = getIntent().getIntExtra("totalCapacity", 0);
        String st          = safe(getIntent().getStringExtra("status"));
        int imgResId       = getIntent().getIntExtra("imageResId", 0);
        double pr          = getIntent().getDoubleExtra("price", 0.0);

        if (imgResId != 0) image.setImageResource(imgResId);
        title.setText(t.isEmpty() ? "(Untitled Event)" : t);
        when.setText(dateTime > 0 ? sdf.format(new Date(dateTime)) : "");
        where.setText(loc);
        price.setText(pr > 0 ? String.format(Locale.getDefault(), "$%.2f", pr) : "Free");
//        status.setText(st);
        cap.setText(totalCapacity > 0 ? String.format(Locale.getDefault(),
                "Capacity\n%d", totalCapacity) : "Unlimited");

        // Get parent
        ViewGroup parent = (ViewGroup) desc.getParent();

        // Delete empty views
        if (organizerId.isEmpty()) { parent.removeView(org); }

        if (d.isEmpty()) { parent.removeView(desc); }

        if (cat.isEmpty()) { parent.removeView(category); }

        org.setText(organizerId);
        desc.setText(d);
        category.setText(cat);

        // Get toolbar; allow navigation back to home page
        MaterialToolbar toolbar = findViewById(R.id.eventDetailToolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> { getOnBackPressedDispatcher().onBackPressed(); });
    }

    private String safe(String s) { return s == null ? "" : s; }
}
