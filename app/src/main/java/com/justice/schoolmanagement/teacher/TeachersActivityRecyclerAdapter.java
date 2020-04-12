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
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.parent.ParentDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class TeachersActivityRecyclerAdapter extends RecyclerView.Adapter<TeachersActivityRecyclerAdapter.ViewHolder> {
    private List<TeacherData> list = new ArrayList<>();
    private Context context;

    private TeachersActivity teachersActivity;

    private TeacherData teacherData;

    public void setList(List<TeacherData> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public TeachersActivityRecyclerAdapter(Context context) {
        this.context = context;
        teachersActivity = (TeachersActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teachers, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.teacherNameTxtView.setText(list.get(position).getFullName());
        holder.teacherSubjectTxtView.setText(list.get(position).getSubject());

        setOnClickListeners(holder, position);

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
                intent.putExtra("email", list.get(position).getEmail());
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TeacherDetailsActivity.class);
                intent.putExtra("email", list.get(position).getEmail());
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
        teacherData = list.get(position);
        teachersActivity.showProgress(true);
        Backendless.Persistence.of(TeacherData.class).remove(teacherData, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                teachersActivity.showProgress(false);
                AllData.teacherDataList.remove(teacherData);
                if (list != AllData.teacherDataList) {
                    list.remove(teacherData);
                }
                AllData.writeAllDataToFiles();
                notifyDataSetChanged();
                Toast.makeText(context, "Removed Teacher data From Database", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                teachersActivity.showProgress(false);
                Toast.makeText(context, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
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
