-- THS schema (idempotent)
CREATE DATABASE IF NOT EXISTS ths_enhanced CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ths;

-- Simple users table for demo login
CREATE TABLE IF NOT EXISTS users (
  username      VARCHAR(50) PRIMARY KEY,
  pass_hash     VARCHAR(255) NOT NULL,
  role          ENUM('patient','doctor','admin') NOT NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Appointments
CREATE TABLE IF NOT EXISTS appointments (
  id           INT PRIMARY KEY AUTO_INCREMENT,
  patient      VARCHAR(50),
  doctor       VARCHAR(64),
  location     VARCHAR(64),
  status       VARCHAR(20) DEFAULT 'PENDING',
  date_time    TIMESTAMP NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX(patient), INDEX(doctor)
);

-- Prescriptions
CREATE TABLE IF NOT EXISTS prescriptions (
  id           INT PRIMARY KEY AUTO_INCREMENT,
  patient      VARCHAR(50),
  doctor       VARCHAR(64) NOT NULL,
  medicine     VARCHAR(50),
  qty          INT NOT NULL,
  status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX(patient), INDEX(doctor)
);

-- Notes
CREATE TABLE IF NOT EXISTS notes (
  id           INT PRIMARY KEY AUTO_INCREMENT,
  patient      VARCHAR(50) NOT NULL,
  doctor       VARCHAR(64) NOT NULL,
  content      TEXT NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX(patient), INDEX(doctor)
);

-- Vitals
CREATE TABLE IF NOT EXISTS vitals (
  id           INT PRIMARY KEY AUTO_INCREMENT,
  patient      VARCHAR(50),
  pulse        INT,
  temperature  FLOAT,
  respiration  INT,
  bp           VARCHAR(20),
  recorded_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX(patient)
);

-- Referrals
CREATE TABLE IF NOT EXISTS referrals (
  id             INT PRIMARY KEY AUTO_INCREMENT,
  patient        VARCHAR(50) NOT NULL,
  doctor         VARCHAR(64) NOT NULL,
  hospital       VARCHAR(50) NOT NULL,
  procedure_desc VARCHAR(100) NOT NULL,
  when_at        TIMESTAMP NOT NULL,
  status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX(patient), INDEX(doctor)
);
