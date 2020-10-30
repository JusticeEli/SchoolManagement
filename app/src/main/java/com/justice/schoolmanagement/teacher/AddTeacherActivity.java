package com.justice.schoolmanagement.teacher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.dashboard.MainActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class AddTeacherActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt;
    private EditText lastNameEdtTxt;
    private EditText contactEdtTxt;
    private EditText salaryEdtTxt;
    private EditText cityEdtTxt;
    private EditText degreeEdtTxt;
    private EditText ageEdtTxt;
    private Spinner subjectSpinner;
    private RadioGroup genderRadioGroup;

    private Button addPhotoBtn;
    private Button addBtn;
    private CircleImageView imageView;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;


    private TeacherData teacherData;
    private String teacherId;


    private CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Teachers");
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);
        initWidgets();
        setDefaultValues();
        //   initNavigationDrawer();

        setOnClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setDefaultValues() {
        contactEdtTxt.setText("07");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logoutMenu) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            Intent intent8 = new Intent(AddTeacherActivity.this, MainActivity.class);
                            startActivity(intent8);
                            finish();
                        }
                    });
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

        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDataFromEdtTxtAndAddItToDatabase();

            }


        });

        contactEdtTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (contactEdtTxt.length() == 10) {
                    salaryEdtTxt.requestFocus();
                }
                if (contactEdtTxt.length() > 10) {
                    contactEdtTxt.setError("Contact Must have 10 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.centerCrop();
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(imageView);

    }

    private void resetEdtTxt() {
        firstNameEdtTxt.setText("");
        lastNameEdtTxt.setText("");
        salaryEdtTxt.setText("");
        cityEdtTxt.setText("");
        degreeEdtTxt.setText("");
        ageEdtTxt.setText("");
        contactEdtTxt.setText("");
    }

    private boolean fieldsAreEmpty() {

        if (uri == null || firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || contactEdtTxt.getText().toString().trim().isEmpty() || salaryEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty() || degreeEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }

        return false;
    }

    private void getDataFromEdtTxtAndAddItToDatabase() {
        if (uri == null) {
            Toasty.info(AddTeacherActivity.this, "Please choose a photo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fieldsAreEmpty()) {
            Toasty.error(AddTeacherActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!contactEdtTxtFormatIsCorrect()) {
            return;
        }

        teacherData = new TeacherData(firstNameEdtTxt.getText().toString().trim() + " " + lastNameEdtTxt.getText().toString().trim(), firstNameEdtTxt.getText().toString().trim(), lastNameEdtTxt.getText().toString().trim(), FirebaseAuth.getInstance().getCurrentUser().getEmail(), salaryEdtTxt.getText().toString().trim(), cityEdtTxt.getText().toString().trim(), degreeEdtTxt.getText().toString().trim(), ageEdtTxt.getText().toString().trim(), getSelectedGenderRadioBtn(), "teacher", "photo", subjectSpinner.getSelectedItem().toString(), contactEdtTxt.getText().toString().trim());
        registerTeacherAndPutDataInDatabase();

    }

    private boolean contactEdtTxtFormatIsCorrect() {
        String contact = contactEdtTxt.getText().toString().trim();
        if (!contact.startsWith("07")) {
            Toasty.error(this, "Contact Must start with 07 !!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (contact.length() != 10) {
            Toasty.info(this, "Contact Must have 10 characters", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    private void registerTeacherAndPutDataInDatabase() {

        teacherId = FirebaseAuth.getInstance().getUid();
        putTeacherPhotoInDatabase();


    }

    private void putTeacherPhotoInDatabase() {


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        showProgress(true);
        final StorageReference ref = FirebaseStorage.getInstance().getReference("teachers_images").child(teacherId + ".jpg");

        UploadTask uploadTask = ref.putFile(uri);


        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    teacherData.setPhoto(downloadUri.toString());
                    uploadThumbnail();
                    Toasty.success(AddTeacherActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(AddTeacherActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

        /////////////////////////////////////////////

    }

    private void uploadThumbnail() {
        Uri thumbnail = null;
        File compressedImgFile = null;

        try {
            compressedImgFile = new Compressor(this).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(new File(uri.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        thumbnail = Uri.fromFile(compressedImgFile);
        showProgress(true);
        final StorageReference ref1 = FirebaseStorage.getInstance().getReference("teachers_thumbnail_images").child(teacherId);

        UploadTask uploadTask1 = ref1.putFile(thumbnail);


        uploadTask1.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ref1.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    teacherData.setThumbnail(downloadUri.toString());
                    putTeacherDataInDatabase();
                    Toasty.info(AddTeacherActivity.this, "Thumbnail Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(AddTeacherActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });
    }


    private void putTeacherDataInDatabase() {
        showProgress(true);
        collectionReference.document(FirebaseAuth.getInstance().getUid()).set(teacherData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.info(AddTeacherActivity.this, "Teacher Data Saved", Toast.LENGTH_SHORT).show();
                    resetEdtTxt();
                    finish();


                } else {
                    String error = task.getException().getMessage();
                    Toasty.error(AddTeacherActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

    }


    private String getSelectedGenderRadioBtn() {
        switch (genderRadioGroup.getCheckedRadioButtonId()) {
            case R.id.maleRadioBtn:
                return "Male";

            case R.id.femaleRadioBtn:

                return "Female";

            case R.id.otherRadioBtn:
                return "Other";
        }
        return null;
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);


        }

    }


    private void initWidgets() {
        firstNameEdtTxt = findViewById(R.id.firstNameEdtTxt);
        lastNameEdtTxt = findViewById(R.id.lastNameEdtTxt);
        salaryEdtTxt = findViewById(R.id.salaryEdtTxt);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);
        degreeEdtTxt = findViewById(R.id.degreeEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        contactEdtTxt = findViewById(R.id.contactEdtTxt);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addBtn = findViewById(R.id.addBtn);

        imageView = findViewById(R.id.imageView);


////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);

        String[] subjects = {"Math", "Science", "English", "Kiswahili", "sst_cre"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        subjectSpinner.setAdapter(arrayAdapter);


    }
}
