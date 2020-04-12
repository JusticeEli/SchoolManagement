package com.justice.schoolmanagement.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;

public class StudentDetailsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextView studentNameTxtView, studentClassTxtView, firstNameTxtView, lastNameTxtView, classGradeTxtView, nationalityTxtView, religionTxtView, emailTxtView, parentNameTxtView, dateOfBirthTxtView, dateOfArrivalTxtView, ageTxtView, genderTxtView, classTeacherNameTxtView, cityTxtView, deleteTxtView, editTxtView;
    private String email;
    private StudentData studentData;
    private StudentMarks studentMarks;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private CardView cardView;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);
        email = getIntent().getStringExtra("email");
        studentData = getStudentData();
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

    private StudentData getStudentData() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getEmail().equals(email)) {
                return studentData;
            }

        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDefaultValues();

    }

    private void setOnClickListeners() {
        deleteTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteStudentDataFromDatabase();
            }
        });
        editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentDetailsActivity.this, EditStudentActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

    private void deleteStudentDataFromDatabase() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        } ).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStudent();
            }
        });
        builder.show();
    }

    private void deleteStudent() {
        showProgress(true);
        Backendless.Persistence.of(StudentData.class).remove(studentData, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                removeStudentMarksFromDatabase();
                showProgress(false);

                AllData.studentDataList.remove(studentData);
                Toast.makeText(StudentDetailsActivity.this, "Student data Removed", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(StudentDetailsActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void removeStudentMarksFromDatabase() {
        studentMarks = getStudentMarks();
        showProgress(true);
        Backendless.Persistence.of(StudentMarks.class).remove(studentMarks, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                showProgress(false);
                AllData.studentMarksList.remove(studentMarks);
                Toast.makeText(StudentDetailsActivity.this, "Student Marks removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(StudentDetailsActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

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

    private void setDefaultValues() {
        studentNameTxtView.setText(studentData.getFullName());
        studentClassTxtView.setText("" + studentData.getClassGrade());
        firstNameTxtView.setText(studentData.getFirstName());
        lastNameTxtView.setText(studentData.getLastName());
        classGradeTxtView.setText("" + studentData.getClassGrade());
        nationalityTxtView.setText(studentData.getNationality());
        religionTxtView.setText(studentData.getReligion());
        emailTxtView.setText(studentData.getEmail());
        parentNameTxtView.setText(studentData.getParentName());
        dateOfBirthTxtView.setText(studentData.getDateOfBirth());
        dateOfArrivalTxtView.setText(studentData.getDateOfArrival());
        ageTxtView.setText("" + studentData.getAge());
        genderTxtView.setText(studentData.getGender());
        classTeacherNameTxtView.setText(studentData.getClassTeacherName());
        cityTxtView.setText(studentData.getCity());

    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            cardView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);


        }

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


        return super.onOptionsItemSelected(item);
    }

    private void initWidgets() {
        studentNameTxtView = findViewById(R.id.studentNameTxtView);
        studentClassTxtView = findViewById(R.id.studentClassTxtView);
        firstNameTxtView = findViewById(R.id.firstNameTxtView);
        lastNameTxtView = findViewById(R.id.lastNameTxtView);
        classGradeTxtView = findViewById(R.id.classGradeTxtView);
        nationalityTxtView = findViewById(R.id.nationalityTxtView);
        religionTxtView = findViewById(R.id.religionTxtView);
        emailTxtView = findViewById(R.id.emailTxtView);
        parentNameTxtView = findViewById(R.id.parentNameTxtView);
        dateOfBirthTxtView = findViewById(R.id.dateOfBirthTxtView);
        dateOfArrivalTxtView = findViewById(R.id.dateOfArrivalTxtView);
        ageTxtView = findViewById(R.id.ageTxtView);
        genderTxtView = findViewById(R.id.genderTxtView);
        classTeacherNameTxtView = findViewById(R.id.classTeacherNameTxtView);
        cityTxtView = findViewById(R.id.cityTxtView);

        /////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        cardView = findViewById(R.id.cardView);


        deleteTxtView = findViewById(R.id.deleteTxtView);
        editTxtView = findViewById(R.id.editTxtView);
    }
}
