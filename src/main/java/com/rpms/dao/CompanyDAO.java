package com.rpms.dao;
import com.rpms.util.DBConnection; import java.sql.*;
public class CompanyDAO {
 public void add(String code,String name,String industry,String location,String hr,String email)throws SQLException{
  try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO companies(company_code,company_name,industry,location,hr_name,hr_email,status) VALUES(?,?,?,?,?,?,'ACTIVE')")){
   p.setString(1,code);p.setString(2,name);p.setString(3,industry);p.setString(4,location);p.setString(5,hr);p.setString(6,email);p.executeUpdate();
  }
 }
 public void list()throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT company_id,company_code,company_name,industry,location,status FROM companies ORDER BY company_id");ResultSet r=p.executeQuery()){System.out.printf("%-5s %-14s %-25s %-18s %-18s %s%n","ID","CODE","COMPANY","INDUSTRY","LOCATION","STATUS");while(r.next())System.out.printf("%-5d %-14s %-25s %-18s %-18s %s%n",r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6));}}
}
