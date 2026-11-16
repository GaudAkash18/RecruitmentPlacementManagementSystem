package com.rpms.dao;
import com.rpms.model.Candidate; import com.rpms.util.DBConnection; import java.sql.*; import java.util.*;
public class CandidateDAO {
 public void add(Candidate c)throws SQLException{
  String q="INSERT INTO candidates(candidate_code,full_name,email,mobile,qualification,college,university,passing_year,percentage,backlog_count,primary_technology,current_location,profile_status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
  try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(q)){
   ps.setString(1,c.code);ps.setString(2,c.name);ps.setString(3,c.email);ps.setString(4,c.mobile);ps.setString(5,c.qualification);ps.setString(6,c.college);ps.setString(7,c.university);ps.setInt(8,c.passingYear);ps.setDouble(9,c.percentage);ps.setInt(10,c.backlogs);ps.setString(11,c.technology);ps.setString(12,c.location);ps.setString(13,c.status);ps.executeUpdate();
  }
 }
 public List<Candidate> all()throws SQLException{
  List<Candidate> out=new ArrayList<>();String q="SELECT * FROM candidates ORDER BY candidate_id DESC";
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}return out;
 }
 public Candidate find(int id)throws SQLException{return one("SELECT * FROM candidates WHERE candidate_id=?",id);}
 public Candidate one(String q,int id)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}}
 public void update(Candidate c)throws SQLException{
  String q="UPDATE candidates SET full_name=?,email=?,mobile=?,qualification=?,college=?,university=?,passing_year=?,percentage=?,backlog_count=?,primary_technology=?,current_location=?,profile_status=?,updated_at=NOW() WHERE candidate_id=?";
  try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(q)){
   ps.setString(1,c.name);ps.setString(2,c.email);ps.setString(3,c.mobile);ps.setString(4,c.qualification);ps.setString(5,c.college);ps.setString(6,c.university);ps.setInt(7,c.passingYear);ps.setDouble(8,c.percentage);ps.setInt(9,c.backlogs);ps.setString(10,c.technology);ps.setString(11,c.location);ps.setString(12,c.status);ps.setInt(13,c.id);if(ps.executeUpdate()==0)throw new SQLException("Candidate not found.");
  }
 }
 public void updateStatus(int id,String status)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE candidates SET profile_status=?,updated_at=NOW() WHERE candidate_id=?")){p.setString(1,status);p.setInt(2,id);if(p.executeUpdate()==0)throw new SQLException("Candidate not found.");}}
 private Candidate map(ResultSet r)throws SQLException{return new Candidate(r.getInt("candidate_id"),r.getString("candidate_code"),r.getString("full_name"),r.getString("email"),r.getString("mobile"),r.getString("qualification"),r.getString("college"),r.getString("university"),r.getInt("passing_year"),r.getDouble("percentage"),r.getInt("backlog_count"),r.getString("primary_technology"),r.getString("current_location"),r.getString("profile_status"));}
}
