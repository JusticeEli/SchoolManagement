package com.justice.schoolmanagement.teacher;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.justice.schoolmanagement.student.StudentsActivity;

public class EditTeacherActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt;
    private EditText lastNameEdtTxt;
    private EditText emailEdtTxt;
    private EditText salaryEdtTxt;
    private EditText cityEdtTxt;
    private EditText degreeEdtTxt;
    private EditText ageEdtTxt;
    private RadioGroup genderRadioGroup;
    private RadioGroup typeRadioGroup;

    private RadioButton maleRadioBtn;
    private RadioButton femaleRadioBtn;
    private RadioButton otherRadioBtn;

    private RadioButton teacherRadioBtn;
    private RadioButton adminRadioBtn;

    private Spinner subjectSpinner;
    private EditText contactEdtTxt;
    private Button addPhotoBtn;
    private Button submitBtn;
    private String email;
    private TeacherData teacherData;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);
        email = getIntent().getStringExtra("email");
        teacherData = getTeacherData();
        initWidgets();
        initNavigationDrawer();


        setDefaultValues();
        setOnClickListeners();
    }

    private boolean contactEdtTxtFormatIsCorrect() {
        String contact = contactEdtTxt.getText().toString().trim();
        if (!contact.startsWith("07")) {
            Toast.makeText(this, "Contact Must start with 07 !!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (contact.length() != 10) {
            Toast.makeText(this, "Contact Must have 10 characters", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
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


    private TeacherData getTeacherData() {
        for (TeacherData teacherData : AllData.teacherDataList) {
            if (teacherData.getEmail().equals(email)) {
                return teacherData;

            }
        }

        return null;
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
        firstNameEdtTxt = findViewById(R.id.firstNameEdtTxt);
        lastNameEdtTxt = findViewById(R.id.lastNameEdtTxt);
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        salaryEdtTxt = findViewById(R.id.salaryEdtTxt);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);
        degreeEdtTxt = findViewById(R.id.degreeEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);

        maleRadioBtn = findViewById(R.id.maleRadioBtn);
        femaleRadioBtn = findViewById(R.id.femaleRadioBtn);
        otherRadioBtn = findViewById(R.id.otherRadioBtn);

        teacherRadioBtn = findViewById(R.id.teacherRadioBtn);
        adminRadioBtn = findViewById(R.id.adminRadioBtn);

        subjectSpinner = findViewById(R.id.subjectSpinner);
        contactEdtTxt = findViewById(R.id.contactEdtTxt);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        submitBtn = findViewById(R.id.submitBtn);

        ///////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);


        String[] subjects = {"Math", "Science", "English", "Kiswahili", "sst_cre"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        subjectSpinner.setAdapter(arrayAdapter);

        contactEdtTxt.setText("07");
    }

    private void setDefaultValues() {
        firstNameEdtTxt.setText(teacherData.getFirstName());
        lastNameEdtTxt.setText(teacherData.getLastName());
        emailEdtTxt.setText(teacherData.getEmail());
        salaryEdtTxt.setText(teacherData.getSalary());
        cityEdtTxt.setText(teacherData.getCity());
        degreeEdtTxt.setText(teacherData.getDegree());
        ageEdtTxt.setText(teacherData.getAge());
        setDefaultValueForGenderRadioBtn();
        setDefaultValueForTypeRadioBtn();

        setDefaultValueForSubjectSpinner();
        contactEdtTxt.setText(teacherData.getContact());
    }

    private void setDefaultValueForTypeRadioBtn() {
        switch (teacherData.getType()) {
            case "teacher":
                teacherRadioBtn.setChecked(true);
                break;
            case "admin":
                adminRadioBtn.setChecked(true);
                break;


        }

    }

    private void setDefaultValueForGenderRadioBtn() {
        switch (teacherData.getGender()) {
            case "Male":
                maleRadioBtn.setChecked(true);
                break;
            case "Female":
                femaleRadioBtn.setChecked(true);
                break;
            case "Other":
                otherRadioBtn.setChecked(true);
                break;

            default:
                maleRadioBtn.setChecked(true);


        }
    }

    private void setDefaultValueForSubjectSpinner() {
        switch (teacherData.getSubject()) {

            case "Math":
                subjectSpinner.setSelection(0);
                break;
            case "Science":
                subjectSpinner.setSelection(1);
                break;
            case "English":
                subjectSpinner.setSelection(2);
                break;
            case "Kiswahili":
                subjectSpinner.setSelection(3);
                break;
            case "sst_cre":
                subjectSpinner.setSelection(4);
                break;

        }
    }

    private void setOnClickListeners() {

        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditTeacherActivity.this, "Not Yet Implemented", Toast.LENGTH_SHORT).show();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(EditTeacherActivity.this, "Please fill All fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!contactEdtTxtFormatIsCorrect()) {
                    return;
                }
                getDataFromEdtTxtAndAddToDataBase();

            }

            private void getDataFromEdtTxtAndAddToDataBase() {
                // TODO: 11-Feb-20  extract a photo from the addPhoto button


                teacherData.setFullName(firstNameEdtTxt.getText().toString() + " " + lastNameEdtTxt.getText().toString());
                teacherData.setFirstName(firstNameEdtTxt.getText().toString());
                teacherData.setLastName(lastNameEdtTxt.getText().toString());
                teacherData.setEmail(emailEdtTxt.getText().toString());
                teacherData.setSalary(salaryEdtTxt.getText().toString());
                teacherData.setCity(cityEdtTxt.getText().toString());
                teacherData.setDegree(degreeEdtTxt.getText().toString());
                teacherData.setAge(ageEdtTxt.getText().toString());

                teacherData.setGender(getSelectedGenderRadioBtn());
                teacherData.setType(getSelectedTypeRadioBtn());

                teacherData.setPhoto("photo");
                teacherData.setSubject(subjectSpinner.getSelectedItem().toString());
                teacherData.setContact(contactEdtTxt.getText().toString());
                showProgress(true);
                Backendless.Persistence.save(teacherData, new AsyncCallback<TeacherData>() {
                    @Override
                    public void handleResponse(TeacherData response) {
                        showProgress(false);
                        Toast.makeText(EditTeacherActivity.this, "Teacher Data updated successfully", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        showProgress(false);
                        Toast.makeText(EditTeacherActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

    }

    private String getSelectedTypeRadioBtn() {
        switch (typeRadioGroup.getCheckedRadioButtonId()) {
            case R.id.teacherRadioBtn:
                return "teacher";

            case R.id.adminRadioBtn:
                return "admin";


        }
        return null;

    }

    private String getSelectedGenderRadioBtn() {
        switch (genderRadioGroup.getCheckedRadioButtonId()) {
            case R.id.maleRadioBtn:
                return "Male";

            case R.id.femaleRadioBtn:

                return "Female";

            case R.id.otherRadioBtn:
                return "Other";
        }

        return null;
    }

    private boolean fieldsAreEmpty() {

        if (firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || contactEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || salaryEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty() || degreeEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }

        return false;
    }


}
