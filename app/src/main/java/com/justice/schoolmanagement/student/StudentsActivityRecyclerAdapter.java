package com.justice.schoolmanagement.student;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;

import java.util.ArrayList;
import java.util.List;

public class StudentsActivityRecyclerAdapter extends FirestoreRecyclerAdapter<StudentData, StudentsActivityRecyclerAdapter.ViewHolder> {

    private Context context;
    private StudentData studentData;
    private StudentMarks studentMarks;

    private StudentsActivity studentsActivity;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public StudentsActivityRecyclerAdapter(Context context, @NonNull FirestoreRecyclerOptions<StudentData> options) {
        super(options);
        this.context = context;
        studentsActivity = (StudentsActivity) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull StudentData model) {
        holder.studentNameTxtView.setText(model.getFullName());
        holder.studentClassTxtView.setText("" + model.getClassGrade());

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
                ApplicationClass.documentSnapshot=getSnapshots().getSnapshot(position);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StudentDetailsActivity.class);
                ApplicationClass.documentSnapshot=getSnapshots().getSnapshot(position);
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

        studentsActivity.showProgress(true);
        getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Deletion Success", Toast.LENGTH_SHORT).show();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView studentNameTxtView, studentClassTxtView, deleteTxtView, editTxtView;
        private ImageView imageView;

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
