package com.justice.schoolmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.class_.ChoosenClassActivity;

public class ClassesActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private Button class_1_btn;
    private Button class_2_btn;
    private Button class_3_btn;
    private Button class_4_btn;
    private Button class_5_btn;
    private Button class_6_btn;
    private Button class_7_btn;
    private Button class_8_btn;



    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        initWidgets();   initNavigationDrawer();

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
        class_1_btn.setOnClickListener(this);
        class_2_btn.setOnClickListener(this);
        class_3_btn.setOnClickListener(this);
        class_4_btn.setOnClickListener(this);
        class_5_btn.setOnClickListener(this);
        class_6_btn.setOnClickListener(this);
        class_7_btn.setOnClickListener(this);
        class_8_btn.setOnClickListener(this);

    }

    private void initWidgets() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        class_1_btn = findViewById(R.id.class_1_btn);
        class_2_btn = findViewById(R.id.class_2_btn);
        class_3_btn = findViewById(R.id.class_3_btn);
        class_4_btn = findViewById(R.id.class_4_btn);
        class_5_btn = findViewById(R.id.class_5_btn);
        class_6_btn = findViewById(R.id.class_6_btn);
        class_7_btn = findViewById(R.id.class_7_btn);
        class_8_btn = findViewById(R.id.class_8_btn);

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.class_1_btn:
                Intent intent1 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent1.putExtra("class", 1);
                startActivity(intent1);
                break;
            case R.id.class_2_btn:
                Intent intent2 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent2.putExtra("class", 2);
                startActivity(intent2);
                break;
            case R.id.class_3_btn:
                Intent intent3 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent3.putExtra("class", 3);
                startActivity(intent3);
                break;
            case R.id.class_4_btn:
                Intent intent4 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent4.putExtra("class", 4);
                startActivity(intent4);
                break;
            case R.id.class_5_btn:
                Intent intent5 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent5.putExtra("class", 5);
                startActivity(intent5);
                break;
            case R.id.class_6_btn:
                Intent intent6 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent6.putExtra("class", 6);
                startActivity(intent6);
                break;
            case R.id.class_7_btn:
                Intent intent7 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent7.putExtra("class", 7);
                startActivity(intent7);
                break;
            case R.id.class_8_btn:
                Intent intent8 = new Intent(ClassesActivity.this, ChoosenClassActivity.class);
                intent8.putExtra("class", 8);
                startActivity(intent8);
                break;

        }

    }
}
