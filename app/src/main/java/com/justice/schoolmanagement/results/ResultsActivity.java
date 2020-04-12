package com.justice.schoolmanagement.results;

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.class_.ChoosenClassActivity;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.student.StudentMarks;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private EditText searchEdtTxt;
    private ResultsActivityRecyclerAdapter resultsActivityRecyclerAdapter;  private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        initWidgets();  initNavigationDrawer();


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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sort,menu);

        return true;
    }


    private void setOnClickListeners() {
        searchEdtTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<StudentMarks> list = new ArrayList<>();
                if (searchEdtTxt.getText().toString().isEmpty()) {
                    resultsActivityRecyclerAdapter.setList(AllData.studentMarksList);
                } else {

                    for (StudentMarks studentMarks : AllData.studentMarksList) {
                        if (studentMarks.getName().toLowerCase().contains(searchEdtTxt.getText().toString().toLowerCase())) {
                            if (!list.contains(studentMarks)) {
                                list.add(studentMarks);
                            }
                        }
                    }
                    resultsActivityRecyclerAdapter.setList(list);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initWidgets() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchEdtTxt = findViewById(R.id.searchEdtTxt);
        recyclerView = findViewById(R.id.recyclerView);


        resultsActivityRecyclerAdapter = new ResultsActivityRecyclerAdapter(this);
        recyclerView.setAdapter(resultsActivityRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsActivityRecyclerAdapter.setList(AllData.studentMarksList);
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
            case R.id.logoutMenu:
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Toast.makeText(ResultsActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(ResultsActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
                Intent intent8 = new Intent(this, MainActivity.class);
                startActivity(intent8);
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

            case R.id.sortMenu:
                sortStudentMarks();
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sortStudentMarks();
        resultsActivityRecyclerAdapter.setList(AllData.studentMarksList);
    }

    private void sortStudentMarks() {
        int x=1;
        Collections.sort(AllData.studentMarksList);
        for (StudentMarks studentMarks:AllData.studentMarksList){
            studentMarks.setPosition(x++);
        }


        resultsActivityRecyclerAdapter.setList(AllData.studentMarksList);

    }
}
