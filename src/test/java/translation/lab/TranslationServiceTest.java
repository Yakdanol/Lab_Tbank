package translation.lab;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import translation.lab.model.TranslationRequest;
import translation.lab.model.TranslationResponse;
import translation.lab.repository.TranslationRepository;
import translation.lab.entity.TranslationEntity;
import translation.lab.service.TranslationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class TranslationServiceTest {

    @Mock
    private TranslationRepository translationRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TranslationService translationService;

    private Cache<String, String> translationCache;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        translationCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .build();
        executorService = Executors.newFixedThreadPool(10);

        Map<String, String> mockLanguages = new HashMap<>();
        mockLanguages.put("en", "English");
        mockLanguages.put("ru", "Russian");

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("langs", mockLanguages));

        translationService = new TranslationService(translationRepository, restTemplate, translationCache, executorService, mockLanguages);
    }

    private static String containsWord(String word) {
        return argThat(argument -> argument != null && argument.contains("text=" + word));
    }

    @Test
    void testTranslateRuToEn() {
        TranslationRequest request = new TranslationRequest();
        request.setIpAddress("127.0.0.1");
        request.setText("привет мир");
        request.setSourceLang("ru");
        request.setTargetLang("en");

        when(restTemplate.getForObject(containsWord("привет"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("hello")));
        when(restTemplate.getForObject(containsWord("мир"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("world")));

        TranslationResponse response = translationService.translate(request);
        System.out.println("Translated Text: " + response.getTranslatedText());

        assertEquals("http 200 hello world", response.getTranslatedText());

        verify(translationRepository, times(1)).save(any(TranslationEntity.class));
    }

    @Test
    void testTranslateEnToRu() {
        TranslationRequest request = new TranslationRequest();
        request.setIpAddress("127.0.0.1");
        request.setText("Hello world, this is my first program");
        request.setSourceLang("en");
        request.setTargetLang("ru");

        when(restTemplate.getForObject(containsWord("Hello"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("Привет")));
        when(restTemplate.getForObject(containsWord("world"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("мир")));
        when(restTemplate.getForObject(containsWord("this"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("это")));
        when(restTemplate.getForObject(containsWord("is"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("есть")));
        when(restTemplate.getForObject(containsWord("my"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("моя")));
        when(restTemplate.getForObject(containsWord("first"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("первая")));
        when(restTemplate.getForObject(containsWord("program"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("программа")));

        TranslationResponse response = translationService.translate(request);
        System.out.println("Translated Text: " + response.getTranslatedText());

        assertEquals("http 200 Привет мир это есть моя первая программа", response.getTranslatedText());

        verify(translationRepository, times(1)).save(any(TranslationEntity.class));
    }

    @Test
    void testTranslateLongSentence() {
        TranslationRequest request = new TranslationRequest();
        request.setIpAddress("127.0.0.1");
        request.setText("This is a long sentence with many words to test the translation service");
        request.setSourceLang("en");
        request.setTargetLang("ru");

        when(restTemplate.getForObject(containsWord("This"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("Это")));
        when(restTemplate.getForObject(containsWord("is"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("есть")));
        when(restTemplate.getForObject(containsWord("a"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("один")));
        when(restTemplate.getForObject(containsWord("long"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("длинное")));
        when(restTemplate.getForObject(containsWord("sentence"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("предложение")));
        when(restTemplate.getForObject(containsWord("with"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("с")));
        when(restTemplate.getForObject(containsWord("many"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("много")));
        when(restTemplate.getForObject(containsWord("words"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("слов")));
        when(restTemplate.getForObject(containsWord("to"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("для")));
        when(restTemplate.getForObject(containsWord("test"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("тестирования")));
        when(restTemplate.getForObject(containsWord("the"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("этой")));
        when(restTemplate.getForObject(containsWord("translation"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("перевод")));
        when(restTemplate.getForObject(containsWord("service"), eq(Map.class)))
                .thenReturn(Map.of("text", List.of("сервис")));

        TranslationResponse response = translationService.translate(request);
        System.out.println("Translated Text: " + response.getTranslatedText());

        assertEquals("http 200 Это есть один длинное предложение с много слов для тестирования этой перевод сервис", response.getTranslatedText());

        verify(translationRepository, times(1)).save(any(TranslationEntity.class));
    }

    @Test
    void testTranslationException() {
        TranslationRequest request = new TranslationRequest();
        request.setIpAddress("127.0.0.1");
        request.setText("Hello world");
        request.setSourceLang("en");
        request.setTargetLang("ru");

        when(restTemplate.getForObject(containsWord("Hello"), eq(Map.class)))
                .thenThrow(new RestClientException("Ошибка доступа к ресурсу перевода"));

        TranslationResponse response = translationService.translate(request);
        System.out.println("Translated Text: " + response.getTranslatedText());

        assertEquals("http 400 Ошибка доступа к ресурсу перевода", response.getTranslatedText());
    }
}
