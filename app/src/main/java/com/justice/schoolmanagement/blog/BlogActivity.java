package com.justice.schoolmanagement.blog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.justice.schoolmanagement.R;

import es.dmoral.toasty.Toasty;

public class BlogActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fob;
    private BlogRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        initWidgets();
        setUpRecyclerView();
        setOnClickListeners();
        setSwipeToDelete();
    }

    private void setSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                new MaterialAlertDialogBuilder(BlogActivity.this).setBackground(getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }).setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBlog(adapter.getBlogReferenceByPosition(viewHolder.getAdapterPosition()));
                    }
                }).show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void deleteBlog(DocumentReference blogReferenceByPosition) {
        blogReferenceByPosition.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(BlogActivity.this, "Deletion success", Toasty.LENGTH_SHORT).show();

                } else {
                    Toasty.error(BlogActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT).show();
                }
            }
        });
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

        adapter = new BlogRecyclerAdapter(this, recyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initWidgets() {
        fob = findViewById(R.id.fob);
    }
}
