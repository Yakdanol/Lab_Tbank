package translation.lab.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import translation.lab.model.TranslationRequest;
import translation.lab.model.TranslationResponse;
import translation.lab.service.TranslationException;
import translation.lab.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public ResponseEntity<String> translate(@Valid @RequestBody TranslationRequest request) {
        log.info("Received translation request: {}", request);

        if (request.getSourceLang() == null || request.getTargetLang() == null) {
            log.error("Source or target language is null");
            throw new IllegalArgumentException("Не найден язык исходного или целевого сообщения");
        }

        TranslationResponse response = translationService.translate(request);
        if (response == null) {
            log.error("Translation service returned null response");
            throw new TranslationException("Ошибка доступа к ресурсу перевода", null);
        }

        String responseBody = "http 200 " + response.getTranslatedText();
        log.info("Translation successful: {}", responseBody);
        return ResponseEntity.ok(responseBody);
    }
}