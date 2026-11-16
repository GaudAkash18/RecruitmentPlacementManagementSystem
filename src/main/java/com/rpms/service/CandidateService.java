package com.rpms.service;
import com.rpms.dao.CandidateDAO;import com.rpms.model.Candidate;import com.rpms.util.ActivityLogger;import java.sql.SQLException;import java.util.List;
public class CandidateService {
 private final CandidateDAO dao=new CandidateDAO();
 public void add(Candidate c)throws SQLException{
  if(c.name.isBlank())throw new IllegalArgumentException("Candidate name is required.");
  if(!c.email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))throw new IllegalArgumentException("Invalid email.");
  if(!c.mobile.matches("^\\d{10}$"))throw new IllegalArgumentException("Mobile must contain 10 digits.");
  if(c.percentage<0||c.percentage>100)throw new IllegalArgumentException("Percentage must be 0-100.");
  if(c.backlogs<0)throw new IllegalArgumentException("Backlog count cannot be negative.");
  dao.add(c);ActivityLogger.log("Candidate Created: "+c.code);
 }
 public void update(Candidate c)throws SQLException{if(c.percentage<0||c.percentage>100||c.backlogs<0)throw new IllegalArgumentException("Invalid percentage or backlog count.");dao.update(c);ActivityLogger.log("Candidate Updated: "+c.id);}
 public List<Candidate> all()throws SQLException{return dao.all();}
 public void deactivate(int id)throws SQLException{dao.updateStatus(id,"INACTIVE");ActivityLogger.log("Candidate Deactivated: "+id);}
}
