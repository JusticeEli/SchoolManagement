package com.justice.schoolmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.dashboard.MainActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeacherData;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.ArrayList;
import java.util.List;

import static android.R.id;
import static android.R.layout;

public class SubjectsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner spinner;
    private ListView listView;
    private List<String> list; //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);
        setTitle("Subjects");
        initWidgets();
        initNavigationDrawer();


        setSpinnerValues();
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

    private void setOnClickListeners() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setDataOnListView(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setDataOnListView(int position) {
        switch (position) {
            case 0:
                List<String> list = getMath();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, layout.simple_list_item_1, list);
                listView.setAdapter(arrayAdapter);
                break;
            case 1:
                List<String> list2 = getScience();
                ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this, layout.simple_list_item_1, list2);
                listView.setAdapter(arrayAdapter2);
                break;
            case 2:
                List<String> list3 = getEnglish();
                ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<String>(this, layout.simple_list_item_1, list3);
                listView.setAdapter(arrayAdapter3);
                break;
            case 3:
                List<String> list4 = getKiswahili();
                ArrayAdapter<String> arrayAdapter4 = new ArrayAdapter<String>(this, layout.simple_list_item_1, list4);
                listView.setAdapter(arrayAdapter4);
                break;
            case 4:
                List<String> list5 = getSst_cre();
                ArrayAdapter<String> arrayAdapter5 = new ArrayAdapter<String>(this, layout.simple_list_item_1, list5);
                listView.setAdapter(arrayAdapter5);
                break;

        }
    }

    private List<String> getMath() {
        list = new ArrayList<>();
        for (TeacherData teacherData : AllData.teacherDataList) {
            if (teacherData.getSubject().equals("Math")) {
                list.add(teacherData.getFullName());
            }
        }
        return list;
    }

    private List<String> getScience() {
        list = new ArrayList<>();
        for (TeacherData teacherData : AllData.teacherDataList) {
            if (teacherData.getSubject().equals("Science")) {
                list.add(teacherData.getFullName());
            }
        }
        return list;
    }

    private List<String> getEnglish() {
        list = new ArrayList<>();
        for (TeacherData teacherData : AllData.teacherDataList) {
            if (teacherData.getSubject().equals("English")) {
                list.add(teacherData.getFullName());
            }
        }
        return list;
    }

    private List<String> getKiswahili() {
        list = new ArrayList<>();
        for (TeacherData teacherData : AllData.teacherDataList) {
            if (teacherData.getSubject().equals("Kiswahili")) {
                list.add(teacherData.getFullName());
            }
        }
        return list;
    }

    private List<String> getSst_cre() {
        list = new ArrayList<>();
        for (TeacherData teacherData : AllData.teacherDataList) {
            if (teacherData.getSubject().equals("sst_cre")) {
                list.add(teacherData.getFullName());
            }
        }
        return list;
    }

    private void setSpinnerValues() {
        String[] subjects = {"Math", "Science", "English", "Kiswahili", "sst_cre"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, layout.simple_dropdown_item_1line, subjects);
        spinner.setAdapter(arrayAdapter);
    }

    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        spinner = findViewById(R.id.spinner);
        listView = findViewById(R.id.listView);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.dashboardMenu:
                Intent intent = new Intent(this, MainActivity.class);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case id.home:
                super.onBackPressed();
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}
