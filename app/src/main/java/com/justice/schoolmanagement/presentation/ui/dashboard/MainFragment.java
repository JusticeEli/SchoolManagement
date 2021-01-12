package com.justice.schoolmanagement.presentation.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

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
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.blog.BlogActivity;
import com.justice.schoolmanagement.presentation.ApplicationClass;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


public class MainFragment extends Fragment implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
    public static final int RC_SIGN_IN = 5;
    private static final String TAG = "DashBoardActivity";

    private View view;

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


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        initWidgets();
        checkIfUserIsLoggedIn();
        //initNavigationDrawer();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                //is sign in is success we want to recreate the activity
                Log.d(TAG, "onActivityResult: success sign in");
                Log.d(TAG, "onActivityResult: recreating the activity");

                recreateFragment();
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

    private void recreateFragment() {
        getFragmentManager()
            .beginTransaction()
            .detach(this)
            .attach(this)
            .addToBackStack(null)
            .commit();

    }

    private void showToast(String message) {
        Toasty.error(getActivity(), message).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_blog, menu);


    }


    ////////////////////////NAVIGATION DRAWER/////////////////////////////////////////////
  /*  private void initNavigationDrawer() {
        DrawerLayout drawerLayout = view.findViewById(R.id.drawer);

        navigationView = view.findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }*/

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
                destroyFragment();
                break;
            case R.id.blogsMenu:
                startActivity(new Intent(getActivity(), BlogActivity.class));
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void destroyFragment() {
        getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        ApplicationClass.onNavigationItemSelected(getActivity(), menuItem.getItemId());
        DrawerLayout drawerLayout = view.findViewById(R.id.drawer);

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
        teachersBtn = view.
                findViewById(R.id.teachersBtn);
        parentsBtn = view.
                findViewById(R.id.parentsBtn);
        studentsBtn = view.
                findViewById(R.id.studentsBtn);
        classessBtn = view.
                findViewById(R.id.classesBtn);
        subjectsBtn = view.
                findViewById(R.id.subjectsBtn);
        resultsBtn = view.findViewById(R.id.resultsBtn);
        ;

        ////////////////PROGRESS_BAR//////////////////////
        load = view.findViewById(R.id.loadingLinearLayout);
        loadTxtView =view. findViewById(R.id.loadTxtView);
        relativeLayout = view.findViewById(R.id.relativeLayout);

        teacherCardView =view. findViewById(R.id.teacherCardView);

    }

    @Override
    public void onClick(View v) {

    }
    //    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.teachersBtn:
//                Intent intent = new Intent(MainActivity.this, TeachersActivity.class);
//                startActivity(intent);
//                break;
//
//            case R.id.parentsBtn:
//                Intent intent2 = new Intent(MainActivity.this, ParentsActivity.class);
//                startActivity(intent2);
//                break;
//            case R.id.studentsBtn:
//                Intent intent3 = new Intent(MainActivity.this, StudentsActivity.class);
//                startActivity(intent3);
//                break;
//            case R.id.classesBtn:
//                Intent intent4 = new Intent(MainActivity.this, ClassesActivity.class);
//                startActivity(intent4);
//                break;
//            case R.id.subjectsBtn:
//                Intent intent5 = new Intent(MainActivity.this, SubjectsActivity.class);
//                startActivity(intent5);
//                break;
//            case R.id.resultsBtn:
//                Intent intent6 = new Intent(MainActivity.this, ResultsActivity.class);
//                startActivity(intent6);
//                break;
//
//
//        }
//    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        //if user is not logged in we want to exit this method
        if (firebaseAuth.getCurrentUser() == null) {
            Log.d(TAG, "onStart: user not signed in");
            return;
        }

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Teachers");
        collectionReference.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener( new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "onEvent: Error: " + e.getMessage());
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!documentSnapshot.exists()) {
                    //teacher metadata does not exit
                    Log.d(TAG, "onEvent: teacher metadata does not exit going to AddTeacherActivity");

//                    Intent intent=new Intent(getContext(), AddTeacherActivity.class);
//                    startActivity(intent);
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