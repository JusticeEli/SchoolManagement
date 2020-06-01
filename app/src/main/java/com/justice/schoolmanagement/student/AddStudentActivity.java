package com.justice.schoolmanagement.student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.alldata.ApplicationClass;
import com.justice.schoolmanagement.parent.AddParentActivity;
import com.justice.schoolmanagement.teacher.TeacherData;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AddStudentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt, lastNameEdtTxt, emailEdtTxt, parentNameEdtTxt, dateOfBirthEdtTxt, dateOfArrivalEdtTxt, ageEdtTxt;
    private AutoCompleteTextView cityEdtTxt;
    private Spinner classGradeSpinner, nationalitySpinner, religionSpinner, classTeacherNameSpinner;
    private RadioGroup genderRadioGroup;

    private Button addPhotoBtn, addBtn;
    private StudentData studentData;
    private StudentMarks studentMarks;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CollectionReference collectionReferenceMarks = FirebaseFirestore.getInstance().collection("StudentsMarks");
    private CollectionReference collectionReferenceData = FirebaseFirestore.getInstance().collection("Students");
    private DocumentSnapshot documentSnapshot;

    private CircleImageView imageView;
    private Uri uri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        initWidgets();
        initNavigationDrawer();

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
                uri = result.getUri();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(imageView);

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

    private String getSelectedRadioBtn() {
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

    private void setOnClickListeners() {


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri == null ){
                    Toast.makeText(AddStudentActivity.this, "Please choose a photo", Toast.LENGTH_SHORT).show();
                    return ;
                }
                if (fieldsAreEmpty()) {
                    Toast.makeText(AddStudentActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                getDataFromEdtTxtAndSaveInDatabase();

            }
        });

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
    }

    private boolean fieldsAreEmpty() {


        if ( firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || parentNameEdtTxt.getText().toString().trim().isEmpty() || dateOfBirthEdtTxt.getText().toString().trim().isEmpty() || dateOfArrivalEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty()) {


            return true;
        }
        return false;
    }

    private void resetEdtTxt() {
        firstNameEdtTxt.setText("");
        lastNameEdtTxt.setText("");
        emailEdtTxt.setText("");
        parentNameEdtTxt.setText("");
        dateOfBirthEdtTxt.setText("");
        dateOfArrivalEdtTxt.setText("");
        ageEdtTxt.setText("");
        cityEdtTxt.setText("");

    }

    private void getDataFromEdtTxtAndSaveInDatabase() {
        // TODO: 13-Apr-20  UN COMMNENT CLASS TEACHER NAME
        studentData = new StudentData();

        studentData.setFullName(firstNameEdtTxt.getText().toString() + " " + lastNameEdtTxt.getText().toString());
        studentData.setClassGrade(Integer.parseInt(classGradeSpinner.getSelectedItem().toString()));
        studentData.setFirstName(firstNameEdtTxt.getText().toString());
        studentData.setLastName(lastNameEdtTxt.getText().toString());
        studentData.setNationality(nationalitySpinner.getSelectedItem().toString());
        studentData.setReligion(religionSpinner.getSelectedItem().toString());
        studentData.setEmail(emailEdtTxt.getText().toString());
        studentData.setParentName(parentNameEdtTxt.getText().toString());
        studentData.setDateOfBirth(dateOfBirthEdtTxt.getText().toString());
        studentData.setDateOfArrival(dateOfArrivalEdtTxt.getText().toString());
        studentData.setAge(ageEdtTxt.getText().toString());
        studentData.setGender(getSelectedRadioBtn());
        studentData.setClassTeacherName(classTeacherNameSpinner.getSelectedItem().toString());
        studentData.setCity(cityEdtTxt.getText().toString());
        putImageToStorage();

    }

    private void putDataIntoDatabase() {
        showProgress(true);

        collectionReferenceData.add(studentData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {

                    task.getResult().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                documentSnapshot = task.getResult();
                                addStudentMarks();
                                resetEdtTxt();
                                Toast.makeText(AddStudentActivity.this, "Student Added ", Toast.LENGTH_SHORT).show();
                                addParent();

                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(AddStudentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddStudentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }
                showProgress(false);
            }
        });

    }

    private void putImageToStorage() {
        String photoName = UUID.randomUUID().toString();
        studentData.setPhotoName(photoName);
        showProgress(true);
        final StorageReference ref = FirebaseStorage.getInstance().getReference("students_images").child(photoName);

        UploadTask uploadTask = ref.putFile(uri);


        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                showProgress(false);
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    studentData.setPhoto(downloadUri.toString());
                    uploadThumbnail();
                    Toast.makeText(AddStudentActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddStudentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

        /////////////////////////////////////////////

    }

    private void uploadThumbnail() {
        String photoName = UUID.randomUUID().toString();
        studentData.setPhotoName(photoName);
        showProgress(true);
        Uri thumbnail;
        File compressedImgFile = null;

        try {
            compressedImgFile = new Compressor(this).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(new File(uri.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        thumbnail = Uri.fromFile(compressedImgFile);

        final StorageReference ref = FirebaseStorage.getInstance().getReference("students_thumbnail_images").child(photoName);

        UploadTask uploadTask = ref.putFile(thumbnail);


        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                showProgress(false);
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    studentData.setThumbnail(downloadUri.toString());

                    putDataIntoDatabase();
                    Toast.makeText(AddStudentActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddStudentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });
    }

    private void addParent() {
        Intent intent = new Intent(this, AddParentActivity.class);
        intent.putExtra("flag", true);
        intent.putExtra("parentName", studentData.getParentName());
        intent.putExtra("parentEmail", studentData.getEmail());

        startActivity(intent);

    }

    private void addStudentMarks() {
        studentMarks = new StudentMarks();
        studentMarks.setFullName(studentData.getFullName());
        studentMarks.setEmail(studentData.getEmail());
        studentMarks.setClassGrade(studentData.getClassGrade());

        showProgress(true);
        collectionReferenceMarks.document(documentSnapshot.getId()).set(studentMarks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddStudentActivity.this, "Student Marks Added", Toast.LENGTH_SHORT).show();
                    Toast.makeText(AddStudentActivity.this, "Finished", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddStudentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }
                showProgress(false);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        ApplicationClass.onNavigationItemSelected(this, menuItem.getItemId());

        DrawerLayout drawerLayout = findViewById(R.id.drawer);

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

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

        classGradeSpinner = findViewById(R.id.classGradeSpinner);
        nationalitySpinner = findViewById(R.id.nationalitySpinner);
        religionSpinner = findViewById(R.id.religionSpinner);
        classTeacherNameSpinner = findViewById(R.id.classTeacherNameSpinner);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);


        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        parentNameEdtTxt = findViewById(R.id.parentNameEdtTxt);
        dateOfBirthEdtTxt = findViewById(R.id.dateOfBirthEdtTxt);
        dateOfArrivalEdtTxt = findViewById(R.id.dateOfArrivalEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);

        /////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);


        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addBtn = findViewById(R.id.addBtn);
        imageView = findViewById(R.id.imageView);

        setValuesForSpinner();

    }

    private void setValuesForSpinner() {
        String[] classGrade = {"1", "2", "3", "4", "5", "6", "7", "8"};
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classGrade);
        classGradeSpinner.setAdapter(arrayAdapter1);

        String[] cities = {"Kisumu", "Kitui", "Lamu", "Nairobi", "Machakos", "Marsabit", "Meru", "Migori", "Mombasa", "Nakuru", "Narok", "Trans Nzoia", "Turkana", "Vihiga", "Naivasha", "Eldoret", "Kericho"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        cityEdtTxt.setAdapter(cityAdapter);


        String[] nationality = {"Kenyan", "Foreigner"};
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nationality);
        nationalitySpinner.setAdapter(arrayAdapter2);

        String[] religion = {"Christian", "Muslim"};
        ArrayAdapter<String> arrayAdapter3 = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, religion);
        religionSpinner.setAdapter(arrayAdapter3);

        setValuesForClassTeacherNameSpinner();


    }

    private void setValuesForClassTeacherNameSpinner() {

        ArrayAdapter<String> arrayAdapter4 = new ArrayAdapter<>(this,R.layout.spinner_item, ApplicationClass.teacherNames);
        classTeacherNameSpinner.setAdapter(arrayAdapter4);
     }
}
