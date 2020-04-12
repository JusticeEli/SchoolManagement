package com.justice.schoolmanagement.alldata;

import android.app.Application;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.justice.schoolmanagement.parent.ParentData;
import com.justice.schoolmanagement.student.StudentData;
import com.justice.schoolmanagement.student.StudentMarks;
import com.justice.schoolmanagement.teacher.TeacherData;

import java.util.List;

public class ApplicationClass extends Application {

    private static final String APPLICATION_ID = "AEBEC993-390A-CC14-FF29-1A31BA9A7000";
    private static final String API_KEY = "88CE15DA-73EF-4399-82FD-AD69D4C3412C";
    private static final String SERVER_URL = "https://api.backendless.com";


    @Override
    public void onCreate() {
        super.onCreate();
        Backendless.setUrl(SERVER_URL);
        Backendless.initApp(getApplicationContext(), APPLICATION_ID, API_KEY);
      //  loadDataFromDatabase();

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
