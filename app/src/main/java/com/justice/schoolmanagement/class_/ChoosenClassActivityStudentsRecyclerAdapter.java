package com.justice.schoolmanagement.class_;

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
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.student.EditStudentActivity;
import com.justice.schoolmanagement.student.StudentData;
import com.justice.schoolmanagement.student.StudentDetailsActivity;
import com.justice.schoolmanagement.student.StudentMarks;

import java.util.ArrayList;
import java.util.List;

public class ChoosenClassActivityStudentsRecyclerAdapter extends RecyclerView.Adapter<ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder> {
    private List<StudentData> list = new ArrayList<>();
    private Context context;
    private StudentData studentData;
    private StudentMarks studentMarks;
    private ChoosenClassActivity choosenClassActivity;

    public void setList(List<StudentData> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ChoosenClassActivityStudentsRecyclerAdapter(Context context) {
        this.context = context;
        choosenClassActivity = (ChoosenClassActivity) context;
    }

    @NonNull
    @Override
    public ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_students, parent, false);
        ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder viewHolder = new ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChoosenClassActivityStudentsRecyclerAdapter.ViewHolder holder, int position) {
        holder.studentNameTxtView.setText(list.get(position).getFullName());
        holder.studentClassTxtView.setText("" + list.get(position).getClassGrade());

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
                intent.putExtra("email", list.get(position).getEmail());
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StudentDetailsActivity.class);
                intent.putExtra("email", list.get(position).getEmail());
                context.startActivity(intent);
            }
        });
    }

    private void deleteStudentFromDatabase(final int position) {
        AlertDialog.Builder builder=new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        } ).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStudent(position);
            }
        });
        builder.show();
    }

    private void deleteStudent(int position) {
        studentData = list.get(position);
        choosenClassActivity.showProgress(true);
        Backendless.Persistence.of(StudentData.class).remove(studentData, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                removeStudentMarksFromDatabase();
                choosenClassActivity.showProgress(false);
                Toast.makeText(context, "Student Data Removed", Toast.LENGTH_SHORT).show();
                AllData.studentDataList.remove(studentData);
                if (list != AllData.studentDataList) {
                    list.remove(studentData);
                }
                notifyDataSetChanged();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                choosenClassActivity.showProgress(false);
                Toast.makeText(context, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void removeStudentMarksFromDatabase() {
        studentMarks = getStudentMarks();
        choosenClassActivity.showProgress(true);
        Backendless.Persistence.of(StudentMarks.class).remove(studentMarks, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                choosenClassActivity.showProgress(false);
                AllData.studentMarksList.remove(studentMarks);
                Toast.makeText(context, "Student Marks removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                choosenClassActivity.showProgress(false);
                Toast.makeText(context, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private StudentMarks getStudentMarks() {
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getEmail().equals(studentData.getEmail())) {
                return studentMarks;
            }
        }


        return null;
    }

    @Override
    public int getItemCount() {
        return list.size();
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
