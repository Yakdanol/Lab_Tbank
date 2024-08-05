package translation.lab.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import translation.lab.model.TranslationRequest;
import translation.lab.model.TranslationResponse;
import translation.lab.repository.TranslationRepository;
import translation.lab.entity.TranslationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class TranslationService {
    private final TranslationRepository translationRepository;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    private Cache<String, String> translationCache;

    @Autowired
    public TranslationService(TranslationRepository translationRepository, RestTemplate restTemplate) {
        this.translationRepository = translationRepository;
        this.restTemplate = restTemplate;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    // Конструктор для тестов
    public TranslationService(TranslationRepository translationRepository, RestTemplate restTemplate, Cache<String, String> translationCache, ExecutorService executorService) {
        this.translationRepository = translationRepository;
        this.restTemplate = restTemplate;
        this.executorService = executorService; // Инициализация пула из 10 потоков
        this.translationCache = translationCache;
    }

    @PostConstruct
    public void init() {
        translationCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
    }

    public TranslationResponse translate(TranslationRequest request) {
        String[] words = request.getText().split("\\s+");
        Map<Integer, String> translations = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(words.length); // Инициализация CountDownLatch с количеством слов

        for (int i = 0; i < words.length; i++) {
            final int index = i;
            final String word = words[i];
            executorService.submit(() -> {
                try {
                    String translatedWord = translateWord(word, request.getSourceLang(), request.getTargetLang());
                    translations.put(index, translatedWord); // Добавление перевода слова в ConcurrentHashMap
                } finally {
                    latch.countDown(); // Уменьшение счётчика CountDownLatch
                }
            });
        }

        try {
            latch.await(); // Ожидание завершения всех задач
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        StringBuilder translatedText = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            translatedText.append(translations.get(i)).append(" "); // Сбор результатов в правильном порядке
        }

        String translatedString = translatedText.toString().trim();
        TranslationEntity translationEntity = new TranslationEntity();
        translationEntity.setIpAddress(request.getIpAddress());
        translationEntity.setOriginalText(request.getText());
        translationEntity.setTranslatedText(translatedString);
        translationRepository.save(translationEntity);

        return new TranslationResponse(translatedString);
    }

    private String translateWord(String word, String sourceLang, String targetLang) {
        String cacheKey = word + ":" + sourceLang + ":" + targetLang;
        String cachedTranslation = translationCache.getIfPresent(cacheKey);
        if (cachedTranslation != null) {
            return cachedTranslation;
        }

        String API_KEY = "t1.9euelZqLjMmdzciZk8-bjJLNz8_NmO3rnpWayMzOksjKyonHx8mYnpzLlonl8_cPTDxK-e9cF1cA_d3z9096OUr571wXVwD9zef1656Vmp2Nk56XzszNksbJxoyblYvL7_zF656Vmp2Nk56XzszNksbJxoyblYvL.d2RTe-UcauvwuQjTaj9DaiYWBKJJh4L7xpnl6ZcIwpVt8J3-BMjPMmjfP0QBeBua8kz2XqjmDbOZPFegHJZ7DA";
        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate"
                + "?key=" + API_KEY
                + "&text=" + word
                + "&lang=" + sourceLang + "-" + targetLang;
        String translation = restTemplate.getForObject(url, String.class);
        translationCache.put(cacheKey, translation);
        return translation;
    }
}