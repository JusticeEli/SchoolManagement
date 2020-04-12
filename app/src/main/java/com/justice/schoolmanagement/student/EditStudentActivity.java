package com.justice.schoolmanagement.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.teacher.TeacherData;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditStudentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String email;
    private StudentData studentData;
    private StudentMarks studentMarks;


    private EditText firstNameEdtTxt, lastNameEdtTxt, emailEdtTxt, parentNameEdtTxt, dateOfBirthEdtTxt, dateOfArrivalEdtTxt, ageEdtTxt;
    private AutoCompleteTextView cityEdtTxt;
    private Spinner classGradeSpinner, nationalitySpinner, religionSpinner, classTeacherNameSpinner;

    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioBtn;
    private RadioButton femaleRadioBtn;
    private RadioButton otherRadioBtn;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    private Button addPhotoBtn, submitBtn;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);
        email = getIntent().getStringExtra("email");
        studentData = ApplicationClass.documentSnapshot.toObject(StudentData.class);
        studentData.setId(ApplicationClass.documentSnapshot.getId());
        initWidgets();

        initNavigationDrawer();

        setDefaultValuesToEdtTxt();
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


    private void setDefaultValueForRadioBtn() {
        switch (studentData.getGender()) {
            case "Male":
                maleRadioBtn.setChecked(true);
                break;
            case "Female":
                femaleRadioBtn.setChecked(true);
                break;
            case "Other":
                otherRadioBtn.setChecked(true);
                break;

        }
    }

    private void setDefaultValuesToEdtTxt() {
        firstNameEdtTxt.setText(studentData.getFirstName());
        lastNameEdtTxt.setText(studentData.getLastName());
        setDefaultValueClassGradeSpinner();
        setDefaultValueForNationalitySpinner();
        setDefaultValueForReligionSpinner();
        setDefaultValueForClassTeacherNameSpinner();

        setDefaultValueForRadioBtn();

        emailEdtTxt.setText(studentData.getEmail());
        parentNameEdtTxt.setText(studentData.getParentName());
        dateOfBirthEdtTxt.setText(studentData.getDateOfBirth());
        dateOfArrivalEdtTxt.setText(studentData.getDateOfArrival());
        ageEdtTxt.setText(studentData.getAge());
        cityEdtTxt.setText(studentData.getCity());
    }

    private void setDefaultValueForClassTeacherNameSpinner() {
        //// TODO: 18-Mar-20 set default value of class teacher
        for (int i = 0; i < AllData.teacherDataList.size(); i++) {

            if (studentData.getClassTeacherName().equals(AllData.teacherDataList.get(i).getFullName())) {

                classTeacherNameSpinner.setSelection(i);
                return;
            }

        }
    }

    private void setDefaultValueForReligionSpinner() {
        switch (studentData.getReligion()) {

            case "Christian":
                religionSpinner.setSelection(0);
                break;
            case "Muslim":
                religionSpinner.setSelection(1);
                break;

        }
    }

    private void setDefaultValueForNationalitySpinner() {
        switch (studentData.getNationality()) {

            case "Kenyan":
                nationalitySpinner.setSelection(0);
                break;
            case "Foreigner":
                nationalitySpinner.setSelection(1);
                break;

        }
    }

    private void setDefaultValueClassGradeSpinner() {
        switch (studentData.getClassGrade()) {

            case 1:
                classGradeSpinner.setSelection(0);
                break;
            case 2:
                classGradeSpinner.setSelection(1);
                break;
            case 3:
                classGradeSpinner.setSelection(2);
                break;
            case 4:
                classGradeSpinner.setSelection(3);
                break;
            case 5:
                classGradeSpinner.setSelection(4);
                break;
            case 6:
                classGradeSpinner.setSelection(5);
                break;
            case 7:
                classGradeSpinner.setSelection(6);
                break;
            case 8:
                classGradeSpinner.setSelection(7);
                break;

        }
    }

    private void setOnClickListeners() {

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (fieldsAreEmpty()) {
                    Toast.makeText(EditStudentActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                getDataFromEdtTxtAndUpdateInDatabase();
                Toast.makeText(EditStudentActivity.this, studentData.getFirstName() + " Edited Successfully ", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private boolean fieldsAreEmpty() {
        if (firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || parentNameEdtTxt.getText().toString().trim().isEmpty() || dateOfBirthEdtTxt.getText().toString().trim().isEmpty() || dateOfArrivalEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    private String getSelectedRadioBtn() {
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

    private void getDataFromEdtTxtAndUpdateInDatabase() {

        studentData.setFullName(firstNameEdtTxt.getText().toString().trim() + " " + lastNameEdtTxt.getText().toString().trim());
        studentData.setFirstName(firstNameEdtTxt.getText().toString().trim());
        studentData.setLastName(lastNameEdtTxt.getText().toString().trim());

        studentData.setClassGrade(Integer.parseInt(classGradeSpinner.getSelectedItem().toString().trim()));
        studentData.setNationality(nationalitySpinner.getSelectedItem().toString().trim());
        studentData.setReligion(religionSpinner.getSelectedItem().toString().trim());
        studentData.setClassTeacherName(classTeacherNameSpinner.getSelectedItem().toString().trim());
        studentData.setGender(getSelectedRadioBtn());

        studentData.setEmail(emailEdtTxt.getText().toString().trim());
        studentData.setParentName(parentNameEdtTxt.getText().toString().trim());
        studentData.setDateOfBirth(dateOfBirthEdtTxt.getText().toString().trim());
        studentData.setDateOfArrival(dateOfArrivalEdtTxt.getText().toString().trim());
        studentData.setAge(ageEdtTxt.getText().toString().trim());
        studentData.setCity(cityEdtTxt.getText().toString().trim());
        studentData.setImage("");

        updateInDatabase();
    }

    private void updateInDatabase() {
        showProgress(true);

        ApplicationClass.documentSnapshot.getReference().set(studentData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    updateStudentMarks();
                    Toast.makeText(EditStudentActivity.this, "Student Data Updated", Toast.LENGTH_SHORT).show();
                    finish();


                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(EditStudentActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

    }

    private void updateStudentMarks() {
        studentMarks = getStudentMarks();

        studentMarks.setFullName(studentData.getFullName());
        studentMarks.setEmail(studentData.getEmail());
        studentMarks.setClassGrade(studentData.getClassGrade());

        showProgress(true);
        Map<String,Object>map=new HashMap<>();
        map.put("fullName",studentData.getFullName());
        map.put("classGrade",studentData.getClassGrade());
        map.put("email",studentData.getEmail());

        FirebaseFirestore.getInstance().collection("StudentsMarks").document(studentData.getId()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(EditStudentActivity.this, "Student Marks updated", Toast.LENGTH_SHORT).show();

                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(EditStudentActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });


    }

    private StudentMarks getStudentMarks() {
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getEmail().equals(email)) {
                return studentMarks;
            }
        }


        return null;

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        ApplicationClass.onNavigationItemSelected(this,menuItem.getItemId());

        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

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

        classGradeSpinner = findViewById(R.id.classGradeSpinner);
        nationalitySpinner = findViewById(R.id.nationalitySpinner);
        religionSpinner = findViewById(R.id.religionSpinner);
        classTeacherNameSpinner = findViewById(R.id.classTeacherNameSpinner);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);

        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        parentNameEdtTxt = findViewById(R.id.parentNameEdtTxt);
        dateOfBirthEdtTxt = findViewById(R.id.dateOfBirthEdtTxt);
        dateOfArrivalEdtTxt = findViewById(R.id.dateOfArrivalEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);

        ////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);

        /////////////////RADIOGROUP///////////////

        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        maleRadioBtn = findViewById(R.id.maleRadioBtn);
        femaleRadioBtn = findViewById(R.id.femaleRadioBtn);
        otherRadioBtn = findViewById(R.id.otherRadioBtn);


        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        submitBtn = findViewById(R.id.submitBtn);

        setValuesForSpinner();
    }

    private void setValuesForSpinner() {
        String[] classGrade = {"1", "2", "3", "4", "5", "6", "7", "8"};
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classGrade);
        classGradeSpinner.setAdapter(arrayAdapter1);

        String[] cities = {"Kisumu", "Kitui", "Lamu", "Machakos", "Marsabit", "Meru", "Migori", "Mombasa", "Nakuru", "Narok", "Trans Nzoia", "Turkana", "Vihiga", "Naivasha", "Eldoret", "Kericho"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        cityEdtTxt.setAdapter(cityAdapter);


        String[] nationality = {"Kenyan", "Foreigner"};
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nationality);
        nationalitySpinner.setAdapter(arrayAdapter2);

        String[] religion = {"Christian", "Muslim"};
        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, religion);
        religionSpinner.setAdapter(arrayAdapter3);

        setValuesForClassTeacherNameSpinner();


    }

    private void setValuesForClassTeacherNameSpinner() {
        List<String> list = new ArrayList<>();
        for (TeacherData teacherData : AllData.teacherDataList) {
            list.add(teacherData.getFullName());

        }
        ArrayAdapter<String> arrayAdapter4 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list);
        classTeacherNameSpinner.setAdapter(arrayAdapter4);


    }
}
