CREATE SCHEMA IF NOT EXISTS main;
SET search_path TO main;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    main_instrument VARCHAR(255),
    musical_description TEXT,
    years_of_practice INT,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_secondary_instruments (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    instrument VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, instrument)
);

CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    formation_type VARCHAR(255),
    city VARCHAR(255),
    rehearsal_address VARCHAR(255),
    rehearsal_schedule VARCHAR(255),
    logistics_info TEXT,
    concert_dress TEXT,
    description TEXT,
    musician_count INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS organization_admins (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, organization_id)
);

CREATE TABLE IF NOT EXISTS rehearsal_schedules (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    day_of_week VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS concerts (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    event_date TIMESTAMP,
    location VARCHAR(255),
    description TEXT
);

CREATE TABLE IF NOT EXISTS annual_needs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    instrument VARCHAR(255) NOT NULL,
    required_count INT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS rehearsal_manuals (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    manual_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    location VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS reinforcement_requests (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(255),
    instrument_needed VARCHAR(255) NOT NULL,
    required_quantity INT NOT NULL,
    event_date TIMESTAMP,
    event_location VARCHAR(255),
    repertoire_details TEXT,
    description TEXT,
    deadline TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS applications (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT REFERENCES reinforcement_requests(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipment_requests (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    equipment_name VARCHAR(255) NOT NULL,
    duration VARCHAR(255),
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipment_request_dates (
    request_id BIGINT REFERENCES equipment_requests(id) ON DELETE CASCADE,
    event_date DATE NOT NULL,
    PRIMARY KEY (request_id, event_date)
);