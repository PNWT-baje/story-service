package ba.unsa.etf.storyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateStoryReactionRequest {

    @NotNull(message = "userId ne smije biti null")
    private Long userId;

    @NotBlank(message = "Emoji ne smije biti prazan")
    private String emoji;
}
