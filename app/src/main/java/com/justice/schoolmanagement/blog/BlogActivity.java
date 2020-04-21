package com.justice.schoolmanagement.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.justice.schoolmanagement.R;

import weborb.reader.BooleanReader;

public class BlogActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        initWidgets();
        setUpRecyclerView();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        fob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BlogActivity.this, AddBlogActivity.class));
            }
        });
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        Query query = FirebaseFirestore.getInstance().collection("blogs").orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Blog> recyclerOptions = new FirestoreRecyclerOptions.Builder<Blog>().setLifecycleOwner(this).setQuery(query, new SnapshotParser<Blog>() {
            @NonNull
            @Override
            public Blog parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                Blog blog = snapshot.toObject(Blog.class);
                blog.setId(snapshot.getId());
                return blog;
            }
        }).build();

        BlogRecyclerAdapter adapter = new BlogRecyclerAdapter(this, recyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initWidgets() {
        fob = findViewById(R.id.fob);
    }
}
