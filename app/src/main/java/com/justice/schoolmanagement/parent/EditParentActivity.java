package com.justice.schoolmanagement.parent;

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
import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;

public class EditParentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt;
    private EditText lastNameEdtTxt;
    private EditText emailEdtTxt;
    private AutoCompleteTextView cityEdtTxt;
    private EditText contactEdtTxt;
    private Spinner jobStatusSpinner;
    private EditText ageEdtTxt;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioBtn;
    private RadioButton femaleRadioBtn;
    private RadioButton otherRadioBtn;

    private AutoCompleteTextView jobTypeEdtTxt;

    private Button addPhotoBtn;
    private Button submitBtn;

    private ParentData parentData;
    private String email;

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
        setContentView(R.layout.activity_edit_parent);
        email = getIntent().getStringExtra("email");
        parentData = getParentData();
        initWidgets();
        initAdapters();
        initNavigationDrawer();


        setDefaulValues();
        setOnClickListeners();
    }

    private void initAdapters() {
        String[] jobStatus = {"Employed", "Unemployed", "Retired"};
        ArrayAdapter<String> jobStatusAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, jobStatus);
        jobStatusSpinner.setAdapter(jobStatusAdapter);

        String[] cities = {"Kisumu", "Kitui", "Lamu", "Machakos", "Marsabit", "Meru", "Migori", "Mombasa", "Nakuru", "Narok", "Trans Nzoia", "Turkana", "Vihiga", "Naivasha", "Eldoret", "Kericho"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        cityEdtTxt.setAdapter(cityAdapter);


        String[] jobType = {"medical", "health", "administrative", "secretarial", "sales", "marketing", "finance", "auditing", "accounting", "education", "ngo", "ict", "building", "construction", "procument", "engineering", "media", "computer", "human resource", "law", "research", "manufacturing", "hospitality"};
        ArrayAdapter<String> jobTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jobType);
        jobTypeEdtTxt.setAdapter(jobTypeAdapter);

    }

    private ParentData getParentData() {
        for (ParentData parentData : AllData.parentDataList) {
            if (parentData.getEmail().equals(email)) {
                return parentData;
            }
        }


        return null;
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

    private void setDefaulValues() {
        firstNameEdtTxt.setText(parentData.getFirstName());
        lastNameEdtTxt.setText(parentData.getLastName());
        emailEdtTxt.setText(parentData.getEmail());
        setJobStatusDefaultValue();
        cityEdtTxt.setText(parentData.getCity());
        jobTypeEdtTxt.setText(parentData.getJobType());
        ageEdtTxt.setText(parentData.getAge());
        setDefaultValueForRadioBtn();
        contactEdtTxt.setText(parentData.getContact());

    }

    private void setJobStatusDefaultValue() {
        switch (parentData.getJobStatus()) {
            case "Employed":
                jobStatusSpinner.setSelection(0);
                break;
            case "Unemployed":
                jobStatusSpinner.setSelection(1);
                break;
            case "Retired":
                jobStatusSpinner.setSelection(2);
                break;

        }
    }

    private void setDefaultValueForRadioBtn() {
        switch (parentData.getGender()) {
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
                        Toast.makeText(EditParentActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(EditParentActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

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
        jobStatusSpinner = findViewById(R.id.jobStatusSpinner);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);
        jobTypeEdtTxt = findViewById(R.id.jobTypeEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        maleRadioBtn = findViewById(R.id.maleRadioBtn);
        femaleRadioBtn = findViewById(R.id.femaleRadioBtn);
        otherRadioBtn = findViewById(R.id.otherRadioBtn);

        contactEdtTxt = findViewById(R.id.contactEdtTxt);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        submitBtn = findViewById(R.id.submitBtn);


////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);

        contactEdtTxt.setText("07");
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

    private void setOnClickListeners() {
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(EditParentActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!contactEdtTxtFormatIsCorrect()) {
                    return;
                }
                getDataFromEdtTxtAndSaveIntoDatabase();


            }
        });


    }

    private boolean fieldsAreEmpty() {

        if (firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty() || contactEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty() || jobTypeEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }

        return false;
    }


    private void getDataFromEdtTxtAndSaveIntoDatabase() {
        // TODO: 11-Feb-20  extract a photo from the addPhoto button
        parentData.setFullName(firstNameEdtTxt.getText().toString().trim() + " " + lastNameEdtTxt.getText().toString().trim());
        parentData.setContact(contactEdtTxt.getText().toString().trim());
        parentData.setFirstName(firstNameEdtTxt.getText().toString().trim());
        parentData.setLastName(lastNameEdtTxt.getText().toString().trim());
        parentData.setCity(cityEdtTxt.getText().toString().trim());
        parentData.setJobStatus(jobStatusSpinner.getSelectedItem().toString().trim());
        parentData.setAge(ageEdtTxt.getText().toString());
        parentData.setGender(getSelectedRadioBtn());
        parentData.setJobType(jobTypeEdtTxt.getText().toString().trim());
        parentData.setEmail(emailEdtTxt.getText().toString().trim());
        showProgress(true);
        Backendless.Persistence.save(parentData, new AsyncCallback<ParentData>() {
            @Override
            public void handleResponse(ParentData response) {
                showProgress(false);
                Toast.makeText(EditParentActivity.this, parentData.getFirstName() + " Edited Successfully", Toast.LENGTH_SHORT).show();
                finish();

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(EditParentActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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


}
