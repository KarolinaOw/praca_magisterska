DROP TABLE IF EXISTS logo_request;
CREATE TABLE logo_request(
                             id SERIAL PRIMARY KEY,
                             file_id VARCHAR(255) NOT NULL UNIQUE,
                             params TEXT,
                             status VARCHAR(255)
);

DROP TABLE IF EXISTS logo_submission;
CREATE TABLE logo_submission(
                                id SERIAL PRIMARY KEY,
                                logo_request_id INT NOT NULL UNIQUE REFERENCES logo_request(id),
                                job_id VARCHAR(255) NOT NULL,
                                output_file_path VARCHAR(255),
                                logo_file_path VARCHAR(255)
);
