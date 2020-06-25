package com.justice.schoolmanagement.parent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.ApplicationClass;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class ParentsActivityRecyclerAdapter extends FirestoreRecyclerAdapter<ParentData, ParentsActivityRecyclerAdapter.ViewHolder> {

    private Context context;

    private ParentData parentData;

    private ParentsActivity parentsActivity;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public ParentsActivityRecyclerAdapter(Context context, @NonNull FirestoreRecyclerOptions<ParentData> options) {
        super(options);
        this.context = context;
        parentsActivity = (ParentsActivity) context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ParentData model) {
        holder.parentNameTxtView.setText(model.getFullName());
        holder.parentContactTxtView.setText(model.getContact());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(model.getPhoto()).thumbnail(Glide.with(context).load(model.getThumbnail())).into(holder.imageView);

        setOnClickListeners(holder, position);

    }

    private void setOnClickListeners(ViewHolder holder, final int position) {
        holder.deleteTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteFromDatabase(position);

            }
        });

        holder.editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditParentActivity.class);
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position==RecyclerView.NO_POSITION){
                    return;
                }
                Intent intent = new Intent(context, ParentDetailsActivity.class);
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                context.startActivity(intent);
            }
        });
    }

    public void deleteFromDatabase(final int position) {

        new MaterialAlertDialogBuilder(context).setBackground(context.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyItemChanged(position);
            }
        }).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteParent(position);
            }
        }).show();
    }

    private void deleteParent(int position) {
        parentsActivity.showProgress(true);

        FirebaseStorage.getInstance().getReferenceFromUrl(getSnapshots().getSnapshot(position).toObject(ParentData.class).getPhoto()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(context, "Photo Deleted", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                parentsActivity.showProgress(false);
            }
        });
        getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(context, "Deletion Success", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                parentsActivity.showProgress(false);
            }
        });


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parents, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    public DocumentReference getSwipedItem(int position) {
        return getSnapshots().getSnapshot(position).getReference();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView parentNameTxtView, parentContactTxtView, deleteTxtView, editTxtView;
        private CircleImageView imageView;

        public ViewHolder(@NonNull View v) {
            super(v);
            parentNameTxtView = v.findViewById(R.id.parentNameTxtView);
            parentContactTxtView = v.findViewById(R.id.parentContactTxtView);
            deleteTxtView = v.findViewById(R.id.deleteTxtView);
            editTxtView = v.findViewById(R.id.editTxtView);
            imageView = v.findViewById(R.id.imageView);
        }
    }

}
