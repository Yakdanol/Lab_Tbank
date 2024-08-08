package translation.lab;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import translation.lab.entity.TranslationEntity;
import translation.lab.repository.TranslationRepository;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class TranslationRepositoryTests {

    @Autowired
    private TranslationRepository translationRepository;

    @Test
    public void testSaveAndRetrieveTranslation() {
        TranslationEntity entity = new TranslationEntity();
        entity.setIpAddress("127.0.0.1");
        entity.setOriginalText("Hello world");
        entity.setTranslatedText("Привет мир");

        translationRepository.save(entity);

        List<TranslationEntity> entities = translationRepository.findAll();
        assertFalse(entities.isEmpty());
        TranslationEntity savedEntity = entities.get(0);
        assertEquals("127.0.0.1", savedEntity.getIpAddress());
        assertEquals("Hello world", savedEntity.getOriginalText());
        assertEquals("Привет мир", savedEntity.getTranslatedText());
    }
}
