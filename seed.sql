-- Make sure DB exists and select it
--CREATE DATABASE IF NOT EXISTS ths_enhanced DEFAULT CHARACTER SET utf8mb4;
USE ths_enhanced;

-- USERS
INSERT INTO users (username, pass_hash, role) VALUES
  ('alice',    SHA2('alice123',   256), 'patient'),
  ('bob',      SHA2('bob123',     256), 'patient'),
  ('charlie',  SHA2('charlie123', 256), 'patient'),
  ('dr.ahmed', SHA2('dr123',      256), 'doctor'),
  ('dr.lee',   SHA2('drlee123',   256), 'doctor'),
  ('admin',    SHA2('admin123',   256), 'admin')
ON DUPLICATE KEY UPDATE role = VALUES(role);

-- USER PROFILES
INSERT INTO user_profile (username, full_name, dob, gender, address) VALUES
  ('alice',   'Alice Brown',   '1994-09-02', 'F', '88 River Rd'),
  ('bob',     'Bob Smith',     '1992-01-22', 'M', '7 Lake Ave'),
  ('charlie', 'Charlie Ng',    '1995-03-11', 'M', '15 Forest Dr'),
  ('dr.ahmed','Dr Ahmed',      '1980-06-10', 'M', 'CQ Hospital'),
  ('dr.lee',  'Dr Lee',        '1978-11-05', 'F', 'Regional Clinic')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- APPOINTMENTS  (when_at, location, status)
INSERT INTO appointments (patient, doctor, when_at, location, status) VALUES
  ('alice',   'dr.lee',   NOW() - INTERVAL 3 DAY, 'Clinic A', 'COMPLETED'),
  ('alice',   'dr.lee',   NOW() + INTERVAL 2 DAY, 'Clinic A', 'PENDING'),
  ('bob',     'dr.lee',   NOW() + INTERVAL 5 DAY, 'Clinic B', 'PENDING'),
  ('charlie', 'dr.ahmed', NOW() + INTERVAL 1 DAY, 'Online',   'PENDING');

-- VITALS (pulse, temp_c, resp, bp, recorded_at)
INSERT INTO vitals (patient, pulse, temp_c, resp, bp, recorded_at) VALUES
  ('alice',   70, 36.7, 15, '117/75', NOW() - INTERVAL 5 DAY),
  ('alice',   73, 36.9, 16, '118/76', NOW() - INTERVAL 3 DAY),
  ('alice',   71, 36.8, 15, '116/74', NOW() - INTERVAL 1 DAY),

  ('bob',     80, 37.2, 19, '122/82', NOW() - INTERVAL 4 DAY),
  ('bob',     78, 37.0, 18, '121/80', NOW() - INTERVAL 2 DAY),

  ('charlie', 66, 36.6, 14, '115/73', NOW() - INTERVAL 6 DAY),
  ('charlie', 68, 36.7, 15, '116/74', NOW() - INTERVAL 2 DAY);

-- PRESCRIPTIONS (status defaults to PENDING)
INSERT INTO prescriptions (patient, doctor, medicine, qty, status) VALUES
  ('alice',   'dr.lee',   'Atorvastatin 10mg', 30, 'APPROVED'),
  ('bob',     'dr.lee',   'Metformin 500mg',   60, 'PENDING'),
  ('charlie', 'dr.ahmed', 'Omeprazole 20mg',   28, 'PENDING');

-- NOTES
INSERT INTO notes (patient, doctor, content) VALUES
  ('alice',   'dr.lee',   'Continue statin; lipid recheck in 3 months.'),
  ('bob',     'dr.lee',   'Monitor fasting glucose; diet & exercise.'),
  ('charlie', 'dr.ahmed', 'Trial PPI; review symptoms in 4 weeks.');

-- REFERRALS
INSERT INTO referrals (patient, doctor, hospital, procedure_desc, when_at, status) VALUES
  ('alice',   'dr.lee',   'Regional Hospital', 'Dermatology Consultation', NOW() + INTERVAL 10 DAY, 'APPROVED'),
  ('charlie', 'dr.ahmed', 'CQ Hospital',       'Endoscopy',                NOW() + INTERVAL 14 DAY, 'PENDING');
