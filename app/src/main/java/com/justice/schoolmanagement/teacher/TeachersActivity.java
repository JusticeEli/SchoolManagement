package com.justice.schoolmanagement.teacher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;

import java.util.ArrayList;
import java.util.List;

public class TeachersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TeachersActivityRecyclerAdapter teachersActivityRecyclerAdapter;
    private RecyclerView recyclerView;
 //   private FloatingActionButton addTeacherBtn;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        initwidgets();
        initNavigationDrawer();

     //   setOnClickListeners();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchItem=menu.findItem(R.id.searchItem);
        SearchView searchView=(SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        ApplicationClass.onNavigationItemSelected(this, menuItem.getItemId());

        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    public void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);


        }

    }


    private void initwidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.recyclerView);

        ////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        Query query = firebaseFirestore.collection("Teachers");
        FirestoreRecyclerOptions<TeacherData> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<TeacherData>().setQuery(query, new SnapshotParser<TeacherData>() {
            @NonNull
            @Override
            public TeacherData parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                TeacherData teacherData = snapshot.toObject(TeacherData.class);
                teacherData.setId(snapshot.getId());
                return teacherData;
            }
        }).setLifecycleOwner(TeachersActivity.this).build();
        teachersActivityRecyclerAdapter = new TeachersActivityRecyclerAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(teachersActivityRecyclerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
