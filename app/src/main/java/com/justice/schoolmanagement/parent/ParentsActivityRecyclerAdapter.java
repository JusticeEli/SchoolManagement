package com.justice.schoolmanagement.parent;

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

import java.util.ArrayList;
import java.util.List;

public class ParentsActivityRecyclerAdapter extends RecyclerView.Adapter<ParentsActivityRecyclerAdapter.ViewHolder> {

    private List<ParentData> list = new ArrayList<>();
    private Context context;

    private ParentData parentData;

    private ParentsActivity parentsActivity;

    public void setList(List<ParentData> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ParentsActivityRecyclerAdapter(Context context) {
        this.context = context;
        parentsActivity = (ParentsActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parents, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.parentNameTxtView.setText(list.get(position).getFullName());
        holder.parentContactTxtView.setText(list.get(position).getContact());
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
                intent.putExtra("email", list.get(position).getEmail());
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ParentDetailsActivity.class);
                intent.putExtra("email", list.get(position).getEmail());
                context.startActivity(intent);
            }
        });
    }

    private void deleteFromDatabase(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteParent(position);
            }
        });
        builder.show();
    }

    private void deleteParent(int position) {
        parentData = list.get(position);
        parentsActivity.showProgress(true);
        Backendless.Persistence.of(ParentData.class).remove(parentData, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                parentsActivity.showProgress(false);
                AllData.parentDataList.remove(parentData);
                if (list != AllData.parentDataList) {
                    list.remove(parentData);
                }
                notifyDataSetChanged();
                Toast.makeText(context, parentData.getFullName() + " removed", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void handleFault(BackendlessFault fault) {
                parentsActivity.showProgress(false);
                Toast.makeText(context, " Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView parentNameTxtView, parentContactTxtView, deleteTxtView, editTxtView;
        private ImageView imageView;

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
