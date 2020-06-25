package com.justice.schoolmanagement.student;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.ApplicationClass;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class StudentsActivityRecyclerAdapter extends FirestoreRecyclerAdapter<StudentData, StudentsActivityRecyclerAdapter.ViewHolder> {

    private Context context;

    private StudentsActivity studentsActivity;


    public StudentsActivityRecyclerAdapter(Context context, @NonNull FirestoreRecyclerOptions<StudentData> options) {
        super(options);
        this.context = context;
        studentsActivity = (StudentsActivity) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull StudentData model) {
        holder.studentNameTxtView.setText(model.getFullName());
        holder.studentClassTxtView.setText("" + model.getClassGrade());

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
                deleteStudentFromDatabase(position);
            }
        });

        holder.editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditStudentActivity.class);
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StudentDetailsActivity.class);
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                context.startActivity(intent);
            }
        });
    }


    public void deleteStudentFromDatabase(final int position) {

        new MaterialAlertDialogBuilder(context).setBackground(context.getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyItemChanged(position);
            }
        }).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               deleteStudent(position);
            }
        }).show();
    }

    private void deleteStudent(int position) {

        studentsActivity.showProgress(true);
        FirebaseStorage.getInstance().getReferenceFromUrl(getSnapshots().getSnapshot(position).toObject(StudentData.class).getPhoto()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(context, "Photo Deleted", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                studentsActivity.showProgress(false);
            }
        });
        getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(context, "StudentData Deleted ", Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                studentsActivity.showProgress(false);
            }
        });
        FirebaseFirestore.getInstance().collection("StudentsMarks").document(getSnapshots().getSnapshot(position).getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(context, "StudentsMarks Deleted ", Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                studentsActivity.showProgress(false);

            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_students, parent, false);
        StudentsActivityRecyclerAdapter.ViewHolder viewHolder = new StudentsActivityRecyclerAdapter.ViewHolder(view);
        return viewHolder;

    }
    public DocumentReference getSwipedItem(int position) {
        return getSnapshots().getSnapshot(position).getReference();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView studentNameTxtView, studentClassTxtView, deleteTxtView, editTxtView;
        private CircleImageView imageView;

        public ViewHolder(@NonNull View v) {
            super(v);
            studentNameTxtView = v.findViewById(R.id.studentNameTxtView);
            studentClassTxtView = v.findViewById(R.id.studentClassTxtView);
            deleteTxtView = v.findViewById(R.id.deleteTxtView);
            editTxtView = v.findViewById(R.id.editTxtView);
            imageView = v.findViewById(R.id.imageView);
        }
    }
}
