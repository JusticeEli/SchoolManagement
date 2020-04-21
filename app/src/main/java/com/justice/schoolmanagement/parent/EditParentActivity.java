package com.justice.schoolmanagement.parent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.EditTeacherActivity;
import com.justice.schoolmanagement.teacher.TeachersActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static com.justice.schoolmanagement.alldata.ApplicationClass.documentSnapshot;

public class EditParentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private EditText firstNameEdtTxt;
    private EditText lastNameEdtTxt;
    private EditText emailEdtTxt;
    private AutoCompleteTextView cityEdtTxt;
    private EditText contactEdtTxt;
    private Spinner jobStatusSpinner;
    private EditText ageEdtTxt;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioBtn;
    private RadioButton femaleRadioBtn;
    private RadioButton otherRadioBtn;

    private AutoCompleteTextView jobTypeEdtTxt;

    private Button addPhotoBtn;
    private Button submitBtn;

    private ParentData parentData;
    private String email;

    //////////////////DRAWER LAYOUT////////////////////////

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;

    private CircleImageView imageView;
    private Uri uri;
    private boolean photoChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_parent);
        email = getIntent().getStringExtra("email");
        parentData = documentSnapshot.toObject(ParentData.class);
        initWidgets();
        initAdapters();
        initNavigationDrawer();


        setDefaulValues();
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
                photoChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(imageView);

    }

    private void initAdapters() {
        String[] jobStatus = {"Employed", "Unemployed", "Retired"};
        ArrayAdapter<String> jobStatusAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, jobStatus);
        jobStatusSpinner.setAdapter(jobStatusAdapter);

        String[] cities = {"Kisumu", "Kitui", "Lamu", "Machakos", "Marsabit", "Meru", "Migori", "Mombasa", "Nakuru", "Narok", "Trans Nzoia", "Turkana", "Vihiga", "Naivasha", "Eldoret", "Kericho"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cities);
        cityEdtTxt.setAdapter(cityAdapter);


        String[] jobType = {"medical", "health", "administrative", "secretarial", "sales", "marketing", "finance", "auditing", "accounting", "education", "ngo", "ict", "building", "construction", "procument", "engineering", "media", "computer", "human resource", "law", "research", "manufacturing", "hospitality"};
        ArrayAdapter<String> jobTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, jobType);
        jobTypeEdtTxt.setAdapter(jobTypeAdapter);

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

    private void setDefaulValues() {
        firstNameEdtTxt.setText(parentData.getFirstName());
        lastNameEdtTxt.setText(parentData.getLastName());
        emailEdtTxt.setText(parentData.getEmail());
        setJobStatusDefaultValue();
        cityEdtTxt.setText(parentData.getCity());
        jobTypeEdtTxt.setText(parentData.getJobType());
        ageEdtTxt.setText(parentData.getAge());
        setDefaultValueForRadioBtn();
        contactEdtTxt.setText(parentData.getContact());

        uri = Uri.parse(parentData.getPhoto());
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.mipmap.place_holder);
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(parentData.getPhoto()).thumbnail(Glide.with(this).load(parentData.getThumbnail())).into(imageView);

    }

    private void setJobStatusDefaultValue() {
        switch (parentData.getJobStatus()) {
            case "Employed":
                jobStatusSpinner.setSelection(0);
                break;
            case "Unemployed":
                jobStatusSpinner.setSelection(1);
                break;
            case "Retired":
                jobStatusSpinner.setSelection(2);
                break;

        }
    }

    private void setDefaultValueForRadioBtn() {
        switch (parentData.getGender()) {
            case "Male":
                maleRadioBtn.setChecked(true);
                break;
            case "Female":
                femaleRadioBtn.setChecked(true);
                break;
            case "Other":
                otherRadioBtn.setChecked(true);
                break;

        }
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
        jobStatusSpinner = findViewById(R.id.jobStatusSpinner);
        cityEdtTxt = findViewById(R.id.cityEdtTxt);
        jobTypeEdtTxt = findViewById(R.id.jobTypeEdtTxt);
        ageEdtTxt = findViewById(R.id.ageEdtTxt);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        maleRadioBtn = findViewById(R.id.maleRadioBtn);
        femaleRadioBtn = findViewById(R.id.femaleRadioBtn);
        otherRadioBtn = findViewById(R.id.otherRadioBtn);

        contactEdtTxt = findViewById(R.id.contactEdtTxt);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        submitBtn = findViewById(R.id.submitBtn);


////////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);

        imageView = findViewById(R.id.imageView);

        contactEdtTxt.setText("07");
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

    private void setOnClickListeners() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(EditParentActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!contactEdtTxtFormatIsCorrect()) {
                    return;
                }
                getDataFromEdtTxtAndSaveIntoDatabase();


            }
        });

        contactEdtTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (contactEdtTxt.length() == 10) {
                    ageEdtTxt.requestFocus();
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

    private boolean fieldsAreEmpty() {

        if (firstNameEdtTxt.getText().toString().trim().isEmpty() || lastNameEdtTxt.getText().toString().trim().isEmpty() || emailEdtTxt.getText().toString().trim().isEmpty() || cityEdtTxt.getText().toString().trim().isEmpty() || contactEdtTxt.getText().toString().trim().isEmpty() || ageEdtTxt.getText().toString().trim().isEmpty() || jobTypeEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }

        return false;
    }


    private void getDataFromEdtTxtAndSaveIntoDatabase() {
        // TODO: 11-Feb-20  extract a photo from the addPhoto button
        parentData.setFullName(firstNameEdtTxt.getText().toString().trim() + " " + lastNameEdtTxt.getText().toString().trim());
        parentData.setContact(contactEdtTxt.getText().toString().trim());
        parentData.setFirstName(firstNameEdtTxt.getText().toString().trim());
        parentData.setLastName(lastNameEdtTxt.getText().toString().trim());
        parentData.setCity(cityEdtTxt.getText().toString().trim());
        parentData.setJobStatus(jobStatusSpinner.getSelectedItem().toString().trim());
        parentData.setAge(ageEdtTxt.getText().toString());
        parentData.setGender(getSelectedRadioBtn());
        parentData.setJobType(jobTypeEdtTxt.getText().toString().trim());
        parentData.setEmail(emailEdtTxt.getText().toString().trim());


        if (photoChanged) {
            showProgress(true);
            final StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(parentData.getPhoto());

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
                        parentData.setPhoto(downloadUri.toString());
                        uploadThumbnail();
                        Toast.makeText(EditParentActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(EditParentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                    showProgress(false);
                }
            });

            /////////////////////////////////////////////


        } else {
            putDataInDatabase();
        }


    }
    private void uploadThumbnail() {
        Uri thumbnail ;
        File compressedImgFile = null;

        try {
            compressedImgFile = new Compressor(this).setCompressFormat(Bitmap.CompressFormat.JPEG).setMaxHeight(10).setMaxWidth(10).setQuality(40).compressToFile(new File(uri.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        thumbnail = Uri.fromFile(compressedImgFile);

        showProgress(true);
        String photoName = UUID.randomUUID().toString();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("parents_thumbnail_images").child(photoName);

        UploadTask uploadTask = ref.putFile(thumbnail);


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
                    parentData.setThumbnail(downloadUri.toString());
                    putDataInDatabase();
                    Toast.makeText(EditParentActivity.this, "Thumbnail Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(EditParentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

    }


    private void putDataInDatabase() {
        showProgress(true);
        documentSnapshot.getReference().set(parentData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    documentSnapshot.getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                ApplicationClass.documentSnapshot = task.getResult();
                                Toast.makeText(EditParentActivity.this, parentData.getFirstName() + " Edited Successfully", Toast.LENGTH_SHORT).show();
                                finish();

                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(EditParentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(EditParentActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                }
                showProgress(false);
            }
        });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
