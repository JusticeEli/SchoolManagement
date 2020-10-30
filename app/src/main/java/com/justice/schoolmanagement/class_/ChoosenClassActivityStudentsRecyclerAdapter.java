package com.justice.schoolmanagement.class_;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.student.EditStudentActivity;
import com.justice.schoolmanagement.student.StudentData;
import com.justice.schoolmanagement.student.StudentDetailsActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChoosenClassActivityStudentsRecyclerAdapter extends FirestoreRecyclerAdapter<StudentData, ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder> {
    private Context context;

    private ChoosenClassActivity choosenClassActivity;


    public ChoosenClassActivityStudentsRecyclerAdapter(Context context, @NonNull FirestoreRecyclerOptions<StudentData> options) {
        super(options);
        this.context = context;
        choosenClassActivity = (ChoosenClassActivity) context;


    }

    @Override
    protected void onBindViewHolder(@NonNull ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder holder, int position, @NonNull StudentData model) {
        holder.studentNameTxtView.setText(model.getFullName());
        holder.studentClassTxtView.setText("" + model.getClassGrade());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(model.getPhoto()).thumbnail(Glide.with(context).load(model.getThumbnail())).into(holder.imageView);

        setOnClickListeners(holder, position);
    }

    private void setOnClickListeners(ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder holder, final int position) {
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


    private void deleteStudentFromDatabase(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStudent(position);
            }
        });
        builder.show();
    }

    private void deleteStudent(int position) {

        choosenClassActivity.showProgress(true);
        FirebaseStorage.getInstance().getReferenceFromUrl(getSnapshots().getSnapshot(position).toObject(StudentData.class).getPhoto()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Photo Deleted", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                choosenClassActivity.showProgress(false);
            }
        });
        getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "StudentData Deleted ", Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                choosenClassActivity.showProgress(false);
            }
        });
        FirebaseFirestore.getInstance().collection("StudentsMarks").document(getSnapshots().getSnapshot(position).getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "StudentsMarks Deleted ", Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                choosenClassActivity.showProgress(false);

            }
        });
    }

    @NonNull
    @Override
    public ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_students, parent, false);
        ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder viewHolder = new ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder(view);
        return viewHolder;

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
