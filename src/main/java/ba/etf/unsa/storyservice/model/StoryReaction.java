package ba.etf.unsa.storyservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_reactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoryReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // eksterna referenca na User Service
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String emoji;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
