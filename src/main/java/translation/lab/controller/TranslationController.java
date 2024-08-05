package translation.lab.controller;

import translation.lab.model.TranslationRequest;
import translation.lab.model.TranslationResponse;
import translation.lab.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public TranslationResponse translate(@RequestBody TranslationRequest request) {
        return translationService.translate(request);
    }
}