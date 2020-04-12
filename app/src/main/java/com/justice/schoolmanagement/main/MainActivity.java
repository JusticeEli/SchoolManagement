package com.justice.schoolmanagement.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.alldata.AllData;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;

public class MainActivity extends AppCompatActivity {
    private EditText emailEdtTxt, passwordEdtTxt;
    private TextView resetPasswordTxtView;
    private Button loginBtn;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initwidgets();
        isValidLogin();
        setOnClickListeners();
    }

    private void isValidLogin() {
        showProgress(true);
        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean response) {
                showProgress(false);
                if (response) {
                    String objectId = UserIdStorageFactory.instance().getStorage().get();
                    Backendless.Data.of(BackendlessUser.class).findById(objectId, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            showProgress(false);
                            AllData.user = response;
                            startActivity(new Intent(MainActivity.this, DashBoardActivity.class));
                            finish();

                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            showProgress(false);
                            Toast.makeText(MainActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(MainActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setOnClickListeners() {

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress(true);
                Backendless.UserService.login(emailEdtTxt.getText().toString().trim(), passwordEdtTxt.getText().toString().trim(), new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        showProgress(false);
                        Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                        AllData.user = response;
                        startActivity(new Intent(MainActivity.this, DashBoardActivity.class));
                        finish();

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        showProgress(false);
                        Toast.makeText(MainActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }, true);
            }


        });


        resetPasswordTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEdtTxt.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Fill Email !!", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress(true);
                Backendless.UserService.restorePassword(emailEdtTxt.getText().toString().trim(), new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        showProgress(false);
                        Toast.makeText(MainActivity.this, "Reset Link Sent Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        showProgress(false);
                        Toast.makeText(MainActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean fieldsAreEmpty() {
        if (emailEdtTxt.getText().toString().trim().isEmpty() || passwordEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }
    /////////////////////PROGRESS_BAR////////////////////////////
    private void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);


        }

    }

    private void initwidgets() {
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        passwordEdtTxt = findViewById(R.id.passwordEdtTxt);
        resetPasswordTxtView = findViewById(R.id.resetPasswordEdtTxt);
        loginBtn = findViewById(R.id.loginBtn);

        //////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        linearLayout = findViewById(R.id.linearLayout);


    }
}
