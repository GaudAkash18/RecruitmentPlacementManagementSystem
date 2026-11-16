package com.rpms.model;
import java.math.BigDecimal; import java.time.LocalDate;
public class Job {
 public int id,companyId,openings,maxBacklogs,minYear,maxYear; public String code,title,technology,location,status; public BigDecimal minPackage,maxPackage,minPercentage; public LocalDate posted,deadline;
 public Job(int id,int companyId,String code,String title,String technology,String location,int openings,BigDecimal minPackage,BigDecimal maxPackage,BigDecimal minPercentage,int minYear,int maxYear,int maxBacklogs,LocalDate posted,LocalDate deadline,String status){
 this.id=id;this.companyId=companyId;this.code=code;this.title=title;this.technology=technology;this.location=location;this.openings=openings;this.minPackage=minPackage;this.maxPackage=maxPackage;this.minPercentage=minPercentage;this.minYear=minYear;this.maxYear=maxYear;this.maxBacklogs=maxBacklogs;this.posted=posted;this.deadline=deadline;this.status=status;
 }
}
