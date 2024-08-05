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
                .andExpect(content().json("{\"translatedText\":\"hello world\"}"));
    }

    @Test
    void testTranslateEnToRu() throws Exception {
        TranslationResponse response = new TranslationResponse("Привет мир это моя первая программа");
        Mockito.when(translationService.translate(any(TranslationRequest.class))).thenReturn(response);

        String jsonRequest = "{\"ipAddress\":\"127.0.0.1\", \"text\":\"Hello world, this is my first program\", \"sourceLang\":\"en\", \"targetLang\":\"ru\"}";

        mockMvc.perform(post("/api/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"translatedText\":\"Привет мир это моя первая программа\"}"));
    }
}