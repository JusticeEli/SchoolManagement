package com.justice.schoolmanagement.results;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.justice.schoolmanagement.class_.ChoosenClassActivity;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.student.StudentData;
import com.justice.schoolmanagement.student.StudentMarks;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;

public class ResultsEditActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText mathEdtTxt, scienceEdtTxt, englishEdtTxt, kiswahiliEdtTxt, sst_creEdtTxt;
    private Button submitBtn;
    private StudentMarks studentMarks;
    private String email;
    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private TextView nameTxtView;

    private ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_edit);
        email = getIntent().getStringExtra("email");
        studentMarks = getStudentMarks();
        initWidgets();
        initNavigationDrawer();


        setDefaultValues();
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

    private StudentMarks getStudentMarks() {
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getEmail().equals(email)) {
                return studentMarks;
            }
        }


        return null;

    }

    private void setOnClickListeners() {
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(ResultsEditActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(marksAreAbove_100()){
                    Toast.makeText(ResultsEditActivity.this, "Some Marks Are Not Valid", Toast.LENGTH_SHORT).show();

                    return;
                }
                getDataFromEdtTxtAndUpdateInDatabase();

            }
        });
    }

    private boolean marksAreAbove_100() {

        if (Integer.parseInt(mathEdtTxt.getText().toString())>100||Integer.parseInt(scienceEdtTxt.getText().toString())>100||Integer.parseInt(englishEdtTxt.getText().toString())>100||Integer.parseInt(kiswahiliEdtTxt.getText().toString())>100||Integer.parseInt(sst_creEdtTxt.getText().toString())>100){
            return true;
        }


        return false;
    }

    private void getDataFromEdtTxtAndUpdateInDatabase() {

        studentMarks.setMath(Integer.parseInt(mathEdtTxt.getText().toString()));
        studentMarks.setScience(Integer.parseInt(scienceEdtTxt.getText().toString()));
        studentMarks.setEnglish(Integer.parseInt(englishEdtTxt.getText().toString()));
        studentMarks.setKiswahili(Integer.parseInt(kiswahiliEdtTxt.getText().toString()));
        studentMarks.setSst_cre(Integer.parseInt(sst_creEdtTxt.getText().toString()));
        updateInDatabase();

    }

    private void updateInDatabase() {
        showProgress(true);
        Backendless.Persistence.save(studentMarks, new AsyncCallback<StudentMarks>() {
            @Override
            public void handleResponse(StudentMarks response) {
                showProgress(false);

                Toast.makeText(ResultsEditActivity.this, "Marks Updated", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(ResultsEditActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private boolean fieldsAreEmpty() {
        if (mathEdtTxt.getText().toString().trim().isEmpty() || scienceEdtTxt.getText().toString().trim().isEmpty() || englishEdtTxt.getText().toString().trim().isEmpty() || kiswahiliEdtTxt.getText().toString().trim().isEmpty() || sst_creEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    private void setDefaultValues() {
        nameTxtView.setText(studentMarks.getFullName());
        mathEdtTxt.setText("" + studentMarks.getMath());
        scienceEdtTxt.setText("" + studentMarks.getScience());
        englishEdtTxt.setText("" + studentMarks.getEnglish());
        kiswahiliEdtTxt.setText("" + studentMarks.getKiswahili());
        sst_creEdtTxt.setText("" + studentMarks.getSst_cre());
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
                        Toast.makeText(ResultsEditActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(ResultsEditActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

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

            default:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);


        }

    }

    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mathEdtTxt = findViewById(R.id.mathEdtTxt);
        scienceEdtTxt = findViewById(R.id.scienceEdtTxt);
        englishEdtTxt = findViewById(R.id.englishEdtTxt);
        kiswahiliEdtTxt = findViewById(R.id.kiswahiliEdtTxt);
        sst_creEdtTxt = findViewById(R.id.sst_creEdtTxt);
        submitBtn = findViewById(R.id.submitBtn);

        nameTxtView = findViewById(R.id.nameTxtView);


////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);


    }
}
