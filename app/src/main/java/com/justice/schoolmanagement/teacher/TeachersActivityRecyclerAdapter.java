package com.justice.schoolmanagement.teacher;

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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.parent.ParentDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class TeachersActivityRecyclerAdapter extends FirestoreRecyclerAdapter<TeacherData, TeachersActivityRecyclerAdapter.ViewHolder> {

    private Context context;

    private TeachersActivity teachersActivity;

    private TeacherData teacherData;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public TeachersActivityRecyclerAdapter(Context context, @NonNull FirestoreRecyclerOptions<TeacherData> options) {
        super(options);
        this.context = context;
        teachersActivity = (TeachersActivity) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull TeacherData model) {
        holder.teacherNameTxtView.setText(model.getFullName());
        holder.teacherSubjectTxtView.setText(model.getSubject());

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
                Intent intent = new Intent(context, EditTeacherActivity.class);
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TeacherDetailsActivity.class);
                ApplicationClass.documentSnapshot = getSnapshots().getSnapshot(position);
                context.startActivity(intent);
            }
        });

    }

    private void deleteTeacherDataFromDatabase(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTeacher(position);
            }
        });
        builder.show();
    }

    private void deleteTeacher(int position) {
        teacherData = getSnapshots().getSnapshot(position).toObject(TeacherData.class);
        teachersActivity.showProgress(true);

        getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                teachersActivity.showProgress(false);

            }
        });

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
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
