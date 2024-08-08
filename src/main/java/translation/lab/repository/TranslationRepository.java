package translation.lab.repository;

import translation.lab.entity.TranslationEntity;
import org.springframework.stereotype.Repository;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.util.List;

@Repository
public class TranslationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TranslationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TranslationEntity translationEntity) {
        String query = "INSERT INTO translations (ip_address, original_text, translated_text) VALUES (?, ?, ?)";
        jdbcTemplate.update(query, translationEntity.getIpAddress(), translationEntity.getOriginalText(), translationEntity.getTranslatedText());
    }

    public List<TranslationEntity> findAll() {
        String query = "SELECT * FROM translations";
        return jdbcTemplate.query(query, new TranslationRowMapper());
    }

    public TranslationEntity findById(Long id) {
        String query = "SELECT * FROM translations WHERE id = ?";
        return jdbcTemplate.queryForObject(query, new TranslationRowMapper(), id);
    }

    private static class TranslationRowMapper implements RowMapper<TranslationEntity> {
        @Override
        public TranslationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            TranslationEntity entity = new TranslationEntity();
            entity.setId(rs.getLong("id"));
            entity.setIpAddress(rs.getString("ip_address"));
            entity.setOriginalText(rs.getString("original_text"));
            entity.setTranslatedText(rs.getString("translated_text"));
            return entity;
        }
    }
}

