package com.justice.schoolmanagement.student;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.ApplicationClass;

import es.dmoral.toasty.Toasty;

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
    private KenBurnsView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);

        studentData = ApplicationClass.documentSnapshot.toObject(StudentData.class);
        studentData.setId(ApplicationClass.documentSnapshot.getId());
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


    @Override
    protected void onResume() {
        super.onResume();
        studentData = ApplicationClass.documentSnapshot.toObject(StudentData.class);
        studentData.setId(ApplicationClass.documentSnapshot.getId());
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


        new MaterialAlertDialogBuilder(StudentDetailsActivity.this).setBackground(getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteStudent();
            }
        }).show();


    }

    private void deleteStudent() {
        showProgress(true);

        FirebaseStorage.getInstance().getReferenceFromUrl(studentData.getPhoto()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(StudentDetailsActivity.this, "Photo Deleted", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(StudentDetailsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });
        ApplicationClass.documentSnapshot.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showProgress(true);
                    ApplicationClass.documentSnapshot.getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                ApplicationClass.documentSnapshot = task.getResult();
                                removeStudentMarksFromDatabase();
                                Toasty.success(StudentDetailsActivity.this, "Student data Removed", Toast.LENGTH_SHORT).show();
                                finish();

                            } else {
                                String error = task.getException().getMessage();
                                Toasty.error(StudentDetailsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                            }
                            showProgress(false);

                        }
                    });

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(StudentDetailsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });


    }

    private void removeStudentMarksFromDatabase() {

        showProgress(true);
        FirebaseFirestore.getInstance().collection("StudentsMarks").document(studentData.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(StudentDetailsActivity.this, "Student Marks removed", Toast.LENGTH_SHORT).show();


                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(StudentDetailsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }
                showProgress(false);
            }
        });

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

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(studentData.getPhoto()).thumbnail(Glide.with(this).load(studentData.getThumbnail())).into(imageView);


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
        ApplicationClass.onNavigationItemSelected(this, menuItem.getItemId());
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

        imageView = findViewById(R.id.imageView);
    }
}
