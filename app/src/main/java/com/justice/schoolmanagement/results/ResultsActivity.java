package com.justice.schoolmanagement.results;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.presentation.ApplicationClass;
import com.justice.schoolmanagement.presentation.ui.results.ResultsActivityRecyclerAdapter;
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private EditText searchEdtTxt;
    private ResultsActivityRecyclerAdapter resultsActivityRecyclerAdapter;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private int counter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_results);
        initWidgets();
        setUpRecyclerView();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_blog, menu);

        return true;
    }


    private void setOnClickListeners() {
        searchEdtTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<StudentMarks> list = new ArrayList<>();
                if (searchEdtTxt.getText().toString().isEmpty()) {
                } else {

                    for (StudentMarks studentMarks : AllData.studentMarksList) {
                        if (studentMarks.getName().toLowerCase().contains(searchEdtTxt.getText().toString().toLowerCase())) {
                            if (!list.contains(studentMarks)) {
                                list.add(studentMarks);
                            }
                        }
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initWidgets() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchEdtTxt = findViewById(R.id.searchEdtTxt);
    }

    private void setUpRecyclerView() {
        Query query = FirebaseFirestore.getInstance().collection("StudentsMarks").orderBy("totalMarks", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<StudentMarks> recyclerOptions = new FirestoreRecyclerOptions.Builder<StudentMarks>().setLifecycleOwner(this).setQuery(query, new SnapshotParser<StudentMarks>() {
            @NonNull
            @Override
            public StudentMarks parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                StudentMarks studentMarks = snapshot.toObject(StudentMarks.class);
                studentMarks.setPosition(counter++);
                studentMarks.setId(snapshot.getId());
                return studentMarks;
            }
        }).build();

        recyclerView = findViewById(R.id.recyclerView);
        resultsActivityRecyclerAdapter = new ResultsActivityRecyclerAdapter(this, recyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(resultsActivityRecyclerAdapter);

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
    protected void onResume() {
        super.onResume();
        sortStudentMarks();
    }

    private void sortStudentMarks() {
        int x = 1;
        Collections.sort(AllData.studentMarksList);
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            studentMarks.setPosition(x++);
        }


    }
}
