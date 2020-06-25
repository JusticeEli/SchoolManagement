package com.justice.schoolmanagement.parent;

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
import com.google.firebase.storage.FirebaseStorage;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.ApplicationClass;

public class ParentDetailsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ParentData parentData;
    private String email;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private CardView cardView;

    private ImageView callImageView, emailImageView;


    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private KenBurnsView imageView;


    private TextView parentNameTxtView, contactTxtView, firstNameTxtView, lastNameTxtView, cityTxtView, jobStatusTxtView, ageTxtView, genderTxtView, jobTypeTxtView, deleteTxtView, editTxtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_details);
        email = getIntent().getStringExtra("email");

        parentData = ApplicationClass.documentSnapshot.toObject(ParentData.class);
        parentData.setId(ApplicationClass.documentSnapshot.getId());
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
                intent.setData(Uri.parse("tel:" + parentData.getContact()));
                startActivity(intent);
            }
        });

        emailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                String email[] = {parentData.getEmail()};
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
                startActivity(intent);
            }
        });
    }

    private void deleteFromDatabase() {

        new MaterialAlertDialogBuilder(ParentDetailsActivity.this).setBackground(getDrawable(R.drawable.button_first)).setIcon(R.drawable.ic_delete).setTitle("delete").setMessage("Are you sure you want to delete ").setNegativeButton("no", null).setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteParent();
            }
        }).show();

    }

    private void deleteParent() {
        /**
         *  ACTIVITY EXITING BEFORE DELETION OF PHOTO IS COMPLETE MAY CAUSE CRASH OF THE PROGRAM//////////
         *
         */

        showProgress(true);
        FirebaseStorage.getInstance().getReferenceFromUrl(parentData.getPhoto()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ParentDetailsActivity.this, "Photo Deleted", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(ParentDetailsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

        ApplicationClass.documentSnapshot.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ParentDetailsActivity.this, parentData.getFirstName() + " Removed Successfully", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(ParentDetailsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
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

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(parentData.getPhoto()).thumbnail(Glide.with(this).load(parentData.getThumbnail())).into(imageView);


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

        callImageView = findViewById(R.id.callImageView);
        emailImageView = findViewById(R.id.emailImageView);

        imageView = findViewById(R.id.imageView);


    }

    @Override
    protected void onResume() {
        super.onResume();
        parentData = ApplicationClass.documentSnapshot.toObject(ParentData.class);
        parentData.setId(ApplicationClass.documentSnapshot.getId());

        setDefaultValues();
    }
}
