-- seed_fixed.sql
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

-- APPOINTMENTS  (schema uses date_time)
INSERT INTO appointments (patient, doctor, location, status, date_time) VALUES
  ('alice',   'dr.lee',   'Clinic A', 'COMPLETED', NOW() - INTERVAL 3 DAY),
  ('alice',   'dr.lee',   'Clinic A', 'PENDING',   NOW() + INTERVAL 2 DAY),
  ('bob',     'dr.lee',   'Clinic B', 'PENDING',   NOW() + INTERVAL 5 DAY),
  ('charlie', 'dr.ahmed', 'Online',   'PENDING',   NOW() + INTERVAL 1 DAY);

-- VITALS  (schema uses temperature, respiration)
INSERT INTO vitals (patient, pulse, temperature, respiration, bp, recorded_at) VALUES
  ('alice',   70, 36.7, 15, '117/75', NOW() - INTERVAL 5 DAY),
  ('alice',   73, 36.9, 16, '118/76', NOW() - INTERVAL 3 DAY),
  ('alice',   71, 36.8, 15, '116/74', NOW() - INTERVAL 1 DAY),

  ('bob',     80, 37.2, 19, '122/82', NOW() - INTERVAL 4 DAY),
  ('bob',     78, 37.0, 18, '121/80', NOW() - INTERVAL 2 DAY),

  ('charlie', 66, 36.6, 14, '115/73', NOW() - INTERVAL 6 DAY),
  ('charlie', 68, 36.7, 15, '116/74', NOW() - INTERVAL 2 DAY);

-- PRESCRIPTIONS
INSERT INTO prescriptions (patient, doctor, medicine, qty, status) VALUES
  ('alice',   'dr.lee',   'Atorvastatin 10mg', 30, 'APPROVED'),
  ('bob',     'dr.lee',   'Metformin 500mg',   60, 'PENDING'),
  ('charlie', 'dr.ahmed', 'Omeprazole 20mg',   28, 'PENDING');

-- NOTES
INSERT INTO notes (patient, doctor, content) VALUES
  ('alice',   'dr.lee',   'Continue statin; lipid recheck in 3 months.'),
  ('bob',     'dr.lee',   'Monitor fasting glucose; diet & exercise.'),
  ('charlie', 'dr.ahmed', 'Trial PPI; review symptoms in 4 weeks.');

-- REFERRALS (schema uses when_at/status as VARCHAR)
INSERT INTO referrals (patient, doctor, hospital, procedure_desc, when_at, status) VALUES
  ('alice',   'dr.lee',   'Regional Hospital', 'Dermatology Consultation', NOW() + INTERVAL 10 DAY, 'APPROVED'),
  ('charlie', 'dr.ahmed', 'CQ Hospital',       'Endoscopy',                NOW() + INTERVAL 14 DAY, 'PENDING');
