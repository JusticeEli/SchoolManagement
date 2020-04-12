package com.justice.schoolmanagement.teacher;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;

public class AddTeacherActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt;
    private EditText lastNameEdtTxt;
    private EditText contactEdtTxt;
    private EditText emailEdtTxt;
    private EditText salaryEdtTxt;
    private EditText cityEdtTxt;
    private EditText degreeEdtTxt;
    private EditText ageEdtTxt;
    private Spinner subjectSpinner;
    private RadioGroup genderRadioGroup;
    private RadioGroup typeRadioGroup;

    private Button addPhotoBtn;
    private Button addBtn;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;


    private TeacherData teacherData;

    private BackendlessUser user;

    private CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Teachers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);
        initWidgets();
        setDefaultValues();
        initNavigationDrawer();

        setOnClickListeners();
    }

    private void setDefaultValues() {
        contactEdtTxt.setText("07");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AddTeacherActivity.this, "Not Yet implemented", Toast.LENGTH_SHORT).show();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDataFromEdtTxtAndAddItToDatabase();

            }


        });
    }

    private void resetEdtTxt() {
        firstNameEdtTxt.setText("");
        lastNameEdtTxt.setText("");
        emailEdtTxt.setText("");
        salaryEdtTxt.setText("");
        cityEdtTxt.setText("");
        degreeEdtTxt.setText("");
        ageEdtTxt.setText("");
        contactEdtTxt.setText("");
    }

    private boolean fieldsAreEmpty() {

        if (firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || contactEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || salaryEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty() || degreeEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }

        return false;
    }

    private void getDataFromEdtTxtAndAddItToDatabase() {
        if (fieldsAreEmpty()) {
            Toast.makeText(AddTeacherActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!contactEdtTxtFormatIsCorrect()) {
            return;
        }
        teacherData = new TeacherData(firstNameEdtTxt.getText().toString().trim() + " " + lastNameEdtTxt.getText().toString().trim(), firstNameEdtTxt.getText().toString().trim(), lastNameEdtTxt.getText().toString().trim(), emailEdtTxt.getText().toString().trim(), salaryEdtTxt.getText().toString().trim(), cityEdtTxt.getText().toString().trim(), degreeEdtTxt.getText().toString().trim(), ageEdtTxt.getText().toString().trim(), getSelectedGenderRadioBtn(), getSelectedTypeRadioBtn(), "photo", subjectSpinner.getSelectedItem().toString(), contactEdtTxt.getText().toString().trim());
        registerTeacherAndPutDataInDatabase();

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

    private void registerTeacherAndPutDataInDatabase() {

        String email = emailEdtTxt.getText().toString().trim();
        String password = "teacher1";
        showProgress(true);
        ////password one letter may cause a problem
        showProgress(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    putTeacherDataInDatabase();
                    Toast.makeText(AddTeacherActivity.this, "Teacher Registered Successfully", Toast.LENGTH_SHORT).show();

                }else{
                   String error=task.getException().getMessage();
                    Toast.makeText(AddTeacherActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);

            }
        });

    }


    private void putTeacherDataInDatabase() {
        showProgress(true);
        collectionReference.add(teacherData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddTeacherActivity.this, "Teacher Data Saved", Toast.LENGTH_SHORT).show();
                    resetEdtTxt();
                    finish();


                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddTeacherActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
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
        firstNameEdtTxt = findViewById(R.id.firstNameEdtTxt);
        lastNameEdtTxt = findViewById(R.id.lastNameEdtTxt);
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        salaryEdtTxt = findViewById(R.id.salaryEdtTxt);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);
        degreeEdtTxt = findViewById(R.id.degreeEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        contactEdtTxt = findViewById(R.id.contactEdtTxt);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addBtn = findViewById(R.id.addBtn);


////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);

        String[] subjects = {"Math", "Science", "English", "Kiswahili", "sst_cre"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        subjectSpinner.setAdapter(arrayAdapter);


    }
}
