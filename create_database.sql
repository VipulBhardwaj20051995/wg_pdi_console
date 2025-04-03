-- Create the database
CREATE DATABASE IF NOT EXISTS wovengold_pdi;

-- Use the database
USE wovengold_pdi;

-- Create the main PDI submissions table
CREATE TABLE IF NOT EXISTS pdi_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    video_url VARCHAR(255),
    submission_date DATETIME NOT NULL,
    email_sent BOOLEAN DEFAULT FALSE,
    sms_sent BOOLEAN DEFAULT FALSE,
    tub_serial_no VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create the PDI images table with proper foreign key
CREATE TABLE IF NOT EXISTS pdi_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pdi_submission_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pdi_submission_id) REFERENCES pdi_submissions(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_customer_email ON pdi_submissions(customer_email);
CREATE INDEX IF NOT EXISTS idx_customer_phone ON pdi_submissions(customer_phone);
CREATE INDEX IF NOT EXISTS idx_submission_date ON pdi_submissions(submission_date);
CREATE INDEX IF NOT EXISTS idx_pdi_submission_id ON pdi_images(pdi_submission_id);

-- Create a view for easy access to submissions with their images
CREATE OR REPLACE VIEW pdi_submissions_with_images AS
SELECT 
    s.*,
    GROUP_CONCAT(i.image_url) as image_urls
FROM 
    pdi_submissions s
LEFT JOIN 
    pdi_images i ON s.id = i.pdi_submission_id
GROUP BY 
    s.id; 