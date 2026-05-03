package ba.unsa.etf.storyservice.dto;

import ba.unsa.etf.storyservice.enums.StoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateStoryRequest {

    @NotNull(message = "userId ne smije biti null")
    private Long userId;

    @NotNull(message = "Tip storija ne smije biti null")
    private StoryType type;

    @NotBlank(message = "Media URL ne smije biti prazan")
    private String mediaUrl;

    private String caption;
    private Boolean hasPoll = false;
    private String pollQuestion;
}
