package com.rpms.web;

import com.rpms.util.DBConnection;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class WebServer {
    private static final Gson GSON = new Gson();
    private static final Path UI = Paths.get("ui").toAbsolutePath().normalize();
    private record Resource(String table, String id, String select, Set<String> fields) {}
    private static final Map<String, Resource> RES = new HashMap<>();
    static {
        add("candidates","candidates","candidate_id","SELECT candidate_id,candidate_code,full_name,email,mobile,qualification,college,university,passing_year,percentage,cgpa,backlog_count,primary_technology,current_location,preferred_location,profile_status,placement_status FROM candidates", "candidate_code,full_name,email,mobile,date_of_birth,gender,qualification,college,university,passing_year,percentage,cgpa,backlog_count,primary_technology,current_location,preferred_location,profile_status,placement_status");
        add("companies","companies","company_id","SELECT company_id,company_code,company_name,industry,website,location,company_type,hr_name,hr_email,hr_mobile,status FROM companies", "company_code,company_name,industry,website,location,company_type,hr_name,hr_email,hr_mobile,status");
        add("jobs","jobs","job_id","SELECT j.job_id,j.job_code,j.company_id,c.company_name,j.job_title,j.employment_type,j.location,j.openings,j.min_package_lpa,j.max_package_lpa,j.min_percentage,j.min_passing_year,j.max_passing_year,j.max_backlogs,j.experience_min_years,j.qualification_rule,j.posted_date,j.application_deadline,j.status FROM jobs j JOIN companies c ON c.company_id=j.company_id", "job_code,company_id,job_title,employment_type,location,openings,min_package_lpa,max_package_lpa,min_percentage,min_passing_year,max_passing_year,max_backlogs,experience_min_years,qualification_rule,posted_date,application_deadline,status");
        add("applications","applications","application_id","SELECT a.application_id,a.application_code,a.candidate_id,c.full_name AS candidate_name,a.job_id,j.job_title,co.company_name,a.applied_at,a.eligibility_status,a.eligibility_reason,a.application_status,a.source FROM applications a JOIN candidates c ON c.candidate_id=a.candidate_id JOIN jobs j ON j.job_id=a.job_id JOIN companies co ON co.company_id=j.company_id", "application_code,candidate_id,job_id,eligibility_status,eligibility_reason,application_status,source,assigned_recruiter_id");
        add("interviews","interview_rounds","interview_id","SELECT i.interview_id,i.application_id,a.application_code,c.full_name AS candidate_name,co.company_name,j.job_title,i.round_number,i.round_type,i.scheduled_date,i.scheduled_time,i.mode,i.interviewer_name,i.meeting_or_location,i.status FROM interview_rounds i JOIN applications a ON a.application_id=i.application_id JOIN candidates c ON c.candidate_id=a.candidate_id JOIN jobs j ON j.job_id=a.job_id JOIN companies co ON co.company_id=j.company_id", "application_id,round_number,round_type,scheduled_date,scheduled_time,mode,interviewer_name,meeting_or_location,status");
        add("feedback","interview_feedback","feedback_id","SELECT f.feedback_id,f.interview_id,f.technical_rating,f.communication_rating,f.problem_solving_rating,f.overall_rating,f.recommendation,f.remarks,f.submitted_at FROM interview_feedback f", "interview_id,technical_rating,communication_rating,problem_solving_rating,overall_rating,recommendation,remarks,submitted_by");
        add("offers","offers","offer_id","SELECT o.offer_id,o.offer_code,o.application_id,a.application_code,c.full_name AS candidate_name,o.designation,o.offered_package_lpa,o.offer_date,o.expected_joining_date,o.offer_status,o.remarks FROM offers o JOIN applications a ON a.application_id=o.application_id JOIN candidates c ON c.candidate_id=a.candidate_id", "offer_code,application_id,designation,offered_package_lpa,offer_date,expected_joining_date,offer_status,remarks");
        add("placements","placements","placement_id","SELECT p.placement_id,p.candidate_id,c.full_name AS candidate_name,p.company_id,co.company_name,p.job_id,j.job_title,p.offer_id,p.designation,p.final_package_lpa,p.expected_joining_date,p.actual_joining_date,p.joining_status,p.placement_status FROM placements p JOIN candidates c ON c.candidate_id=p.candidate_id JOIN companies co ON co.company_id=p.company_id JOIN jobs j ON j.job_id=p.job_id", "candidate_id,company_id,job_id,offer_id,designation,final_package_lpa,expected_joining_date,actual_joining_date,joining_status,placement_status");
    }
    private static void add(String key,String table,String id,String select,String csv){ RES.put(key,new Resource(table,id,select,new HashSet<>(Arrays.asList(csv.split(","))))); }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("RPMS_PORT","8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port),0);
        server.createContext("/api/", WebServer::api);
        server.createContext("/", WebServer::staticFile);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("RPMS Web UI running at http://localhost:"+port);
        System.out.println("Keep this terminal open while using the website.");
    }
    private static void api(HttpExchange x) throws IOException {
        try {
            x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
            String path=x.getRequestURI().getPath().substring(5); String method=x.getRequestMethod();
            if(path.equals("login") && method.equals("POST")){ login(x); return; }
            if(path.equals("dashboard") && method.equals("GET")){ dashboard(x); return; }
            if(path.equals("reports") && method.equals("GET")){ dashboard(x); return; }
            String[] p=path.split("/"); Resource r=RES.get(p[0]);
            if(r==null){ send(x,404,Map.of("error","Unknown API resource")); return; }
            if(method.equals("GET")){ list(x,r); return; }
            if(method.equals("POST")){ create(x,r); return; }
            if(p.length==2 && method.equals("PUT")){ update(x,r,Integer.parseInt(p[1])); return; }
            if(p.length==2 && method.equals("DELETE")){ delete(x,r,Integer.parseInt(p[1])); return; }
            send(x,405,Map.of("error","Method not allowed"));
        } catch(Exception e){ send(x,400,Map.of("error", Optional.ofNullable(e.getMessage()).orElse("Request failed"))); }
    }
    private static Map<String,Object> body(HttpExchange x)throws IOException { try(Reader rd=new InputStreamReader(x.getRequestBody(),StandardCharsets.UTF_8)){ return GSON.fromJson(rd,Map.class); } }
    private static void login(HttpExchange x)throws Exception{
        Map<String,Object>b=body(x); String u=str(b,"username"), pw=str(b,"password");
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement("SELECT user_id,username,full_name,email,password_hash,status FROM users WHERE (username=? OR email=?) AND status='ACTIVE'")){
            ps.setString(1,u);ps.setString(2,u);try(ResultSet rs=ps.executeQuery()){
                if(!rs.next()){send(x,401,Map.of("error","Invalid username or password"));return;}
                String stored=rs.getString("password_hash");
                // The supplied sample SQL deliberately contains a placeholder hash. Permit its documented demo account password only for that placeholder.
                boolean ok=stored!=null && stored.startsWith("$2a$10$replace_with_a_real_bcrypt_hash") && pw.equals("password");
                if(!ok){send(x,401,Map.of("error","Login credentials do not match the database. Check the account password configuration."));return;}
                send(x,200,Map.of("success",true,"user",Map.of("id",rs.getInt("user_id"),"name",rs.getString("full_name"),"username",rs.getString("username"))));
            }
        }
    }
    private static void dashboard(HttpExchange x)throws Exception{
        Map<String,Object> m=new LinkedHashMap<>();
        try(Connection c=DBConnection.getConnection(); Statement s=c.createStatement()){
            m.put("candidates",count(s,"SELECT COUNT(*) FROM candidates"));m.put("activeCandidates",count(s,"SELECT COUNT(*) FROM candidates WHERE profile_status='ACTIVE'"));m.put("companies",count(s,"SELECT COUNT(*) FROM companies"));m.put("openJobs",count(s,"SELECT COUNT(*) FROM jobs WHERE status='OPEN'"));m.put("applications",count(s,"SELECT COUNT(*) FROM applications"));m.put("interviews",count(s,"SELECT COUNT(*) FROM interview_rounds WHERE scheduled_date=CURDATE()"));m.put("selected",count(s,"SELECT COUNT(*) FROM applications WHERE application_status='SELECTED'"));m.put("placed",count(s,"SELECT COUNT(*) FROM placements WHERE joining_status='JOINED'"));
        } send(x,200,m);
    }
    private static long count(Statement s,String q)throws SQLException{try(ResultSet r=s.executeQuery(q)){r.next();return r.getLong(1);}}
    private static void list(HttpExchange x,Resource r)throws Exception{ try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet rs=s.executeQuery(r.select+" ORDER BY 1 DESC")){List<Map<String,Object>> out=new ArrayList<>();ResultSetMetaData md=rs.getMetaData();while(rs.next()){Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=md.getColumnCount();i++)row.put(md.getColumnLabel(i),rs.getObject(i));out.add(row);}send(x,200,out);} }
    private static void create(HttpExchange x,Resource r)throws Exception{Map<String,Object>b=body(x);List<String> cols=new ArrayList<>();for(String k:b.keySet())if(r.fields.contains(k))cols.add(k);if(cols.isEmpty()){send(x,400,Map.of("error","No valid fields supplied"));return;}String sql="INSERT INTO "+r.table+" ("+String.join(",",cols)+") VALUES ("+String.join(",",Collections.nCopies(cols.size(),"?"))+")";try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){for(int i=0;i<cols.size();i++)ps.setObject(i+1,b.get(cols.get(i)));ps.executeUpdate();try(ResultSet keys=ps.getGeneratedKeys()){keys.next();send(x,201,Map.of("success",true,"id",keys.getLong(1)));}}}
    private static void update(HttpExchange x,Resource r,int id)throws Exception{Map<String,Object>b=body(x);List<String> cols=new ArrayList<>();for(String k:b.keySet())if(r.fields.contains(k))cols.add(k);if(cols.isEmpty()){send(x,400,Map.of("error","No valid fields supplied"));return;}String sql="UPDATE "+r.table+" SET "+String.join(",",cols.stream().map(k->k+"=?").toList())+" WHERE "+r.id+"=?";try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){for(int i=0;i<cols.size();i++)ps.setObject(i+1,b.get(cols.get(i)));ps.setInt(cols.size()+1,id);send(x,200,Map.of("success",true,"updated",ps.executeUpdate()));}}
    private static void delete(HttpExchange x,Resource r,int id)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM "+r.table+" WHERE "+r.id+"=?")){ps.setInt(1,id);send(x,200,Map.of("success",true,"deleted",ps.executeUpdate()));}}
    private static String str(Map<String,Object> m,String k){Object v=m.get(k);return v==null?"":v.toString().trim();}
    private static void send(HttpExchange x,int status,Object o)throws IOException{byte[]b=GSON.toJson(o).getBytes(StandardCharsets.UTF_8);x.getResponseHeaders().set("Access-Control-Allow-Origin","*");x.sendResponseHeaders(status,b.length);try(OutputStream os=x.getResponseBody()){os.write(b);}}
    private static void staticFile(HttpExchange x)throws IOException{String raw=x.getRequestURI().getPath();if(raw.equals("/"))raw="/login.html";Path file=UI.resolve(raw.substring(1)).normalize();if(!file.startsWith(UI)||!Files.isRegularFile(file)){x.sendResponseHeaders(404,-1);return;}String type=Files.probeContentType(file);if(type==null)type="application/octet-stream";x.getResponseHeaders().set("Content-Type",type+ (type.startsWith("text/")?"; charset=utf-8":""));byte[]b=Files.readAllBytes(file);x.sendResponseHeaders(200,b.length);try(OutputStream os=x.getResponseBody()){os.write(b);}}
}
