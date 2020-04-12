package com.justice.schoolmanagement.dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.class_.ChoosenClassActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.parent.ParentData;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.student.StudentData;
import com.justice.schoolmanagement.student.StudentMarks;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeacherData;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.List;

public class DashBoardActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private Button teachersBtn, parentsBtn, studentsBtn, classessBtn, subjectsBtn, resultsBtn;
    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        initWidgets();

     //   loadDataFromDatabase();
        initNavigationDrawer();
        setOnClickListeners();
    }



    ////////////////////////NAVIGATION DRAWER/////////////////////////////////////////////
    private void initNavigationDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void setOnClickListeners() {
        teachersBtn.setOnClickListener(this);
        parentsBtn.setOnClickListener(this);
        studentsBtn.setOnClickListener(this);
        classessBtn.setOnClickListener(this);
        subjectsBtn.setOnClickListener(this);
        resultsBtn.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
      ApplicationClass.onNavigationItemSelected(this,menuItem.getItemId());
        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);


        }

    }


    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        teachersBtn = findViewById(R.id.teachersBtn);
        parentsBtn = findViewById(R.id.parentsBtn);
        studentsBtn = findViewById(R.id.studentsBtn);
        classessBtn = findViewById(R.id.classesBtn);
        subjectsBtn = findViewById(R.id.subjectsBtn);
        resultsBtn = findViewById(R.id.resultsBtn);

        ////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        relativeLayout = findViewById(R.id.relativeLayout);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.teachersBtn:
                Intent intent = new Intent(DashBoardActivity.this, TeachersActivity.class);
                startActivity(intent);
                break;

            case R.id.parentsBtn:
                Intent intent2 = new Intent(DashBoardActivity.this, ParentsActivity.class);
                startActivity(intent2);
                break;
            case R.id.studentsBtn:
                Intent intent3 = new Intent(DashBoardActivity.this, StudentsActivity.class);
                startActivity(intent3);
                break;
            case R.id.classesBtn:
                Intent intent4 = new Intent(DashBoardActivity.this, ClassesActivity.class);
                startActivity(intent4);
                break;
            case R.id.subjectsBtn:
                Intent intent5 = new Intent(DashBoardActivity.this, SubjectsActivity.class);
                startActivity(intent5);
                break;
            case R.id.resultsBtn:
                Intent intent6 = new Intent(DashBoardActivity.this, ResultsActivity.class);
                startActivity(intent6);
                break;



        }
    }

    //////////////////LOAD DATA FROM DATABASE////////////////////
    private void loadDataFromDatabase() {
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setGroupBy("fullName");
        showProgress(true);
        Backendless.Persistence.of(TeacherData.class).find(dataQueryBuilder, new AsyncCallback<List<TeacherData>>() {
            @Override
            public void handleResponse(List<TeacherData> response) {
                showProgress(false);
                AllData.teacherDataList = response;
                Toast.makeText(DashBoardActivity.this, "loaded Teacher data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(DashBoardActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        showProgress(true);
        Backendless.Persistence.of(ParentData.class).find(dataQueryBuilder, new AsyncCallback<List<ParentData>>() {
            @Override
            public void handleResponse(List<ParentData> response) {
                showProgress(false);
                AllData.parentDataList = response;
                Toast.makeText(DashBoardActivity.this, "loaded Parent data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(DashBoardActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        showProgress(true);
        Backendless.Persistence.of(StudentData.class).find(dataQueryBuilder, new AsyncCallback<List<StudentData>>() {
            @Override
            public void handleResponse(List<StudentData> response) {
                showProgress(false);
                AllData.studentDataList = response;
                Toast.makeText(DashBoardActivity.this, "loaded StudentData data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(DashBoardActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        showProgress(true);
        Backendless.Persistence.of(StudentMarks.class).find(dataQueryBuilder, new AsyncCallback<List<StudentMarks>>() {
            @Override
            public void handleResponse(List<StudentMarks> response) {
                showProgress(false);
                AllData.studentMarksList = response;
                Toast.makeText(DashBoardActivity.this, "loaded StudentMarks data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(DashBoardActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}
