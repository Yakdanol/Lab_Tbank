package translation.lab;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;
import translation.lab.model.TranslationRequest;
import translation.lab.model.TranslationResponse;
import translation.lab.repository.TranslationRepository;
import translation.lab.entity.TranslationEntity;
import translation.lab.service.TranslationService;

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
        translationService = new TranslationService(translationRepository, restTemplate, translationCache, executorService);
    }

    private static String containsWord(String word) {
        return argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                return argument != null && argument.contains("text=" + word);
            }
        });
    }

    @Test
    void testTranslateRuToEn() {
        TranslationRequest request = new TranslationRequest();
        request.setIpAddress("127.0.0.1");
        request.setText("привет мир");
        request.setSourceLang("ru");
        request.setTargetLang("en");

        when(restTemplate.getForObject(containsWord("привет"), eq(String.class))).thenReturn("hello");
        when(restTemplate.getForObject(containsWord("мир"), eq(String.class))).thenReturn("world");

        TranslationResponse response = translationService.translate(request);
        System.out.println("Translated Text: " + response.getTranslatedText() + "\n");
        assertEquals("hello world", response.getTranslatedText());
        verify(translationRepository, times(1)).save(any(TranslationEntity.class));
    }

    @Test
    void testTranslateEnToRu() {
        TranslationRequest request = new TranslationRequest();
        request.setIpAddress("127.0.0.1");
        request.setText("Hello world, this is my first program");
        request.setSourceLang("en");
        request.setTargetLang("ru");

        when(restTemplate.getForObject(containsWord("Hello"), eq(String.class))).thenReturn("Привет");
        when(restTemplate.getForObject(containsWord("world"), eq(String.class))).thenReturn("мир");
        when(restTemplate.getForObject(containsWord("this"), eq(String.class))).thenReturn("это");
        when(restTemplate.getForObject(containsWord("is"), eq(String.class))).thenReturn("есть");
        when(restTemplate.getForObject(containsWord("my"), eq(String.class))).thenReturn("моя");
        when(restTemplate.getForObject(containsWord("first"), eq(String.class))).thenReturn("первая");
        when(restTemplate.getForObject(containsWord("program"), eq(String.class))).thenReturn("программа");

        TranslationResponse response = translationService.translate(request);
        System.out.println("Translated Text: " + response.getTranslatedText() + "\n");
        assertEquals("Привет мир это есть моя первая программа", response.getTranslatedText());
        verify(translationRepository, times(1)).save(any(TranslationEntity.class));
    }

    @Test
    void testTranslateLongSentence() {
        TranslationRequest request = new TranslationRequest();
        request.setIpAddress("127.0.0.1");
        request.setText("This is a long sentence with many words to test the translation service");
        request.setSourceLang("en");
        request.setTargetLang("ru");

        when(restTemplate.getForObject(containsWord("This"), eq(String.class))).thenReturn("Это");
        when(restTemplate.getForObject(containsWord("is"), eq(String.class))).thenReturn("есть");
        when(restTemplate.getForObject(containsWord("a"), eq(String.class))).thenReturn("один");
        when(restTemplate.getForObject(containsWord("long"), eq(String.class))).thenReturn("длинное");
        when(restTemplate.getForObject(containsWord("sentence"), eq(String.class))).thenReturn("предложение");
        when(restTemplate.getForObject(containsWord("with"), eq(String.class))).thenReturn("с");
        when(restTemplate.getForObject(containsWord("many"), eq(String.class))).thenReturn("много");
        when(restTemplate.getForObject(containsWord("words"), eq(String.class))).thenReturn("слов");
        when(restTemplate.getForObject(containsWord("to"), eq(String.class))).thenReturn("для");
        when(restTemplate.getForObject(containsWord("test"), eq(String.class))).thenReturn("тестирования");
        when(restTemplate.getForObject(containsWord("the"), eq(String.class))).thenReturn("этой");
        when(restTemplate.getForObject(containsWord("translation"), eq(String.class))).thenReturn("перевод");
        when(restTemplate.getForObject(containsWord("service"), eq(String.class))).thenReturn("сервис");

        TranslationResponse response = translationService.translate(request);
        System.out.println("Translated Text: " + response.getTranslatedText() + "\n");
        assertEquals("Это есть один длинное предложение с много слов для тестирования этой перевод сервис", response.getTranslatedText());
        verify(translationRepository, times(1)).save(any(TranslationEntity.class));
    }
}