package ba.unsa.etf.storyservice.model;

import ba.unsa.etf.storyservice.enums.StoryType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // eksterna referenca na User Service — ne @ManyToOne
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoryType type;

    @Column(nullable = false, length = 500)
    private String mediaUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(nullable = false)
    private Boolean hasPoll = false;

    @Column(length = 200)
    private String pollQuestion;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relacije (lokalne)
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL)
    private List<StoryView> views;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL)
    private List<StoryReaction> reactions;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL)
    private List<StoryPollOption> pollOptions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = createdAt.plusHours(24);
        }
    }
}
