package com.rpms.dao;
import com.rpms.util.DBConnection;import java.sql.*;import java.time.LocalDate;
public class WorkflowDAO {
 public void apply(int candidateId,int jobId,String source)throws Exception{
  try(Connection c=DBConnection.getConnection()){
   c.setAutoCommit(false);
   try{
    CandidateRow x=candidate(c,candidateId);if(x==null)throw new Exception("Candidate not found.");if(!"ACTIVE".equals(x.status))throw new Exception("Candidate is not active.");
    JobRow j=job(c,jobId);if(j==null)throw new Exception("Job not found.");if(!"OPEN".equals(j.status))throw new Exception("Job is not open.");if(j.deadline.isBefore(LocalDate.now()))throw new Exception("Application deadline has passed.");
    if(x.percentage<j.minPercentage)throw new Exception("Not eligible: minimum percentage is "+j.minPercentage+".");
    if(j.minYear>0&&x.year<j.minYear)throw new Exception("Not eligible: passing year is below requirement.");
    if(j.maxYear>0&&x.year>j.maxYear)throw new Exception("Not eligible: passing year is above requirement.");
    if(x.backlogs>j.maxBacklogs)throw new Exception("Not eligible: backlog criteria not satisfied.");
    if(!j.technology.isBlank()&&!x.technology.equalsIgnoreCase(j.technology))throw new Exception("Not eligible: required technology is "+j.technology+".");
    try(PreparedStatement p=c.prepareStatement("INSERT INTO applications(application_code,candidate_id,job_id,eligibility_status,eligibility_reason,application_status,source) VALUES(?,?,?,'ELIGIBLE','All eligibility criteria satisfied','APPLIED',?)")){
     p.setString(1,"APP-"+System.currentTimeMillis());p.setInt(2,candidateId);p.setInt(3,jobId);p.setString(4,source);p.executeUpdate();}
    int appId;try(PreparedStatement p=c.prepareStatement("SELECT LAST_INSERT_ID()");ResultSet r=p.executeQuery()){r.next();appId=r.getInt(1);}
    history(c,appId,null,"APPLIED","Application submitted");c.commit();
   }catch(Exception e){c.rollback();if(e instanceof SQLIntegrityConstraintViolationException)throw new Exception("Candidate has already applied for this job.");throw e;}
  }
 }
 public void changeStatus(int appId,String next,String remarks)throws Exception{
  String[] allowed={"APPLIED","ELIGIBILITY_VERIFIED","SHORTLISTED","INTERVIEW_SCHEDULED","INTERVIEWED","SELECTED","REJECTED","OFFERED","JOINED","WITHDRAWN"};
  try(Connection c=DBConnection.getConnection()){c.setAutoCommit(false);try{
   String old;try(PreparedStatement p=c.prepareStatement("SELECT application_status FROM applications WHERE application_id=? FOR UPDATE")){p.setInt(1,appId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new Exception("Application not found.");old=r.getString(1);}}
   int a=java.util.Arrays.asList(allowed).indexOf(old),b=java.util.Arrays.asList(allowed).indexOf(next);
   boolean valid=next.equals("REJECTED")||next.equals("WITHDRAWN")||(b==a+1);
   if(!valid)throw new Exception("Invalid status transition: "+old+" -> "+next);
   try(PreparedStatement p=c.prepareStatement("UPDATE applications SET application_status=?,updated_at=NOW() WHERE application_id=?")){p.setString(1,next);p.setInt(2,appId);p.executeUpdate();}
   history(c,appId,old,next,remarks);c.commit();
  }catch(Exception e){c.rollback();throw e;}}
 }
 public void listApplications()throws SQLException{String q="SELECT a.application_id,a.application_code,ca.full_name,j.job_title,a.eligibility_status,a.application_status FROM applications a JOIN candidates ca ON ca.candidate_id=a.candidate_id JOIN jobs j ON j.job_id=a.job_id ORDER BY a.application_id DESC";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){System.out.printf("%-5s %-16s %-20s %-25s %-16s %s%n","ID","CODE","CANDIDATE","JOB","ELIGIBILITY","STATUS");while(r.next())System.out.printf("%-5d %-16s %-20s %-25s %-16s %s%n",r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6));}}
 public void scheduleInterview(int appId,int round,String type,LocalDate date,String mode,String interviewer)throws SQLException{
  try(Connection c=DBConnection.getConnection()){
   c.setAutoCommit(false);
   try{
    try(PreparedStatement p=c.prepareStatement("INSERT INTO interview_rounds(application_id,round_number,round_type,scheduled_date,mode,interviewer_name,status) VALUES(?,?,?,?,?,?,'SCHEDULED')")){p.setInt(1,appId);p.setInt(2,round);p.setString(3,type);p.setDate(4,java.sql.Date.valueOf(date));p.setString(5,mode);p.setString(6,interviewer);p.executeUpdate();}
    try(PreparedStatement p=c.prepareStatement("UPDATE applications SET application_status='INTERVIEW_SCHEDULED' WHERE application_id=? AND application_status='SHORTLISTED'")){p.setInt(1,appId);if(p.executeUpdate()==0)throw new SQLException("Application must be SHORTLISTED before scheduling an interview.");}
    history(c,appId,"SHORTLISTED","INTERVIEW_SCHEDULED","Interview scheduled");c.commit();
   }catch(SQLException e){c.rollback();throw e;}
  }
 }
 public void createOffer(int appId,String designation,java.math.BigDecimal pkg,LocalDate offerDate,LocalDate joinDate)throws SQLException{
  try(Connection c=DBConnection.getConnection()){
   c.setAutoCommit(false);
   try{
    try(PreparedStatement p=c.prepareStatement("INSERT INTO offers(offer_code,application_id,designation,offered_package_lpa,offer_date,expected_joining_date,offer_status) VALUES(?,?,?,?,?,?,'OFFERED')")){p.setString(1,"OFF-"+System.currentTimeMillis());p.setInt(2,appId);p.setString(3,designation);p.setBigDecimal(4,pkg);p.setDate(5,java.sql.Date.valueOf(offerDate));p.setDate(6,java.sql.Date.valueOf(joinDate));p.executeUpdate();}
    try(PreparedStatement p=c.prepareStatement("UPDATE applications SET application_status='OFFERED' WHERE application_id=? AND application_status='SELECTED'")){p.setInt(1,appId);if(p.executeUpdate()==0)throw new SQLException("Application must be SELECTED before creating an offer.");}
    history(c,appId,"SELECTED","OFFERED","Offer created");c.commit();
   }catch(SQLException e){c.rollback();throw e;}
  }
 }
 public void confirmJoining(int offerId,LocalDate actualDate)throws SQLException{
  try(Connection c=DBConnection.getConnection()){c.setAutoCommit(false);try{
   String q="SELECT o.application_id,o.designation,o.offered_package_lpa,a.candidate_id,j.company_id,j.job_id FROM offers o JOIN applications a ON a.application_id=o.application_id JOIN jobs j ON j.job_id=a.job_id WHERE o.offer_id=? AND o.offer_status='ACCEPTED'";
   int app,cand,company,job;String designation;java.math.BigDecimal pkg;
   try(PreparedStatement p=c.prepareStatement(q)){p.setInt(1,offerId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Accepted offer not found.");app=r.getInt(1);designation=r.getString(2);pkg=r.getBigDecimal(3);cand=r.getInt(4);company=r.getInt(5);job=r.getInt(6);}}
   try(PreparedStatement p=c.prepareStatement("INSERT INTO placements(candidate_id,company_id,job_id,offer_id,designation,final_package_lpa,actual_joining_date,joining_status) VALUES(?,?,?,?,?,?,?,'JOINED')")){p.setInt(1,cand);p.setInt(2,company);p.setInt(3,job);p.setInt(4,offerId);p.setString(5,designation);p.setBigDecimal(6,pkg);p.setDate(7,Date.valueOf(actualDate));p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("UPDATE candidates SET placement_status='PLACED' WHERE candidate_id=?")){p.setInt(1,cand);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("UPDATE applications SET application_status='JOINED' WHERE application_id=?")){p.setInt(1,app);p.executeUpdate();}
   history(c,app,"OFFERED","JOINED","Joining confirmed");c.commit();
  }catch(SQLException e){c.rollback();throw e;}}
 }
 public void addFeedback(int interviewId,double technical,double communication,double problem,double overall,String recommendation,String remarks)throws SQLException{
  if(technical<0||technical>5||communication<0||communication>5||problem<0||problem>5||overall<0||overall>5)throw new IllegalArgumentException("Ratings must be between 0 and 5.");
  try(Connection c=DBConnection.getConnection()){c.setAutoCommit(false);try{
   try(PreparedStatement p=c.prepareStatement("INSERT INTO interview_feedback(interview_id,technical_rating,communication_rating,problem_solving_rating,overall_rating,recommendation,remarks) VALUES(?,?,?,?,?,?,?)")){p.setInt(1,interviewId);p.setDouble(2,technical);p.setDouble(3,communication);p.setDouble(4,problem);p.setDouble(5,overall);p.setString(6,recommendation);p.setString(7,remarks);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("UPDATE interview_rounds SET status='COMPLETED' WHERE interview_id=?")){p.setInt(1,interviewId);p.executeUpdate();}
   if(recommendation.equals("SELECTED")||recommendation.equals("REJECTED")||recommendation.equals("ON_HOLD")||recommendation.equals("NEXT_ROUND")){
    String next=recommendation.equals("SELECTED")?"SELECTED":recommendation.equals("REJECTED")?"REJECTED":"INTERVIEWED";
    int appId;try(PreparedStatement p=c.prepareStatement("SELECT application_id FROM interview_rounds WHERE interview_id=?")){p.setInt(1,interviewId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Interview not found.");appId=r.getInt(1);}}
    try(PreparedStatement p=c.prepareStatement("UPDATE applications SET application_status=? WHERE application_id=?")){p.setString(1,next);p.setInt(2,appId);p.executeUpdate();}
    history(c,appId,"INTERVIEW_SCHEDULED",next,"Interview feedback: "+recommendation);
   }
   c.commit();
  }catch(SQLException e){c.rollback();throw e;}}
 }
 public void updateOfferStatus(int offerId,String status)throws SQLException{
  if(!java.util.List.of("OFFERED","ACCEPTED","REJECTED","EXPIRED","WITHDRAWN").contains(status))throw new IllegalArgumentException("Invalid offer status.");
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE offers SET offer_status=? WHERE offer_id=?")){p.setString(1,status);p.setInt(2,offerId);if(p.executeUpdate()==0)throw new SQLException("Offer not found.");}
 }
 private void history(Connection c,int app,String old,String next,String remarks)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO application_history(application_id,old_status,new_status,remarks) VALUES(?,?,?,?)")){p.setInt(1,app);p.setString(2,old);p.setString(3,next);p.setString(4,remarks);p.executeUpdate();}}
 private CandidateRow candidate(Connection c,int id)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT profile_status,percentage,passing_year,backlog_count,COALESCE(primary_technology,'') FROM candidates WHERE candidate_id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?new CandidateRow(r.getString(1),r.getDouble(2),r.getInt(3),r.getInt(4),r.getString(5)):null;}}}
 private JobRow job(Connection c,int id)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT j.status,j.application_deadline,j.min_percentage,j.min_passing_year,j.max_passing_year,j.max_backlogs,COALESCE(s.skill_name,'') technology FROM jobs j LEFT JOIN job_skills js ON js.job_id=j.job_id LEFT JOIN skills s ON s.skill_id=js.skill_id WHERE j.job_id=? LIMIT 1")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?new JobRow(r.getString(1),r.getDate(2).toLocalDate(),r.getBigDecimal(3)==null?0:r.getBigDecimal(3).doubleValue(),r.getInt(4),r.getInt(5),r.getInt(6),r.getString(7)):null;}}}
 private record CandidateRow(String status,double percentage,int year,int backlogs,String technology){}
 private record JobRow(String status,LocalDate deadline,double minPercentage,int minYear,int maxYear,int maxBacklogs,String technology){}
}
