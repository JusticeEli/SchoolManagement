package com.justice.schoolmanagement.results;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.student.StudentMarks;

import java.util.List;

public class ResultsActivityRecyclerAdapter extends RecyclerView.Adapter<ResultsActivityRecyclerAdapter.ViewHolder> {
    private List<StudentMarks> list;
    private Context context;

    public ResultsActivityRecyclerAdapter(Context context) {
        this.context = context;
    }

    public void setList(List<StudentMarks> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_results, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.positionTxtView.setText(""+list.get(position).getPosition());
        holder.nameTxtView.setText(""+list.get(position).getName());
        holder.mathTxtView.setText(""+list.get(position).getMath());
        holder.scienceTxtView.setText(""+list.get(position).getScience());
        holder.englishTxtView.setText(""+list.get(position).getEnglish());
        holder.kiswahiliTxtView.setText(""+list.get(position).getKiswahili());
        holder.sst_creTxtView.setText(""+list.get(position).getSst_cre());
        holder.classGradeTxtView.setText("Class: "+list.get(position).getClassGrade());

        int totalMarks=list.get(position).getMath()+list.get(position).getScience()+list.get(position).getEnglish()+list.get(position).getKiswahili()+list.get(position).getSst_cre();
        holder.totalMarksTxtView.setText(""+totalMarks);

           holder.editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,ResultsEditActivity.class);
                intent.putExtra("email",list.get(position).getEmail());
                context.startActivity(intent);

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView positionTxtView, nameTxtView, mathTxtView, scienceTxtView, englishTxtView, kiswahiliTxtView, sst_creTxtView, editTxtView, totalMarksTxtView,classGradeTxtView;

        public ViewHolder(@NonNull View v) {
            super(v);
            positionTxtView = v.findViewById(R.id.positionTxtView);
            nameTxtView = v.findViewById(R.id.nameTxtView);
            mathTxtView = v.findViewById(R.id.mathTxtView);
            scienceTxtView = v.findViewById(R.id.scienceTxtView);
            englishTxtView = v.findViewById(R.id.englishTxtView);
            kiswahiliTxtView = v.findViewById(R.id.kiswahiliTxtView);
            sst_creTxtView = v.findViewById(R.id.sst_creTxtView);
            editTxtView = v.findViewById(R.id.editTxtView);
            totalMarksTxtView = v.findViewById(R.id.totalMarkTxtView);
            classGradeTxtView = v.findViewById(R.id.classGradeTxtView);

        }
    }
}
