package com.justice.schoolmanagement.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.teacher.AddTeacherActivity;
import com.justice.schoolmanagement.teacher.TeacherData;
import com.justice.schoolmanagement.teacher.TeachersActivity;
import com.justice.schoolmanagement.teacher.TeachersActivityRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.justice.schoolmanagement.R.layout.activity_students;

public class StudentsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private StudentsActivityRecyclerAdapter studentsActivityRecyclerAdapter;
    private EditText searchEdtTxt;
    private RecyclerView recyclerView;
    private Button addStudentBtn;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);

        initwidgets();
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


    @Override
    protected void onResume() {
        super.onResume();
        studentsActivityRecyclerAdapter.setList(AllData.studentDataList);
    }

    private void setOnClickListeners() {

        addStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentsActivity.this, AddStudentActivity.class);
                startActivity(intent);

            }
        });
        searchEdtTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<StudentData> list = new ArrayList<>();
                if (searchEdtTxt.getText().toString().isEmpty()) {
                    studentsActivityRecyclerAdapter.setList(AllData.studentDataList);
                } else {
                    for (StudentData studentData : AllData.studentDataList) {
                        if (studentData.getFullName().toLowerCase().contains(searchEdtTxt.getText().toString().toLowerCase())) {
                            if (!list.contains(studentData)) {
                                list.add(studentData);

                            }


                        }
                    }
                    studentsActivityRecyclerAdapter.setList(list);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.dashboardMenu:
                Intent intent = new Intent(this, DashBoardActivity.class);
                startActivity(intent);
                break;
            case R.id.teacherMenu:
                Intent intent2 = new Intent(this, TeachersActivity.class);
                startActivity(intent2);
                break;
            case R.id.studentsMenu:
                Intent intent3 = new Intent(this, StudentsActivity.class);
                startActivity(intent3);
                break;
            case R.id.parentsMenu:
                Intent intent4 = new Intent(this, ParentsActivity.class);
                startActivity(intent4);
                break;
            case R.id.subjectsMenu:
                Intent intent5 = new Intent(this, SubjectsActivity.class);
                startActivity(intent5);
                break;
            case R.id.resultsMenu:
                Intent intent6 = new Intent(this, ResultsActivity.class);
                startActivity(intent6);
                break;
            case R.id.classesMenu:
                Intent intent7 = new Intent(this, ClassesActivity.class);
                startActivity(intent7);
                break;



        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
    /////////////////////PROGRESS_BAR////////////////////////////
    public void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);


        }

    }


    private void initwidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchEdtTxt = findViewById(R.id.searchEdtTxt);
        recyclerView = findViewById(R.id.recyclerView);
        addStudentBtn = findViewById(R.id.addStudentBtn);

        /////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);



        studentsActivityRecyclerAdapter = new StudentsActivityRecyclerAdapter(this);
        recyclerView.setAdapter(studentsActivityRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsActivityRecyclerAdapter.setList(AllData.studentDataList);
    }
}
