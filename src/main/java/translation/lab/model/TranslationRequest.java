package translation.lab.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslationRequest {
    @NotNull
    private String ipAddress;

    @NotNull
    private String text;

    @NotNull
    private String sourceLang;

    @NotNull
    private String targetLang;
}
