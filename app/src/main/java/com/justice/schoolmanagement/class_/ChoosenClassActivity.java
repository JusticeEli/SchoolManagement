package com.justice.schoolmanagement.class_;

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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentData;
import com.justice.schoolmanagement.student.StudentMarks;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChoosenClassActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
//////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    private int class_;

    private ChoosenClassActivityResultsRecyclerAdapter choosenClassActivityResultsRecyclerAdapter;
    private ChoosenClassActivityStudentsRecyclerAdapter choosenClassActivityStudentsRecyclerAdapter;


    private List<StudentData> choosenClassStudentDataList = new ArrayList<>();
    private List<StudentMarks> choosenClassStudentMarkList = new ArrayList<>();

    private TextView headerTxtView;
    private Button listOfStudentBtn, resultsBtn;
    private EditText searchEdtTxt;
    private RecyclerView listOfStudentRecyclerView, resultsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosen_class);
        class_ = getIntent().getIntExtra("class", 0);
        initWidgets();
        initNavigationDrawer();


        setDefaultValues();
        setAdapters();
        setOnClickListeners();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_sort, menu);
        return super.onCreateOptionsMenu(menu);
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


    private void setAdapters() {
        choosenClassActivityResultsRecyclerAdapter = new ChoosenClassActivityResultsRecyclerAdapter(this);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(choosenClassActivityResultsRecyclerAdapter);
        choosenClassActivityResultsRecyclerAdapter.setList(choosenClassStudentMarkList);


        choosenClassActivityStudentsRecyclerAdapter = new ChoosenClassActivityStudentsRecyclerAdapter(this);
        listOfStudentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listOfStudentRecyclerView.setAdapter(choosenClassActivityStudentsRecyclerAdapter);
        choosenClassActivityStudentsRecyclerAdapter.setList(choosenClassStudentDataList);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        ApplicationClass.onNavigationItemSelected(this,menuItem.getItemId());
        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortMenu:
                sortStudentMarks();
                break;
        }

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void sortStudentMarks() {
        int x = 1;
        Collections.sort(choosenClassStudentMarkList);
        for (StudentMarks studentMarks : choosenClassStudentMarkList) {
            studentMarks.setPosition(x++);
        }
        choosenClassActivityResultsRecyclerAdapter.setList(choosenClassStudentMarkList);
    }

    private void setDefaultValues() {
        switch (class_) {
            case 1:
                headerTxtView.setText("Class 1");
                getClass_1_data();
                break;
            case 2:
                headerTxtView.setText("Class 2");
                getClass_2_data();
                break;
            case 3:
                headerTxtView.setText("Class 3");
                getClass_3_data();
                break;
            case 4:
                headerTxtView.setText("Class 4");
                getClass_4_data();
                break;
            case 5:
                headerTxtView.setText("Class 5");
                getClass_5_data();
                break;
            case 6:
                headerTxtView.setText("Class 6");
                getClass_6_data();
                break;
            case 7:
                headerTxtView.setText("Class 7");
                getClass_7_data();
                break;
            case 8:
                headerTxtView.setText("Class 8");
                getClass_8_data();
                break;
            default:
                break;

        }
    }

    private void getClass_1_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 1) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 1) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_2_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 2) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 2) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_3_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 3) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 3) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_4_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 4) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 4) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_5_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 5) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 5) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_6_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 6) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 6) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_7_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 7) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 7) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_8_data() {

        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 8) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 8) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void setOnClickListeners() {
        setOnClickListenerForSearchEdtTxt();
        listOfStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listOfStudentRecyclerView.setVisibility(View.VISIBLE);
                resultsRecyclerView.setVisibility(View.GONE);

            }
        });
        resultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listOfStudentRecyclerView.setVisibility(View.GONE);
                resultsRecyclerView.setVisibility(View.VISIBLE);


            }
        });

    }

    private void setOnClickListenerForSearchEdtTxt() {
        searchEdtTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<StudentMarks> list = new ArrayList<>();
                if (searchEdtTxt.getText().toString().isEmpty()) {
                    choosenClassActivityResultsRecyclerAdapter.setList(choosenClassStudentMarkList);
                } else {

                    for (StudentMarks studentMarks : choosenClassStudentMarkList) {
                        if (studentMarks.getName().toLowerCase().contains(searchEdtTxt.getText().toString().toLowerCase())) {
                            if (!list.contains(studentMarks)) {
                                list.add(studentMarks);
                            }
                        }
                    }
                    choosenClassActivityResultsRecyclerAdapter.setList(list);
                }

                List<StudentData> list2 = new ArrayList<>();
                if (searchEdtTxt.getText().toString().isEmpty()) {
                    choosenClassActivityStudentsRecyclerAdapter.setList(choosenClassStudentDataList);
                } else {

                    for (StudentData studentData : choosenClassStudentDataList) {
                        if (studentData.getFullName().toLowerCase().contains(searchEdtTxt.getText().toString().toLowerCase())) {
                            if (!list2.contains(studentData)) {
                                list2.add(studentData);
                            }
                        }
                    }
                    choosenClassActivityStudentsRecyclerAdapter.setList(list2);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    public void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            resultsRecyclerView.setVisibility(View.GONE);
            listOfStudentRecyclerView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            resultsRecyclerView.setVisibility(View.VISIBLE);
            listOfStudentRecyclerView.setVisibility(View.VISIBLE);


        }

    }


    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        headerTxtView = findViewById(R.id.headerTxtView);
        listOfStudentBtn = findViewById(R.id.listOfStudentBtn);
        resultsBtn = findViewById(R.id.resultsBtn);
        searchEdtTxt = findViewById(R.id.searchEdtTxt);
        listOfStudentRecyclerView = findViewById(R.id.listOfStudentRecyclerView);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);


////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);


    }

    @Override
    protected void onResume() {
        super.onResume();
        sortStudentMarks();
        choosenClassActivityResultsRecyclerAdapter.notifyDataSetChanged();
        choosenClassActivityStudentsRecyclerAdapter.notifyDataSetChanged();
    }
}
