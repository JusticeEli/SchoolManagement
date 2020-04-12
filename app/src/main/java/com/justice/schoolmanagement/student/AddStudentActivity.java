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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.class_.ChoosenClassActivity;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.parent.AddParentActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.teacher.TeacherData;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.ArrayList;
import java.util.List;

public class AddStudentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt, lastNameEdtTxt, emailEdtTxt, parentNameEdtTxt, dateOfBirthEdtTxt, dateOfArrivalEdtTxt, ageEdtTxt;
    private AutoCompleteTextView cityEdtTxt;
    private Spinner classGradeSpinner, nationalitySpinner, religionSpinner, classTeacherNameSpinner;
    private RadioGroup genderRadioGroup;

    private Button addPhotoBtn, addBtn;
    private StudentData studentData;
    private StudentMarks studentMarks;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CollectionReference collectionReferenceMarks = FirebaseFirestore.getInstance().collection("StudentsMarks");
    private CollectionReference collectionReferenceData = FirebaseFirestore.getInstance().collection("Students");
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        initWidgets();
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

    private void setOnClickListeners() {
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(AddStudentActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                getDataFromEdtTxtAndSaveInDatabase();

            }
        });
    }

    private boolean fieldsAreEmpty() {
        if (firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || parentNameEdtTxt.getText().toString().trim().isEmpty() || dateOfBirthEdtTxt.getText().toString().trim().isEmpty() || dateOfArrivalEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    private void resetEdtTxt() {
        firstNameEdtTxt.setText("");
        lastNameEdtTxt.setText("");
        emailEdtTxt.setText("");
        parentNameEdtTxt.setText("");
        dateOfBirthEdtTxt.setText("");
        dateOfArrivalEdtTxt.setText("");
        ageEdtTxt.setText("");
        cityEdtTxt.setText("");

    }

    private void getDataFromEdtTxtAndSaveInDatabase() {
        studentData = new StudentData();

        studentData.setFullName(firstNameEdtTxt.getText().toString() + " " + lastNameEdtTxt.getText().toString());
        studentData.setClassGrade(Integer.parseInt(classGradeSpinner.getSelectedItem().toString()));
        studentData.setFirstName(firstNameEdtTxt.getText().toString());
        studentData.setLastName(lastNameEdtTxt.getText().toString());
        studentData.setNationality(nationalitySpinner.getSelectedItem().toString());
        studentData.setReligion(religionSpinner.getSelectedItem().toString());
        studentData.setEmail(emailEdtTxt.getText().toString());
        studentData.setParentName(parentNameEdtTxt.getText().toString());
        studentData.setDateOfBirth(dateOfBirthEdtTxt.getText().toString());
        studentData.setDateOfArrival(dateOfArrivalEdtTxt.getText().toString());
        studentData.setAge(ageEdtTxt.getText().toString());
        studentData.setGender(getSelectedRadioBtn());
        studentData.setClassTeacherName(classTeacherNameSpinner.getSelectedItem().toString());
        studentData.setCity(cityEdtTxt.getText().toString());
        studentData.setImage("");

        putDataIntoDatabase();

    }

    private void putDataIntoDatabase() {
        showProgress(true);

        collectionReferenceData.add(studentData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    documentReference = task.getResult();
                    addStudentMarks();
                    resetEdtTxt();
                    Toast.makeText(AddStudentActivity.this, "Student Added Successfully", Toast.LENGTH_SHORT).show();
                    addParent();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddStudentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }
                showProgress(false);
            }
        });

    }

    private void addParent() {
        Intent intent = new Intent(this, AddParentActivity.class);
        intent.putExtra("flag", true);
        intent.putExtra("parentName", studentData.getParentName());
        intent.putExtra("parentEmail", studentData.getEmail());

        startActivity(intent);

    }

    private void addStudentMarks() {
        studentMarks = new StudentMarks();
        studentMarks.setFullName(studentData.getFullName());
        studentMarks.setEmail(studentData.getEmail());
        studentMarks.setClassGrade(studentData.getClassGrade());
        showProgress(true);
        collectionReferenceMarks.document(documentReference.getId()).set(studentMarks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddStudentActivity.this, "Student Marks Added", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddStudentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        ApplicationClass.onNavigationItemSelected(this, menuItem.getItemId());

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

        /////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);


        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addBtn = findViewById(R.id.addBtn);

        setValuesForSpinner();

    }

    private void setValuesForSpinner() {
        String[] classGrade = {"1", "2", "3", "4", "5", "6", "7", "8"};
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classGrade);
        classGradeSpinner.setAdapter(arrayAdapter1);

        String[] cities = {"Kisumu", "Kitui", "Lamu", "Nairobi", "Machakos", "Marsabit", "Meru", "Migori", "Mombasa", "Nakuru", "Narok", "Trans Nzoia", "Turkana", "Vihiga", "Naivasha", "Eldoret", "Kericho"};
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
