package com.justice.schoolmanagement.parent;

import java.io.Serializable;

public class ParentData implements Serializable {
    private String fullName;
    private String contact;
    private String firstName;
    private String lastName;
    private String city;
    private String jobStatus;
    private String age;
    private String gender;
    private String jobType;
    private String email;

    private String objectId;
public ParentData(){

}
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ParentData(String fullName, String contact, String firstName, String lastName, String city, String jobStatus, String age, String gender, String jobType, String email) {
        this.fullName = fullName;
        this.contact = contact;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.jobStatus = jobStatus;
        this.age = age;
        this.gender = gender;
        this.jobType = jobType;
        this.email = email;
    }
}
