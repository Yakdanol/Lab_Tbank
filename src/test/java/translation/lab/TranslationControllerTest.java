package translation.lab;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import translation.lab.model.TranslationRequest;
import translation.lab.model.TranslationResponse;
import translation.lab.service.TranslationException;
import translation.lab.service.TranslationService;
import translation.lab.controller.TranslationController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(TranslationController.class)
class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @Test
    void testTranslateRuToEn() throws Exception {
        TranslationResponse response = new TranslationResponse("hello world");
        Mockito.when(translationService.translate(any(TranslationRequest.class))).thenReturn(response);

        String jsonRequest = "{\"ipAddress\":\"127.0.0.1\", \"text\":\"привет мир\", \"sourceLang\":\"ru\", \"targetLang\":\"en\"}";

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("http 200 hello world"));
    }

    @Test
    void testTranslateEnToRu() throws Exception {
        TranslationResponse response = new TranslationResponse("Привет мир это есть моя первая программа");
        Mockito.when(translationService.translate(any(TranslationRequest.class))).thenReturn(response);

        String jsonRequest = "{\"ipAddress\":\"127.0.0.1\", \"text\":\"Hello world, this is my first program\", \"sourceLang\":\"en\", \"targetLang\":\"ru\"}";

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("http 200 Привет мир это есть моя первая программа"));
    }

    @Test
    void testTranslateLongSentence() throws Exception {
        TranslationResponse response = new TranslationResponse("Это есть один длинное предложение с много слов для тестирования этой перевод сервис");
        Mockito.when(translationService.translate(any(TranslationRequest.class))).thenReturn(response);

        String jsonRequest = "{\"ipAddress\":\"127.0.0.1\", \"text\":\"This is a long sentence with many words to test the translation service\", \"sourceLang\":\"en\", \"targetLang\":\"ru\"}";

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("http 200 Это есть один длинное предложение с много слов для тестирования этой перевод сервис"));
    }

    @Test
    void testMissingSourceLang() throws Exception {
        String jsonRequest = "{\"ipAddress\":\"127.0.0.1\", \"text\":\"Hello world\", \"sourceLang\":\"eeeen\", \"targetLang\":\"ru\"}";

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("http 400 Не найден язык исходного сообщения"));
    }

    @Test
    void testTranslationException() throws Exception {
        Mockito.when(translationService.translate(any(TranslationRequest.class))).thenThrow(new TranslationException("Ошибка доступа к ресурсу перевода", null));

        String jsonRequest = "{\"ipAddress\":\"127.0.0.1\", \"text\":\"Hello world\", \"sourceLang\":\"en\", \"targetLang\":\"ru\"}";

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("http 400 Ошибка доступа к ресурсу перевода"));
    }
}
