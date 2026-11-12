SET search_path TO main;

-- Table des musiciens de la plateforme (historique global des musiciens)
CREATE TABLE IF NOT EXISTS musicians (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    instrument VARCHAR(255) NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table de liaison entre organisations et musiciens actifs
CREATE TABLE IF NOT EXISTS organization_musicians (
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    musician_id BIGINT REFERENCES musicians(id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (organization_id, musician_id)
);

-- Table des absences pour répétitions et concerts
CREATE TABLE IF NOT EXISTS absences (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE,
    musician_id BIGINT REFERENCES musicians(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_date DATE NOT NULL,
    end_date DATE,
    reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    submitted_by_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_musicians_email ON musicians(email);
CREATE INDEX IF NOT EXISTS idx_musicians_user_id ON musicians(user_id);
CREATE INDEX IF NOT EXISTS idx_absences_org_date ON absences(organization_id, event_date);
CREATE INDEX IF NOT EXISTS idx_absences_musician ON absences(musician_id);
