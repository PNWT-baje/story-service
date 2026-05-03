package ba.unsa.etf.storyservice.dto;

import ba.unsa.etf.storyservice.enums.StoryType;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoryResponse {

    private Long id;
    private Long userId;
    private StoryType type;
    private String mediaUrl;
    private String caption;
    private Boolean hasPoll;
    private String pollQuestion;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private long viewCount;
    private boolean isExpired;
}
