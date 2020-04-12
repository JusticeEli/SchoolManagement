package com.justice.schoolmanagement.parent;

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
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.class_.ChoosenClassActivity;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;

public class ParentDetailsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ParentData parentData;
    private String email;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private CardView cardView;

    private ImageView callImageView,emailImageView;


    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;





    private TextView parentNameTxtView, contactTxtView, firstNameTxtView, lastNameTxtView, cityTxtView, jobStatusTxtView, ageTxtView, genderTxtView, jobTypeTxtView, deleteTxtView, editTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_details);
        email = getIntent().getStringExtra("email");

        parentData = getParentData();
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
                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+parentData.getContact()));
                startActivity(intent);
            }
        });

        emailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                String email[]={parentData.getEmail()};
                intent.putExtra(Intent.EXTRA_EMAIL,email);
                startActivity(Intent.createChooser(intent,"Choose app to use for sending Email"));
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

    private ParentData getParentData() {
        for (ParentData parentData:AllData.parentDataList){
            if (parentData.getEmail().equals(email)){
                return parentData;
            }
        }


        return null;
    }

    private void setOnClickListeners() {
        deleteTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromDatabase();

            }
        });
        editTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentDetailsActivity.this, EditParentActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }

    private void deleteFromDatabase() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Delete").setMessage("Are You Sure you Want To delete!!").setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteParent();
            }
        });
        builder.show();
    }

    private void deleteParent() {
        showProgress(true);
        Backendless.Persistence.of(ParentData.class).remove(parentData, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                showProgress(false);
                AllData.parentDataList.remove(parentData);
                Toast.makeText(ParentDetailsActivity.this, parentData.getFirstName() + " Removed Successfully", Toast.LENGTH_SHORT).show();
                finish();


            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(ParentDetailsActivity.this, "Error: "+fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void setDefaultValues() {
        parentNameTxtView.setText(parentData.getFirstName() + " " + parentData.getLastName());
        contactTxtView.setText(parentData.getContact());
        firstNameTxtView.setText(parentData.getFirstName());
        lastNameTxtView.setText(parentData.getLastName());
        cityTxtView.setText(parentData.getCity());
        jobStatusTxtView.setText(parentData.getJobStatus());
        ageTxtView.setText(parentData.getAge());
        genderTxtView.setText(parentData.getGender());
        jobTypeTxtView.setText(parentData.getJobType());

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
                        Toast.makeText(ParentDetailsActivity.this, "Logout Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(ParentDetailsActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

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
            cardView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);


        }

    }


    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parentNameTxtView = findViewById(R.id.parentNameTxtView);
        contactTxtView = findViewById(R.id.contactTxtView);
        firstNameTxtView = findViewById(R.id.firstNameTxtView);
        lastNameTxtView = findViewById(R.id.lastNameTxtView);
        cityTxtView = findViewById(R.id.cityTxtView);
        jobStatusTxtView = findViewById(R.id.jobStatusTxtView);
        ageTxtView = findViewById(R.id.ageTxtView);
        genderTxtView = findViewById(R.id.genderTxtView);
        jobTypeTxtView = findViewById(R.id.jobTypeTxtView);

        deleteTxtView = findViewById(R.id.deleteTxtView);
        editTxtView = findViewById(R.id.editTxtView);

        /////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        cardView = findViewById(R.id.cardView);

        callImageView=findViewById(R.id.callImageView);
        emailImageView=findViewById(R.id.emailImageView);



    }

    @Override
    protected void onResume() {
        super.onResume();
        setDefaultValues();
    }
}
