package com.justice.schoolmanagement.presentation.ui.teacher;

import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.presentation.ApplicationClass;
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class TeachersActivityRecyclerAdapter extends FirestoreRecyclerAdapter<TeacherData, TeachersActivityRecyclerAdapter.ViewHolder> {

    private Context context;

    private TeachersFragment teachersFragment;

    private TeacherData teacherData;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public TeachersActivityRecyclerAdapter(TeachersFragment teachersFragment, @NonNull FirestoreRecyclerOptions<TeacherData> options) {
        super(options);
        this.teachersFragment = teachersFragment;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull TeacherData model) {
        holder.teacherNameTxtView.setText(model.getFullName());
        holder.teacherSubjectTxtView.setText(model.getSubject());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(model.getPhoto()).thumbnail(Glide.with(context).load(model.getThumbnail())).into(holder.imageView);

        setOnClickListeners(holder, position);

        setOnClickListeners(holder, position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teachers, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    private void setOnClickListeners(ViewHolder holder, final int position) {
        holder.deleteTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTeacherDataFromDatabase(position);
            }
        });

        holder.editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                teachersFragment.navController.navigate(R.id.action_teachersFragment_to_editTeacherFragment);

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                teachersFragment.navController.navigate(R.id.action_teachersFragment_to_teacherDetailsFragment);
            }
        });

    }

    public void deleteTeacherDataFromDatabase(final int position) {
        new MaterialAlertDialogBuilder(context).setBackground(context.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyItemChanged(position);
            }
        }).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTeacher(position);
            }
        }).show();

    }

    public DocumentReference getSwipedItem(int position) {
        return getSnapshots().getSnapshot(position).getReference();
    }

    private void deleteTeacher(int position) {
        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
        teacherData = getSnapshots().getSnapshot(position).toObject(TeacherData.class);
        teachersFragment.showProgress(true);

        FirebaseStorage.getInstance().getReference("teachers_images").child(documentSnapshot.getId() + ".jpg").delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(context, "Photo Deleted", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                teachersFragment.showProgress(false);
            }
        });


        getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.error(context, "Teacher Deleted", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                teachersFragment.showProgress(false);

            }
        });

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imageView;
        private TextView teacherNameTxtView, teacherSubjectTxtView, deleteTxtView, editTxtView;


        public ViewHolder(@NonNull View v) {
            super(v);
            imageView = v.findViewById(R.id.imageView);
            teacherNameTxtView = v.findViewById(R.id.teacherNameTxtView);
            teacherSubjectTxtView = v.findViewById(R.id.teacherSubjectTxtView);
            deleteTxtView = v.findViewById(R.id.deleteTxtView);
            editTxtView = v.findViewById(R.id.editTxtView);
        }
    }
}
