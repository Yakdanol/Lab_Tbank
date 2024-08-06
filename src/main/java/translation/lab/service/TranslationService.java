package translation.lab.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import translation.lab.model.TranslationRequest;
import translation.lab.model.TranslationResponse;
import translation.lab.repository.TranslationRepository;
import translation.lab.entity.TranslationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class TranslationService {
    private final TranslationRepository translationRepository;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;
    private Cache<String, String> translationCache;
    @Setter
    private Map<String, String> availableLanguages;

    // Ключ API, необходимо обновлять каждые 12 часов
    private final String API_KEY = "t1.9euelZqLjMmdzciZk8-bjJLNz8_NmO3rnpWayMzOksjKyonHx8mYnpzLlonl8_cPTDxK-e9cF1cA_d3z9096OUr571wXVwD9zef1656Vmp2Nk56XzszNksbJxoyblYvL7_zF656Vmp2Nk56XzszNksbJxoyblYvL.d2RTe-UcauvwuQjTaj9DaiYWBKJJh4L7xpnl6ZcIwpVt8J3-BMjPMmjfP0QBeBua8kz2XqjmDbOZPFegHJZ7DA";

    @Autowired
    public TranslationService(TranslationRepository translationRepository, RestTemplate restTemplate) {
        this.translationRepository = translationRepository;
        this.restTemplate = restTemplate;
        this.executorService = Executors.newFixedThreadPool(10);
        this.translationCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
        loadAvailableLanguages();
    }

    // Конструктор для тестов
    public TranslationService(TranslationRepository translationRepository, RestTemplate restTemplate, Cache<String, String> translationCache, ExecutorService executorService, Map<String, String> availableLanguages) {
        this.translationRepository = translationRepository;
        this.restTemplate = restTemplate;
        this.executorService = executorService;
        this.translationCache = translationCache;
        this.availableLanguages = availableLanguages;
    }

    @PostConstruct
    public void init() {
        if (translationCache == null) {
            translationCache = Caffeine.newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)
                    .maximumSize(1000)
                    .build();
        }
        if (availableLanguages == null) {
            loadAvailableLanguages();
        }
    }

    private void loadAvailableLanguages() {
        if (availableLanguages == null) {
            availableLanguages = fetchAvailableLanguages();
        }
    }

    private Map<String, String> fetchAvailableLanguages() {
        String url = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=" + API_KEY + "&ui=en";
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                throw new TranslationException("Ошибка доступа к ресурсу перевода", null);
            }
            return (Map<String, String>) response.get("langs");
        } catch (RestClientException e) {
            log.error("Error fetching available languages from translation resource", e);
            throw new TranslationException("Ошибка доступа к ресурсу перевода", e);
        }
    }


    private void validateLanguage(String lang, boolean isSource) {
        if (!availableLanguages.containsKey(lang)) {
            String message = isSource ? "Не найден язык исходного сообщения" : "Не найден язык целевого сообщения";
            throw new IllegalArgumentException(message);
        }
    }

    public TranslationResponse translate(TranslationRequest request) {
        log.info("Starting translation process for request: {}", request);

        validateLanguage(request.getSourceLang(), true);
        validateLanguage(request.getTargetLang(), false);

        String[] words = request.getText().split("\\s+");
        Map<Integer, String> translations = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(words.length);
        AtomicReference<String> errorStatus = new AtomicReference<>("http 200");

        for (int i = 0; i < words.length; i++) {
            final int index = i;
            final String word = words[i];
            executorService.submit(() -> {
                try {
                    log.debug("Translating word: {}", word);
                    String translatedWord = translateWord(word, request.getSourceLang(), request.getTargetLang());
                    translations.put(index, translatedWord);
                    log.debug("Translated word: {} -> {}", word, translatedWord);
                } catch (TranslationException e) {
                    log.error("Error translating word: {}", word, e);
                    errorStatus.set("http 400 " + e.getMessage());
                    latch.countDown();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Translation process was interrupted", e);
            throw new TranslationException("Ошибка при ожидании завершения переводов", e);
        }

        if (errorStatus.get().startsWith("http 400")) {
            return new TranslationResponse(errorStatus.get());
        }

        StringBuilder translatedText = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            translatedText.append(translations.get(i)).append(" ");
        }

        String translatedString = translatedText.toString().trim();
        TranslationEntity translationEntity = new TranslationEntity();
        translationEntity.setIpAddress(request.getIpAddress());
        translationEntity.setOriginalText(request.getText());
        translationEntity.setTranslatedText(translatedString);
        translationRepository.save(translationEntity);

        log.info("Translation process completed successfully: {}", translatedString);
        return new TranslationResponse(errorStatus.get() + " " + translatedString);
    }

    private String translateWord(String word, String sourceLang, String targetLang) {
        String cacheKey = word + ":" + sourceLang + ":" + targetLang;
        String cachedTranslation = translationCache.getIfPresent(cacheKey);
        if (cachedTranslation != null) {
            log.debug("Cache hit for word: {}", word);
            return cachedTranslation;
        }

        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate"
                + "?key=" + API_KEY
                + "&text=" + word
                + "&lang=" + sourceLang + "-" + targetLang;
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("text")) {
                throw new TranslationException("Ошибка доступа к ресурсу перевода", null);
            }
            String translation = ((List<String>) response.get("text")).get(0);
            translationCache.put(cacheKey, translation);
            log.debug("Translated word using API: {} -> {}", word, translation);
            return translation;
        } catch (RestClientException e) {
            log.error("Error accessing translation resource for word: {}", word, e);
            throw new TranslationException("Ошибка доступа к ресурсу перевода", e);
        }
    }
}

