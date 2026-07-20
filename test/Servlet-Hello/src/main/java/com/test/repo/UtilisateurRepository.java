package com.test.repo; // Ajusté pour correspondre à ton package com.test.repo

import com.test.model.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UtilisateurRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Équivalent du "save" de JpaRepository
    public int save(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateurs (nom, email, role) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, utilisateur.getNom(), utilisateur.getEmail(), utilisateur.getRole());
    }

    // Équivalent du "findAll" de JpaRepository
    public List<Utilisateur> findAll() {
        String sql = "SELECT * FROM utilisateurs";
        return jdbcTemplate.query(sql, new UtilisateurRowMapper());
    }

    // Équivalent du "findByEmail" de JpaRepository
    public Utilisateur findByEmail(String email) {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new UtilisateurRowMapper(), email);
    }

    // Le RowMapper convertit chaque ligne SQL en objet Utilisateur
    private static final class UtilisateurRowMapper implements RowMapper<Utilisateur> {
        @Override
        public Utilisateur mapRow(ResultSet rs, int rowNum) throws SQLException {
            Utilisateur u = new Utilisateur();
            u.setId(rs.getLong("id"));
            u.setNom(rs.getString("nom"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));
            if (rs.getTimestamp("date_inscription") != null) {
                u.setDateInscription(rs.getTimestamp("date_inscription").toLocalDateTime());
            }
            return u;
        }
    }
}