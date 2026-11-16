package com.rpms.dao;
import com.rpms.model.Job;import com.rpms.util.DBConnection;import java.sql.*;import java.util.*;
public class JobDAO {
 public void add(Job j)throws SQLException{
  try(Connection c=DBConnection.getConnection()){
   c.setAutoCommit(false);
   try{
    String q="INSERT INTO jobs(job_code,company_id,job_title,location,openings,min_package_lpa,max_package_lpa,min_percentage,min_passing_year,max_passing_year,max_backlogs,posted_date,application_deadline,status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try(PreparedStatement p=c.prepareStatement(q,Statement.RETURN_GENERATED_KEYS)){
     p.setString(1,j.code);p.setInt(2,j.companyId);p.setString(3,j.title);p.setString(4,j.location);p.setInt(5,j.openings);p.setBigDecimal(6,j.minPackage);p.setBigDecimal(7,j.maxPackage);p.setBigDecimal(8,j.minPercentage);p.setInt(9,j.minYear);p.setInt(10,j.maxYear);p.setInt(11,j.maxBacklogs);p.setDate(12,java.sql.Date.valueOf(j.posted));p.setDate(13,java.sql.Date.valueOf(j.deadline));p.setString(14,j.status);p.executeUpdate();
     try(ResultSet keys=p.getGeneratedKeys()){keys.next();int jobId=keys.getInt(1);
      if(j.technology!=null&&!j.technology.isBlank()){
       int skillId;
       try(PreparedStatement find=c.prepareStatement("SELECT skill_id FROM skills WHERE LOWER(skill_name)=LOWER(?)")){find.setString(1,j.technology);try(ResultSet r=find.executeQuery()){if(r.next())skillId=r.getInt(1);else{try(PreparedStatement ins=c.prepareStatement("INSERT INTO skills(skill_name,category) VALUES(?,'Programming')",Statement.RETURN_GENERATED_KEYS)){ins.setString(1,j.technology);ins.executeUpdate();try(ResultSet k=ins.getGeneratedKeys()){k.next();skillId=k.getInt(1);}}}}}
       try(PreparedStatement map=c.prepareStatement("INSERT INTO job_skills(job_id,skill_id,minimum_level,mandatory_flag) VALUES(?,?,'BASIC',1)")){map.setInt(1,jobId);map.setInt(2,skillId);map.executeUpdate();}
      }
     }
    }
    c.commit();
   }catch(SQLException e){c.rollback();throw e;}
  }
 }
 public Job find(int id)throws SQLException{String q="SELECT j.*,COALESCE((SELECT s.skill_name FROM job_skills js JOIN skills s ON s.skill_id=js.skill_id WHERE js.job_id=j.job_id LIMIT 1),'') technology FROM jobs j WHERE j.job_id=?";
 try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;return new Job(r.getInt("job_id"),r.getInt("company_id"),r.getString("job_code"),r.getString("job_title"),r.getString("technology"),r.getString("location"),r.getInt("openings"),r.getBigDecimal("min_package_lpa"),r.getBigDecimal("max_package_lpa"),r.getBigDecimal("min_percentage"),r.getInt("min_passing_year"),r.getInt("max_passing_year"),r.getInt("max_backlogs"),r.getDate("posted_date").toLocalDate(),r.getDate("application_deadline").toLocalDate(),r.getString("status"));}}}
 public void list()throws SQLException{String q="SELECT j.job_id,j.job_code,c.company_name,j.job_title,j.location,j.openings,j.status,j.application_deadline FROM jobs j JOIN companies c ON c.company_id=j.company_id ORDER BY j.job_id DESC";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){System.out.printf("%-5s %-16s %-20s %-28s %-15s %-8s %-12s%n","ID","JOB CODE","COMPANY","TITLE","LOCATION","OPEN","STATUS");while(r.next())System.out.printf("%-5d %-16s %-20s %-28s %-15s %-8d %-12s%n",r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getInt(6),r.getString(7));}}
}
