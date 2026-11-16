package com.rpms.dao;
import com.rpms.util.DBConnection;import java.sql.*;
public class ReportDAO {
 private void run(String title,String sql)throws SQLException{System.out.println("\n--- "+title+" ---");try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){ResultSetMetaData m=r.getMetaData();int n=m.getColumnCount();for(int i=1;i<=n;i++)System.out.printf("%-24s",m.getColumnLabel(i));System.out.println();while(r.next()){for(int i=1;i<=n;i++)System.out.printf("%-24s",String.valueOf(r.getObject(i)));System.out.println();}}}
 public void menu()throws SQLException{
  run("Candidates by Technology","SELECT COALESCE(primary_technology,'Unknown') technology,COUNT(*) total FROM candidates GROUP BY primary_technology");
  run("Open Jobs by Company","SELECT c.company_name,j.job_title,j.openings,j.location FROM jobs j JOIN companies c ON c.company_id=j.company_id WHERE j.status='OPEN'");
  run("Applications by Status","SELECT application_status,COUNT(*) total FROM applications GROUP BY application_status");
  run("Company-wise Placements","SELECT c.company_name,COUNT(*) placed FROM placements p JOIN companies c ON c.company_id=p.company_id WHERE p.joining_status='JOINED' GROUP BY c.company_name ORDER BY placed DESC");
  run("Placed Candidates","SELECT full_name,email,primary_technology FROM candidates WHERE placement_status='PLACED'");
  run("Unplaced Candidates","SELECT full_name,email,primary_technology FROM candidates WHERE placement_status='NOT_PLACED'");
  run("Pending Interviews","SELECT i.interview_id,ca.full_name,j.job_title,i.round_type,i.scheduled_date,i.status FROM interview_rounds i JOIN applications a ON a.application_id=i.application_id JOIN candidates ca ON ca.candidate_id=a.candidate_id JOIN jobs j ON j.job_id=a.job_id WHERE i.status='SCHEDULED'");
 }
}
