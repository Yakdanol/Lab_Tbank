package translation.lab.repository;

import translation.lab.entity.TranslationEntity;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class TranslationRepository {
    private final DataSource dataSource;

    public TranslationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void save(TranslationEntity translationEntity) {
        String query = "INSERT INTO translations (ip_address, original_text, translated_text) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, translationEntity.getIpAddress());
            preparedStatement.setString(2, translationEntity.getOriginalText());
            preparedStatement.setString(3, translationEntity.getTranslatedText());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
