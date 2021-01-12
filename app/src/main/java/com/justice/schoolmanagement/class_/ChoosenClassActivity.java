package com.justice.schoolmanagement.class_;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.justice.schoolmanagement.presentation.ui.class_.ChoosenClassActivityResultsRecyclerAdapter;
import com.justice.schoolmanagement.presentation.ui.class_.ChoosenClassActivityStudentsRecyclerAdapter;
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData;
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks;

import java.util.ArrayList;
import java.util.List;

public class ChoosenClassActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
//////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    private int class_;

    private ChoosenClassActivityResultsRecyclerAdapter choosenClassActivityResultsRecyclerAdapter;
    private ChoosenClassActivityStudentsRecyclerAdapter choosenClassActivityStudentsRecyclerAdapter;


    private List<StudentData> choosenClassStudentDataList = new ArrayList<>();
    private List<StudentMarks> choosenClassStudentMarkList = new ArrayList<>();

    private TextView headerTxtView;
    private Button listOfStudentBtn, resultsBtn;
    private EditText searchEdtTxt;
    private RecyclerView listOfStudentRecyclerView, resultsRecyclerView;
    private int counter=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_choosen_class);
        class_ = getIntent().getIntExtra("class", 0);
        initWidgets();
        setUpRecyclerView();
        initNavigationDrawer();
        setDefaultValues();
        setOnClickListeners();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_blog, menu);
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




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        ApplicationClass.onNavigationItemSelected(this,menuItem.getItemId());
        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
             }

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }



    private void setDefaultValues() {
        switch (class_) {
            case 1:
                headerTxtView.setText("Class 1");
                getClass_1_data();
                break;
            case 2:
                headerTxtView.setText("Class 2");
                getClass_2_data();
                break;
            case 3:
                headerTxtView.setText("Class 3");
                getClass_3_data();
                break;
            case 4:
                headerTxtView.setText("Class 4");
                getClass_4_data();
                break;
            case 5:
                headerTxtView.setText("Class 5");
                getClass_5_data();
                break;
            case 6:
                headerTxtView.setText("Class 6");
                getClass_6_data();
                break;
            case 7:
                headerTxtView.setText("Class 7");
                getClass_7_data();
                break;
            case 8:
                headerTxtView.setText("Class 8");
                getClass_8_data();
                break;
            default:
                break;

        }
    }

    private void getClass_1_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 1) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 1) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_2_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 2) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 2) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_3_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 3) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 3) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_4_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 4) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 4) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_5_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 5) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 5) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_6_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 6) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 6) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_7_data() {
        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 7) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 7) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void getClass_8_data() {

        for (StudentData studentData : AllData.studentDataList) {
            if (studentData.getClassGrade() == 8) {
                choosenClassStudentDataList.add(studentData);
            }
        }
        for (StudentMarks studentMarks : AllData.studentMarksList) {
            if (studentMarks.getClassGrade() == 8) {
                choosenClassStudentMarkList.add(studentMarks);
            }
        }
    }

    private void setOnClickListeners() {
        setOnClickListenerForSearchEdtTxt();
        listOfStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listOfStudentRecyclerView.setVisibility(View.VISIBLE);
                resultsRecyclerView.setVisibility(View.GONE);

            }
        });
        resultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listOfStudentRecyclerView.setVisibility(View.GONE);
                resultsRecyclerView.setVisibility(View.VISIBLE);


            }
        });

    }

    private void setOnClickListenerForSearchEdtTxt() {

    }

    /////////////////////PROGRESS_BAR////////////////////////////
    public void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            resultsRecyclerView.setVisibility(View.GONE);
            listOfStudentRecyclerView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            resultsRecyclerView.setVisibility(View.VISIBLE);
            listOfStudentRecyclerView.setVisibility(View.VISIBLE);


        }

    }


    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        headerTxtView = findViewById(R.id.headerTxtView);
        listOfStudentBtn = findViewById(R.id.listOfStudentBtn);
        resultsBtn = findViewById(R.id.resultsBtn);
        searchEdtTxt = findViewById(R.id.searchEdtTxt);


////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);





    }

    private void setUpRecyclerView() {
        //////////////////STUDENTS/////////////////////////////
        listOfStudentRecyclerView = findViewById(R.id.listOfStudentRecyclerView);
        Query query = FirebaseFirestore.getInstance().collection("Students").whereEqualTo("classGrade",class_);
        FirestoreRecyclerOptions<StudentData> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<StudentData>().setQuery(query, new SnapshotParser<StudentData>() {
            @NonNull
            @Override
            public StudentData parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                StudentData studentData = snapshot.toObject(StudentData.class);
                studentData.setId(snapshot.getId());
                return studentData;
            }
        }).setLifecycleOwner(ChoosenClassActivity.this).build();


        choosenClassActivityStudentsRecyclerAdapter = new ChoosenClassActivityStudentsRecyclerAdapter(this,firestoreRecyclerOptions);
        listOfStudentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listOfStudentRecyclerView.setAdapter(choosenClassActivityStudentsRecyclerAdapter);










        ////////////////////////////////RESULTS/////////////////////////
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        Query query2 = FirebaseFirestore.getInstance().collection("StudentsMarks").whereEqualTo("classGrade",class_).orderBy("totalMarks", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<StudentMarks> recyclerOptions=new FirestoreRecyclerOptions.Builder<StudentMarks>().setLifecycleOwner(this).setQuery(query2, new SnapshotParser<StudentMarks>() {
            @NonNull
            @Override
            public StudentMarks parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                StudentMarks studentMarks=snapshot.toObject(StudentMarks.class);
                studentMarks.setPosition(counter++);
                studentMarks.setId(snapshot.getId());
                return studentMarks;
            }
        }).build();

        choosenClassActivityResultsRecyclerAdapter = new ChoosenClassActivityResultsRecyclerAdapter(this,recyclerOptions);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(choosenClassActivityResultsRecyclerAdapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        choosenClassActivityResultsRecyclerAdapter.notifyDataSetChanged();
        choosenClassActivityStudentsRecyclerAdapter.notifyDataSetChanged();
    }
}
