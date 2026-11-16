USE recruitment_placement;
INSERT INTO roles(role_code,role_name) VALUES ('ADMIN','Administrator'),('RECRUITER','Recruiter'),('PLACEMENT_OFFICER','Placement Officer');
INSERT INTO users(username,password_hash,full_name,email,role_id) VALUES
('admin','$2a$10$replace_with_a_real_bcrypt_hash','System Admin','admin@demo.com',1),
('recruiter1','$2a$10$replace_with_a_real_bcrypt_hash','Demo Recruiter','recruiter@demo.com',2),
('placement1','$2a$10$replace_with_a_real_bcrypt_hash','Placement Officer','placement@demo.com',3);
INSERT INTO skills(skill_name,category) VALUES ('Java','Programming'),('Python','Programming'),('SQL','Database'),('HTML','Web'),('CSS','Web'),('JavaScript','Web'),('React','Web'),('Spring Boot','Framework'),('C++','Programming'),('Testing','QA');
INSERT INTO companies(company_code,company_name,industry,location,company_type,hr_name,hr_email,status) VALUES
('COM-001','Infosys','IT Services','Pune','MNC','Amit Shah','hr@infosys.demo','ACTIVE'),
('COM-002','TCS','IT Services','Mumbai','MNC','Neha Rao','hr@tcs.demo','ACTIVE'),
('COM-003','Wipro','IT Services','Pune','MNC','Ravi Kumar','hr@wipro.demo','ACTIVE'),
('COM-004','Capgemini','Consulting','Nashik','MNC','Pooja Patil','hr@capgemini.demo','ACTIVE'),
('COM-005','Tech Mahindra','IT Services','Pune','MNC','Anil Das','hr@techm.demo','ACTIVE'),
('COM-006','Persistent Systems','Software','Pune','MID_SIZE','Riya Sen','hr@persistent.demo','ACTIVE'),
('COM-007','Zensar','IT Services','Pune','MID_SIZE','Karan Mehta','hr@zensar.demo','ACTIVE'),
('COM-008','Cognizant','IT Services','Mumbai','MNC','Sara Khan','hr@cognizant.demo','ACTIVE');
INSERT INTO qualifications(qualification_name) VALUES ('BCA'),('BSc'),('BE'),('BTech'),('MCA'),('MSc');
INSERT INTO locations(location_name,state_name) VALUES ('Pune','Maharashtra'),('Mumbai','Maharashtra'),('Nashik','Maharashtra'),('Bengaluru','Karnataka'),('Hyderabad','Telangana');
