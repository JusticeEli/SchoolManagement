package com.justice.schoolmanagement.teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;


public class TeacherDetailsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TextView teacherNameTxtView, teacherSubjectTxtView, firstNameTxtView, lastNameTxtView, cityTxtView, degreeTxtView, ageTxtView, genderTxtView, salaryTxtView, emailTxtView, contactTxtView, deleteTxtView, editTxtView;
    private String email;
    private TeacherData teacherData;

    private ImageView callImageView, emailImageView;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private CardView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_details);
        email = getIntent().getStringExtra("email");
        teacherData = getTeacherData();

        initWidgets();
        initNavigationDrawer();

        setDefaultValues();
        setOnClickListeners();
        setImageViewClickListeners();

    }

    private void setImageViewClickListeners() {
        callImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + teacherData.getContact()));
                startActivity(intent);
            }
        });

        emailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                String email[] = {teacherData.getEmail()};
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                startActivity(Intent.createChooser(intent, "Choose app to use for sending Email"));
            }
        });


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

    private void setOnClickListeners() {
        deleteTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTeacherDataFromDatabase();
            }
        });
        editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDetailsActivity.this, EditTeacherActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDefaultValues();
    }

    private void deleteTeacherDataFromDatabase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTeacher();
            }
        });
        builder.show();
    }

    private void deleteTeacher() {

        showProgress(true);
        Backendless.Persistence.of(TeacherData.class).remove(teacherData, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                showProgress(false);
                AllData.teacherDataList.remove(teacherData);


                Toast.makeText(TeacherDetailsActivity.this, "Removed Teacher data From Database", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(TeacherDetailsActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDefaultValues() {
        teacherNameTxtView.setText(teacherData.getFirstName() + " " + teacherData.getLastName());
        teacherSubjectTxtView.setText(teacherData.getSubject());
        firstNameTxtView.setText(teacherData.getFirstName());
        lastNameTxtView.setText(teacherData.getLastName());
        cityTxtView.setText(teacherData.getCity());
        degreeTxtView.setText(teacherData.getDegree());
        ageTxtView.setText(teacherData.getAge());
        genderTxtView.setText(teacherData.getGender());
        salaryTxtView.setText(teacherData.getSalary());
        emailTxtView.setText(teacherData.getEmail());
        contactTxtView.setText(teacherData.getContact());
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
        teacherNameTxtView = findViewById(R.id.teacherNameTxtView);
        teacherSubjectTxtView = findViewById(R.id.teacherSubjectTxtView);
        firstNameTxtView = findViewById(R.id.firstNameTxtView);
        lastNameTxtView = findViewById(R.id.lastNameTxtView);
        cityTxtView = findViewById(R.id.cityTxtView);
        degreeTxtView = findViewById(R.id.degreeTxtView);
        ageTxtView = findViewById(R.id.ageTxtView);
        genderTxtView = findViewById(R.id.genderTxtView);
        salaryTxtView = findViewById(R.id.salaryTxtView);
        emailTxtView = findViewById(R.id.emailTxtView);
        contactTxtView = findViewById(R.id.contactTxtView);
        deleteTxtView = findViewById(R.id.deleteTxtView);
        editTxtView = findViewById(R.id.editTxtView);

        callImageView = findViewById(R.id.callImageView);
        emailImageView = findViewById(R.id.emailImageView);

        ///////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);


    }
}
