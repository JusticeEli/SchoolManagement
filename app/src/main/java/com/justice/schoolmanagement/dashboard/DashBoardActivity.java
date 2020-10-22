package com.justice.schoolmanagement.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.blog.BlogActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.AddTeacherActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;

public class DashBoardActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    public static final int RC_SIGN_IN = 5;
    private static final String TAG = "DashBoardActivity";

    private Button teachersBtn, parentsBtn, studentsBtn, classessBtn, subjectsBtn, resultsBtn;
    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private RelativeLayout relativeLayout;

    private CardView teacherCardView;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        initWidgets();

        checkIfUserIsLoggedIn();
        initNavigationDrawer();
        setOnClickListeners();
    }

    private void checkIfUserIsLoggedIn() {
        Log.d(TAG, "checkIfUserIsLoggedIn: checking if user is logged in");
        if (firebaseAuth.getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                                    new AuthUI.IdpConfig.AnonymousBuilder().build()))
                            .build(),
                    RC_SIGN_IN);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                //is sign in is success we want to recreate the activity
                Log.d(TAG, "onActivityResult: success sign in");
                Log.d(TAG, "onActivityResult: recreating the activity");

                recreate();
            } else {
                // Sign in failed
                Log.d(TAG, "onActivityResult: sign in failed");
                if (response == null) {
                    // User pressed back button
                    showToast("sign in cancelled");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showToast("not internet connection");
                    return;
                }

                showToast("unknown error");
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }
private  void showToast(String message){
    Toasty.error(this,message).show();
}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sort, menu);

        return super.onCreateOptionsMenu(menu);

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
        teachersBtn.setOnClickListener(this);
        parentsBtn.setOnClickListener(this);
        studentsBtn.setOnClickListener(this);
        classessBtn.setOnClickListener(this);
        subjectsBtn.setOnClickListener(this);
        resultsBtn.setOnClickListener(this);
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
            case R.id.blogsMenu:
                startActivity(new Intent(this, BlogActivity.class));
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        ApplicationClass.onNavigationItemSelected(this, menuItem.getItemId());
        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);


        }

    }


    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        teachersBtn = findViewById(R.id.teachersBtn);
        parentsBtn = findViewById(R.id.parentsBtn);
        studentsBtn = findViewById(R.id.studentsBtn);
        classessBtn = findViewById(R.id.classesBtn);
        subjectsBtn = findViewById(R.id.subjectsBtn);
        resultsBtn = findViewById(R.id.resultsBtn);

        ////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        relativeLayout = findViewById(R.id.relativeLayout);

        teacherCardView = findViewById(R.id.teacherCardView);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.teachersBtn:
                Intent intent = new Intent(DashBoardActivity.this, TeachersActivity.class);
                startActivity(intent);
                break;

            case R.id.parentsBtn:
                Intent intent2 = new Intent(DashBoardActivity.this, ParentsActivity.class);
                startActivity(intent2);
                break;
            case R.id.studentsBtn:
                Intent intent3 = new Intent(DashBoardActivity.this, StudentsActivity.class);
                startActivity(intent3);
                break;
            case R.id.classesBtn:
                Intent intent4 = new Intent(DashBoardActivity.this, ClassesActivity.class);
                startActivity(intent4);
                break;
            case R.id.subjectsBtn:
                Intent intent5 = new Intent(DashBoardActivity.this, SubjectsActivity.class);
                startActivity(intent5);
                break;
            case R.id.resultsBtn:
                Intent intent6 = new Intent(DashBoardActivity.this, ResultsActivity.class);
                startActivity(intent6);
                break;


        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        //if user is not logged in we want to exit this method
        if (firebaseAuth.getCurrentUser()==null){
            Log.d(TAG, "onStart: user not signed in");
            return;
        }

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Teachers");
        collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "onEvent: Error: "+e.getMessage());
                    Toast.makeText(DashBoardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!documentSnapshot.exists()) {
                    //teacher metadata does not exit
                    Log.d(TAG, "onEvent: teacher metadata does not exit going to AddTeacherActivity");
                    startActivity(new Intent(DashBoardActivity.this, AddTeacherActivity.class));

                } else {
                    if (documentSnapshot.getString("type").equals("teacher")) {
                        //its a teacher not admin
                        Log.d(TAG, "onEvent: its a teacher not admin");
                        teacherCardView.setVisibility(View.GONE);
                    }
                }

            }
        });


    }


}
