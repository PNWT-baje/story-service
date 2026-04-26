package ba.unsa.etf.storyservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_views",
       uniqueConstraints = @UniqueConstraint(columnNames = {"story_id", "viewer_user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoryView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // eksterna referenca na User Service
    @Column(nullable = false)
    private Long viewerUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}
