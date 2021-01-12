package com.justice.schoolmanagement.alldata;

import android.os.Environment;

import com.justice.schoolmanagement.presentation.LoginData;
import com.justice.schoolmanagement.presentation.ui.parent.model.ParentData;
import com.justice.schoolmanagement.presentation.ui.student.models.StudentData;
import com.justice.schoolmanagement.presentation.ui.student.models.StudentMarks;
import com.justice.schoolmanagement.presentation.ui.teacher.model.TeacherData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AllData {
    private static String path;

    public static List<LoginData> loginDataList = new ArrayList<>();
    public static List<ParentData> parentDataList = new ArrayList<>();

    public static List<TeacherData> teacherDataList = new ArrayList<>();
    public static List<StudentData> studentDataList = new ArrayList<>();
    public static List<StudentMarks> studentMarksList = new ArrayList<>();




    public static void createDirectoryForDataStorage() {
      path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/SchoolManagement";

        File dir = new File(path);
        dir.mkdirs();
    }


    public static void readAllDataFromFiles() {
        readStudentMarksFile();
        readLoginDataFile();
        readParentDataFile();
        readTeacherDataFile();
        readStudentDataFile();
    }

    private static void readStudentMarksFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(path + "/student_marks.bin");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            studentMarksList = (ArrayList<StudentMarks>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readStudentDataFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(path + "/student_data.bin");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            studentDataList = (ArrayList<StudentData>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readTeacherDataFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(path + "/teacher_data.bin");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            teacherDataList = (ArrayList<TeacherData>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readParentDataFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(path + "/parent_data.bin");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            parentDataList = (ArrayList<ParentData>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readLoginDataFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream(path + "/login_data.bin");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            loginDataList = (ArrayList<LoginData>) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void writeAllDataToFiles() {
       writeStudentMarksFile();
        writeLoginDataFile();
        writeParentDataFile();
        writeTeacherDataFile();
        writeStudentDataFile();
    }

    private static void writeStudentMarksFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + "/student_marks.bin");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(studentMarksList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeStudentDataFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + "/student_data.bin");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(studentDataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeTeacherDataFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + "/teacher_data.bin");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(teacherDataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeParentDataFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + "/parent_data.bin");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(parentDataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeLoginDataFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + "/login_data.bin");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(loginDataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
