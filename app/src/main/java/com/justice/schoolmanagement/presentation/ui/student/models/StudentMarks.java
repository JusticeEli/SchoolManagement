package com.justice.schoolmanagement.presentation.ui.student.models;

import java.io.Serializable;

public class StudentMarks implements Serializable, Comparable<StudentMarks> {
    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getClassGrade() {
        return classGrade;
    }

    public void setClassGrade(int classGrade) {
        this.classGrade = classGrade;
    }


    private String fullName;
    private String email;
    private int classGrade;
    private int math;
    private int science;
    private int english;
    private int kiswahili;
    private int sst_cre;
    private int totalMarks;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StudentMarks() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public StudentMarks(int math, int science, int english, int kiswahili, int sst_cre, int totalMarks, String fullName) {
        this.math = math;
        this.science = science;
        this.english = english;
        this.kiswahili = kiswahili;
        this.sst_cre = sst_cre;
        this.totalMarks = totalMarks;
        this.fullName = fullName;
    }

    public StudentMarks(String fullName) {
        this.fullName = fullName;

    }

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    public int getMath() {
        return math;
    }

    public void setMath(int math) {
        this.math = math;
    }

    public int getScience() {
        return science;
    }

    public void setScience(int science) {
        this.science = science;
    }

    public int getEnglish() {
        return english;
    }

    public void setEnglish(int english) {
        this.english = english;
    }

    public int getKiswahili() {
        return kiswahili;
    }

    public void setKiswahili(int kiswahili) {
        this.kiswahili = kiswahili;
    }

    public int getSst_cre() {
        return sst_cre;
    }

    public void setSst_cre(int sst_cre) {
        this.sst_cre = sst_cre;
    }

    @Override
    public int compareTo(StudentMarks o) {
        if (totalMarks < o.totalMarks) {
            return 1;
        } else if (totalMarks > o.totalMarks) {
            return -1;
        }
        return 0;
    }
}
