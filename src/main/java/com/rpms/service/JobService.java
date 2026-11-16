package com.rpms.service;
import com.rpms.dao.JobDAO;import com.rpms.model.Job;import com.rpms.util.ActivityLogger;import java.sql.SQLException;
public class JobService {
 private final JobDAO dao=new JobDAO();
 public void add(Job j)throws SQLException{
  if(j.openings<=0)throw new IllegalArgumentException("Openings must be greater than zero.");
  if(j.minPackage!=null&&j.maxPackage!=null&&j.minPackage.compareTo(j.maxPackage)>0)throw new IllegalArgumentException("Minimum package cannot exceed maximum package.");
  if(j.deadline.isBefore(j.posted))throw new IllegalArgumentException("Deadline cannot be before posted date.");
  if(j.minPercentage!=null&&(j.minPercentage.doubleValue()<0||j.minPercentage.doubleValue()>100))throw new IllegalArgumentException("Minimum percentage must be 0-100.");
  dao.add(j);ActivityLogger.log("Job Created: "+j.code);
 }
}
