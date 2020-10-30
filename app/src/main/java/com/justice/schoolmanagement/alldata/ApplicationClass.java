package com.justice.schoolmanagement.alldata;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.justice.schoolmanagement.ClassesActivity;
import com.justice.schoolmanagement.R;
import com.justice.schoolmanagement.SubjectsActivity;
import com.justice.schoolmanagement.parent.ParentsActivity;
import com.justice.schoolmanagement.results.ResultsActivity;
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
    public static List<String> teacherNames = new ArrayList<>();


    public static void onNavigationItemSelected(final Context context, int itemId) {
        switch (itemId) {

            case R.id.dashboardMenu:
//                Intent intent = new Intent(context, MainActivity.class);
//                context.startActivity(intent);
//                break;
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


                AuthUI.getInstance()
                        .signOut(context)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
//                                Intent intent8 = new Intent(context, MainActivity.class);
//                                context.startActivity(intent8);
//                                Activity activity = (Activity) context;
//                                activity.finish();
                            }
                        });

                break;


        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        loadTeacherNames();

    }

    private void loadTeacherNames() {

        FirebaseFirestore.getInstance().collection("Teachers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Toast.makeText(ApplicationClass.this, "Loading Teachers name: ", Toast.LENGTH_SHORT).show();

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        teacherNames.add(documentSnapshot.toObject(TeacherData.class).getFullName());
                        AllData.teacherDataList.add(documentSnapshot.toObject(TeacherData.class));
                    }

                } else {
                    Toast.makeText(ApplicationClass.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
