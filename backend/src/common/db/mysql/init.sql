
CREATE TABLE experiment_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    var_threshold FLOAT NOT NULL,
    learning_rate FLOAT NOT NULL,
    iterations INT NOT NULL,
    learning_path TEXT NOT NULL,
    test_image_path TEXT NOT NULL,
    roi_path TEXT NOT NULL,
    project_id VARCHAR(250) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME
);


CREATE TABLE cctv_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    experiment_session_id BIGINT NOT NULL,
    cctv_id VARCHAR(50) NOT NULL,
    learning_data_size INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    
    FOREIGN KEY (experiment_session_id) REFERENCES experiment_sessions(id)
        ON DELETE CASCADE
);

CREATE TABLE roi_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cctv_result_id BIGINT NOT NULL,
    roi_id INT NOT NULL,
    rate FLOAT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    
    FOREIGN KEY (cctv_result_id) REFERENCES cctv_results(id)
        ON DELETE CASCADE
);


CREATE INDEX idx_session_time ON experiment_sessions(created_at);
CREATE INDEX idx_cctv ON cctv_results(cctv_id);
CREATE INDEX idx_roi ON roi_results(cctv_result_id, roi_id);