package ba.unsa.etf.storyservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "story_poll_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoryPollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(nullable = false, length = 200)
    private String optionText;

    @Column(nullable = false)
    private Integer voteCount = 0;

    @Column(nullable = false)
    private Integer displayOrder;

    @OneToMany(mappedBy = "pollOption", cascade = CascadeType.ALL)
    private List<StoryPollVote> votes;
}
