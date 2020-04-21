package com.justice.schoolmanagement.blog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.results.ResultsActivityRecyclerAdapter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends FirestoreRecyclerAdapter<Blog, BlogRecyclerAdapter.ViewHolder> {
    private Context context;


    public BlogRecyclerAdapter(Context context, @NonNull FirestoreRecyclerOptions<Blog> options) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Blog model) {
        FirebaseFirestore.getInstance().collection("Teachers").document(model.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Glide.with(context).load(documentSnapshot.getString("photo")).into(holder.userProfileImageView);
                holder.userNameTxtView.setText(documentSnapshot.getString("firstName"));
                Toast.makeText(context, "Success loading user Data", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        final String date = new SimpleDateFormat("dd/MM/yy : HH:mm:ss").format(model.getDate());
        holder.dateTxtView.setText(date);

        Glide.with(context).load(model.getPhoto()).into(holder.postImageView);
        holder.descriptionTxtView.setText(model.getDescription());

        ///////////////////////////////////////////
        FirebaseFirestore.getInstance().collection("blogs").document(model.getId()).collection("likes").document(FirebaseAuth.getInstance().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    holder.hasLiked = true;
                    holder.likeImageView.setImageDrawable(context.getDrawable(R.drawable.ic_like));
                } else {
                    holder.hasLiked = false;
                    holder.likeImageView.setImageDrawable(context.getDrawable(R.drawable.ic_unlike));
                }
            }
        });
holder.likeImageView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if(holder.hasLiked){
            holder.hasLiked = false;
            holder.likeImageView.setImageDrawable(context.getDrawable(R.drawable.ic_unlike));
            FirebaseFirestore.getInstance().collection("blogs").document(model.getId()).collection("likes").document(FirebaseAuth.getInstance().getUid()).delete();

        }else {
            Map<String, Object> map = new HashMap<>();
            map.put("data", "data");
            holder.hasLiked = true;
            holder.likeImageView.setImageDrawable(context.getDrawable(R.drawable.ic_like));
            FirebaseFirestore.getInstance().collection("blogs").document(model.getId()).collection("likes").document(FirebaseAuth.getInstance().getUid()).set(map);

        }
    }
});

        ///////////////////////////////////////////


        FirebaseFirestore.getInstance().collection("blogs").document(model.getId()).collection("likes").addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (queryDocumentSnapshots.isEmpty()) {
                    holder.numberOfLikesTxtView.setText("0 Likes");
                } else {
                    holder.numberOfLikesTxtView.setText(queryDocumentSnapshots.size() + " Likes");

                }
            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog, parent, false);

        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView userProfileImageView;
        private TextView userNameTxtView;
        private TextView dateTxtView;
        private ImageView postImageView;
        private TextView descriptionTxtView;
        private ImageView likeImageView;
        private TextView numberOfLikesTxtView;
        private boolean hasLiked = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            userNameTxtView = itemView.findViewById(R.id.userNameTxtView);
            dateTxtView = itemView.findViewById(R.id.dateTxtView);
            postImageView = itemView.findViewById(R.id.postImageView);
            descriptionTxtView = itemView.findViewById(R.id.descriptionTxtView);
            likeImageView = itemView.findViewById(R.id.likeImageView);
            numberOfLikesTxtView = itemView.findViewById(R.id.numberOfLikesTxtView);

        }
    }
}
