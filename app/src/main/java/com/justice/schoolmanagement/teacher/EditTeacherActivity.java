package com.justice.schoolmanagement.teacher;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.AddStudentActivity;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditTeacherActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt;
    private EditText lastNameEdtTxt;
    private EditText emailEdtTxt;
    private EditText salaryEdtTxt;
    private EditText cityEdtTxt;
    private EditText degreeEdtTxt;
    private EditText ageEdtTxt;
    private RadioGroup genderRadioGroup;
    private RadioGroup typeRadioGroup;

    private RadioButton maleRadioBtn;
    private RadioButton femaleRadioBtn;
    private RadioButton otherRadioBtn;

    private RadioButton teacherRadioBtn;
    private RadioButton adminRadioBtn;

    private Spinner subjectSpinner;
    private EditText contactEdtTxt;
    private Button addPhotoBtn;
    private Button submitBtn;
    private String email;
    private TeacherData teacherData;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;

    private CircleImageView imageView;
    private Uri uri = null;
    private boolean photoChanged = false;
    private String photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);
        initWidgets();
        teacherData = ApplicationClass.documentSnapshot.toObject(TeacherData.class);
        teacherData.setId(ApplicationClass.documentSnapshot.getId());
        initNavigationDrawer();


        setDefaultValues();
        setOnClickListeners();
    }

    private void choosePhoto() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                photoChanged = true;
                uri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(imageView);
    }

    private boolean contactEdtTxtFormatIsCorrect() {
        String contact = contactEdtTxt.getText().toString().trim();
        if (!contact.startsWith("07")) {
            Toast.makeText(this, "Contact Must start with 07 !!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (contact.length() != 10) {
            Toast.makeText(this, "Contact Must have 10 characters", Toast.LENGTH_SHORT).show();
            return false;
        }


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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firstNameEdtTxt = findViewById(R.id.firstNameEdtTxt);
        lastNameEdtTxt = findViewById(R.id.lastNameEdtTxt);
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        salaryEdtTxt = findViewById(R.id.salaryEdtTxt);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);
        degreeEdtTxt = findViewById(R.id.degreeEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);

        maleRadioBtn = findViewById(R.id.maleRadioBtn);
        femaleRadioBtn = findViewById(R.id.femaleRadioBtn);
        otherRadioBtn = findViewById(R.id.otherRadioBtn);

        teacherRadioBtn = findViewById(R.id.teacherRadioBtn);
        adminRadioBtn = findViewById(R.id.adminRadioBtn);

        subjectSpinner = findViewById(R.id.subjectSpinner);
        contactEdtTxt = findViewById(R.id.contactEdtTxt);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        submitBtn = findViewById(R.id.submitBtn);

        ///////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);

        imageView = findViewById(R.id.imageView);


        String[] subjects = {"Math", "Science", "English", "Kiswahili", "sst_cre"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        subjectSpinner.setAdapter(arrayAdapter);

        contactEdtTxt.setText("07");
    }

    private void setDefaultValues() {
        firstNameEdtTxt.setText(teacherData.getFirstName());
        lastNameEdtTxt.setText(teacherData.getLastName());
        emailEdtTxt.setText(teacherData.getEmail());
        salaryEdtTxt.setText(teacherData.getSalary());
        cityEdtTxt.setText(teacherData.getCity());
        degreeEdtTxt.setText(teacherData.getDegree());
        ageEdtTxt.setText(teacherData.getAge());
        setDefaultValueForGenderRadioBtn();
        setDefaultValueForTypeRadioBtn();

        setDefaultValueForSubjectSpinner();
        contactEdtTxt.setText(teacherData.getContact());

        uri=Uri.parse(teacherData.getPhoto());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(teacherData.getPhoto()).thumbnail(Glide.with(this).load(teacherData.getThumbnail())).into(imageView);

    }

    private void setDefaultValueForTypeRadioBtn() {
        switch (teacherData.getType()) {
            case "teacher":
                teacherRadioBtn.setChecked(true);
                break;
            case "admin":
                adminRadioBtn.setChecked(true);
                break;


        }

    }

    private void setDefaultValueForGenderRadioBtn() {
        switch (teacherData.getGender()) {
            case "Male":
                maleRadioBtn.setChecked(true);
                break;
            case "Female":
                femaleRadioBtn.setChecked(true);
                break;
            case "Other":
                otherRadioBtn.setChecked(true);
                break;

            default:
                maleRadioBtn.setChecked(true);


        }
    }

    private void setDefaultValueForSubjectSpinner() {
        switch (teacherData.getSubject()) {

            case "Math":
                subjectSpinner.setSelection(0);
                break;
            case "Science":
                subjectSpinner.setSelection(1);
                break;
            case "English":
                subjectSpinner.setSelection(2);
                break;
            case "Kiswahili":
                subjectSpinner.setSelection(3);
                break;
            case "sst_cre":
                subjectSpinner.setSelection(4);
                break;

        }
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
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri == null ){
                    Toast.makeText(EditTeacherActivity.this, "Please choose a photo", Toast.LENGTH_SHORT).show();
                    return ;
                }
                if (fieldsAreEmpty()) {
                    Toast.makeText(EditTeacherActivity.this, "Please fill All fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!contactEdtTxtFormatIsCorrect()) {
                    return;
                }
                getDataFromEdtTxtAndAddToDataBase();

            }

            private void getDataFromEdtTxtAndAddToDataBase() {
                teacherData.setFullName(firstNameEdtTxt.getText().toString() + " " + lastNameEdtTxt.getText().toString());
                teacherData.setFirstName(firstNameEdtTxt.getText().toString());
                teacherData.setLastName(lastNameEdtTxt.getText().toString());
                teacherData.setEmail(emailEdtTxt.getText().toString());
                teacherData.setSalary(salaryEdtTxt.getText().toString());
                teacherData.setCity(cityEdtTxt.getText().toString());
                teacherData.setDegree(degreeEdtTxt.getText().toString());
                teacherData.setAge(ageEdtTxt.getText().toString());

                teacherData.setGender(getSelectedGenderRadioBtn());
                teacherData.setType(getSelectedTypeRadioBtn());

                teacherData.setSubject(subjectSpinner.getSelectedItem().toString());
                teacherData.setContact(contactEdtTxt.getText().toString());

                if (photoChanged) {
                    teacherData.setPhoto(photo);
                }

                if (photoChanged) {
                    showProgress(true);

                    final StorageReference ref = FirebaseStorage.getInstance().getReference("teachers_images").child(teacherData.getId() + ".jpg");
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
                                photo = downloadUri.toString();
                                teacherData.setPhoto(photo);
                                uploadThumbnail();
                                Toast.makeText(EditTeacherActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(EditTeacherActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                            showProgress(false);
                        }
                    });
                } else {
                    putDataInDatabase();
                }
            }
        });

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
        final StorageReference ref1 = FirebaseStorage.getInstance().getReference("teachers_thumbnail_images").child(teacherData.getId());

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
                    putDataInDatabase();
                    Toast.makeText(EditTeacherActivity.this, "Thumbnail Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(EditTeacherActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });
    }

    @Override
    public void onBackPressed() {

        ApplicationClass.teacherData = teacherData;
        super.onBackPressed();
    }

    private void putDataInDatabase() {
        showProgress(true);
        ApplicationClass.documentSnapshot.getReference().set(teacherData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EditTeacherActivity.this, "Teacher Data updated successfully", Toast.LENGTH_SHORT).show();
                    ApplicationClass.documentSnapshot.getReference().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ApplicationClass.documentSnapshot=documentSnapshot;
                            onBackPressed();
                        }
                    });

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(EditTeacherActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });
    }

    private String getSelectedTypeRadioBtn() {
        switch (typeRadioGroup.getCheckedRadioButtonId()) {
            case R.id.teacherRadioBtn:
                return "teacher";

            case R.id.adminRadioBtn:
                return "admin";


        }
        return null;

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

    private boolean fieldsAreEmpty() {

        if (uri == null || firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || contactEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || salaryEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty() || degreeEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }

        return false;
    }


}
