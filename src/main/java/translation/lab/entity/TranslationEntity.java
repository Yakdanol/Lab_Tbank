package translation.lab.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslationEntity {
    private Long id;
    private String ipAddress;
    private String originalText;
    private String translatedText;
}
