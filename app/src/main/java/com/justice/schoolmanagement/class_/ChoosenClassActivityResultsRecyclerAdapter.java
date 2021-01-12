package com.justice.schoolmanagement.class_;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.presentation.ApplicationClass;
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks;
import com.justice.schoolmanagement.results.ResultsEditActivity;

public class ChoosenClassActivityResultsRecyclerAdapter extends FirestoreRecyclerAdapter<StudentMarks,ChoosenClassActivityResultsRecyclerAdapter.ViewHolder> {
    private Context context;


    public ChoosenClassActivityResultsRecyclerAdapter(Context context, @NonNull FirestoreRecyclerOptions<StudentMarks> options) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChoosenClassActivityResultsRecyclerAdapter.ViewHolder holder, final int position, @NonNull StudentMarks model) {

        //////////////////////////////////
        holder.positionTxtView.setText("" + model.getPosition());
        holder.nameTxtView.setText("" + model.getName());
        holder.mathTxtView.setText("" + model.getMath());
        holder.scienceTxtView.setText("" + model.getScience());
        holder.englishTxtView.setText("" + model.getEnglish());
        holder.kiswahiliTxtView.setText("" + model.getKiswahili());
        holder.sst_creTxtView.setText("" + model.getSst_cre());
        holder.classGradeTxtView.setText("Class: " + model.getClassGrade());

        int totalMarks = model.getMath() + model.getScience() + model.getEnglish() + model.getKiswahili() + model.getSst_cre();
        holder.totalMarksTxtView.setText("" + totalMarks);

        holder.editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationClass.documentSnapshot=getSnapshots().getSnapshot(position);
                Intent intent = new Intent(context, ResultsEditActivity.class);
                context.startActivity(intent);

            }
        });

    }


    @NonNull
    @Override
    public ChoosenClassActivityResultsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_results, parent, false);
        ChoosenClassActivityResultsRecyclerAdapter.ViewHolder viewHolder = new ChoosenClassActivityResultsRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView positionTxtView, nameTxtView, mathTxtView, scienceTxtView, englishTxtView, kiswahiliTxtView, sst_creTxtView, editTxtView, totalMarksTxtView, classGradeTxtView;

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
