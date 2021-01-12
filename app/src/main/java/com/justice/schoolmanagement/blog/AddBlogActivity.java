package com.justice.schoolmanagement.blog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.presentation.ui.blog.model.Blog;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Date;
import java.util.UUID;

public class AddBlogActivity extends AppCompatActivity {
    private ImageView postImageView;
    private EditText descriptionEdtTxt;
    private Button submitBtn;
    private Blog blog;
    private Uri uri = null;
    private String downLoadLink;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_blog);
        initWidgets();
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(AddBlogActivity.this);

            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putPhotoInDatabase();
            }
        });
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

    private void putBlogIntoDatabase() {


        blog = new Blog(FirebaseAuth.getInstance().getUid(), descriptionEdtTxt.getText().toString().trim(),downLoadLink,downLoadLink, new Date());
       showProgress(true);
        FirebaseFirestore.getInstance().collection("blogs").add(blog).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddBlogActivity.this, "Blog Added", Toast.LENGTH_SHORT).show();
                    finish();

                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(AddBlogActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

    }

    private void putPhotoInDatabase() {
        if (descriptionEdtTxt.getText().toString().trim().isEmpty() || uri == null) {
            Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = UUID.randomUUID().toString();
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        showProgress(true);
        final StorageReference ref = FirebaseStorage.getInstance().getReference("blogs_images").child(name);

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
                    downLoadLink = downloadUri.toString();
                    putBlogIntoDatabase();
                    Toast.makeText(AddBlogActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(AddBlogActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                showProgress(false);
            }
        });

        /////////////////////////////////////////////
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
        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(uri).into(postImageView);

    }

    private void initWidgets() {
        postImageView = findViewById(R.id.postImageView);
        descriptionEdtTxt = findViewById(R.id.descriptionEdtTxt);
        submitBtn = findViewById(R.id.submitBtn);

        ///////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        scrollView = findViewById(R.id.scrollView);

    }
}
