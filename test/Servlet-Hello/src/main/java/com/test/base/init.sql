\c sprint5;

DROP TABLE IF EXISTS utilisateurs;

CREATE TABLE utilisateurs (
    id SERIAL PRIMARY KEY,                  -- Identifiant unique (auto-incrémenté)
    nom VARCHAR(100) NOT NULL,              -- Nom de l'utilisateur
    email VARCHAR(150) UNIQUE NOT NULL,     -- Email unique
    role VARCHAR(50) DEFAULT 'membre',      -- Rôle (ex: admin, membre)
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Date automatique
);

-- 4. Insertion de données de test
INSERT INTO utilisateurs (nom, email, role) VALUES
('Alice Martin', 'alice.martin@email.com', 'admin'),
('Bob Durand', 'bob.durand@email.com', 'membre'),
('Charlie Dubois', 'charlie.dubois@email.com', 'membre');
