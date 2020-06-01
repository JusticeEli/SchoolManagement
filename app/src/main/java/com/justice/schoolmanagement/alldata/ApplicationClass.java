package com.justice.schoolmanagement.alldata;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.dashboard.DashBoardActivity;
import com.justice.schoolmanagement.main.MainActivity;
import com.justice.schoolmanagement.parent.ParentData;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
import com.justice.schoolmanagement.student.StudentData;
import com.justice.schoolmanagement.student.StudentMarks;
import com.justice.schoolmanagement.student.StudentsActivity;
import com.justice.schoolmanagement.teacher.TeacherData;
import com.justice.schoolmanagement.teacher.TeachersActivity;

import java.util.ArrayList;
import java.util.List;

public class ApplicationClass extends Application {

    private static final String APPLICATION_ID = "AEBEC993-390A-CC14-FF29-1A31BA9A7000";
    private static final String API_KEY = "88CE15DA-73EF-4399-82FD-AD69D4C3412C";
    private static final String SERVER_URL = "https://api.backendless.com";

    public static DocumentSnapshot documentSnapshot;

    public static TeacherData teacherData;
    public static List<String> teacherNames=new ArrayList<>();


    public static void onNavigationItemSelected(Context context, int itemId) {
        switch (itemId) {

            case R.id.dashboardMenu:
                Intent intent = new Intent(context, DashBoardActivity.class);
                context.startActivity(intent);
                break;
            case R.id.teacherMenu:
                Intent intent2 = new Intent(context, TeachersActivity.class);
                context.startActivity(intent2);
                break;
            case R.id.studentsMenu:
                Intent intent3 = new Intent(context, StudentsActivity.class);
                context.startActivity(intent3);
                break;
            case R.id.parentsMenu:
                Intent intent4 = new Intent(context, ParentsActivity.class);
                context.startActivity(intent4);
                break;
            case R.id.subjectsMenu:
                Intent intent5 = new Intent(context, SubjectsActivity.class);
                context.startActivity(intent5);
                break;
            case R.id.resultsMenu:
                Intent intent6 = new Intent(context, ResultsActivity.class);
                context.startActivity(intent6);
                break;
            case R.id.classesMenu:
                Intent intent7 = new Intent(context, ClassesActivity.class);
                context.startActivity(intent7);
                break;
            case R.id.logoutMenu:
                FirebaseAuth.getInstance().signOut();
                Intent intent8 = new Intent(context, MainActivity.class);
                context.startActivity(intent8);
                Activity activity=(Activity)context;
                activity.finish();
                break;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        loadTeacherNames();

        Backendless.setUrl(SERVER_URL);
        Backendless.initApp(getApplicationContext(), APPLICATION_ID, API_KEY);
        //  loadDataFromDatabase();

    }

    private void loadTeacherNames() {

        FirebaseFirestore.getInstance().collection("Teachers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Toast.makeText(ApplicationClass.this, "Loading Teachers name: ", Toast.LENGTH_SHORT).show();

                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        teacherNames.add(documentSnapshot.toObject(TeacherData.class).getFullName());
                    }

                }else {
                    Toast.makeText(ApplicationClass.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadDataFromDatabase() {
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setGroupBy("fullName");

        Backendless.Persistence.of(TeacherData.class).find(dataQueryBuilder, new AsyncCallback<List<TeacherData>>() {
            @Override
            public void handleResponse(List<TeacherData> response) {
                AllData.teacherDataList = response;
                Toast.makeText(ApplicationClass.this, "loaded Teacher data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ApplicationClass.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        Backendless.Persistence.of(ParentData.class).find(dataQueryBuilder, new AsyncCallback<List<ParentData>>() {
            @Override
            public void handleResponse(List<ParentData> response) {
                AllData.parentDataList = response;
                Toast.makeText(ApplicationClass.this, "loaded Parent data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ApplicationClass.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        Backendless.Persistence.of(StudentData.class).find(dataQueryBuilder, new AsyncCallback<List<StudentData>>() {
            @Override
            public void handleResponse(List<StudentData> response) {
                AllData.studentDataList = response;
                Toast.makeText(ApplicationClass.this, "loaded StudentData data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ApplicationClass.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        Backendless.Persistence.of(StudentMarks.class).find(dataQueryBuilder, new AsyncCallback<List<StudentMarks>>() {
            @Override
            public void handleResponse(List<StudentMarks> response) {
                AllData.studentMarksList = response;
                Toast.makeText(ApplicationClass.this, "loaded StudentMarks data list", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ApplicationClass.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}
