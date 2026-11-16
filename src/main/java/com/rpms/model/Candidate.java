package com.rpms.model;
public class Candidate {
 public int id,passingYear,backlogs; public String code,name,email,mobile,qualification,college,university,technology,location,status; public double percentage;
 public Candidate(int id,String code,String name,String email,String mobile,String qualification,String college,String university,int year,double percentage,int backlogs,String technology,String location,String status){
  this.id=id;this.code=code;this.name=name;this.email=email;this.mobile=mobile;this.qualification=qualification;this.college=college;this.university=university;this.passingYear=year;this.percentage=percentage;this.backlogs=backlogs;this.technology=technology;this.location=location;this.status=status;
 }
}
