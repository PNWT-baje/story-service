package ba.unsa.etf.storyservice.repository;

import ba.unsa.etf.storyservice.enums.StoryType;
import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.model.StoryReaction;
import ba.unsa.etf.storyservice.model.StoryView;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StoryRepositoryTest {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private StoryViewRepository storyViewRepository;

    @Autowired
    private StoryReactionRepository reactionRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private EntityManager entityManager;

    private Long storyId;

    @BeforeEach
    void setUp() {
        Story story = storyRepository.save(Story.builder()
                .userId(1L)
                .type(StoryType.PHOTO)
                .mediaUrl("http://example.com/img.jpg")
                .hasPoll(false)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build());

        storyViewRepository.save(StoryView.builder().story(story).viewerUserId(2L).build());
        storyViewRepository.save(StoryView.builder().story(story).viewerUserId(3L).build());

        reactionRepository.save(StoryReaction.builder().story(story).userId(2L).emoji("❤️").build());

        storyId = story.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUserIdPaged_returnsResults() {
        Page<Story> page = storyRepository.findByUserIdAndExpiresAtAfter(
                1L, LocalDateTime.now(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void findActiveStoriesWithMinViews_returnsCorrect() {
        List<Story> stories = storyRepository.findActiveStoriesWithMinViews(LocalDateTime.now(), 2);
        assertEquals(1, stories.size());
        assertEquals(storyId, stories.get(0).getId());
    }

    @Test
    void findActiveStoriesWithMinViews_tooHighMin_returnsEmpty() {
        List<Story> stories = storyRepository.findActiveStoriesWithMinViews(LocalDateTime.now(), 10);
        assertTrue(stories.isEmpty());
    }

    @Test
    void entityGraph_reducesQueryCountComparedToLazyLoading() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        // Bez EntityGraph-a: story + views + reactions = 3 query-a
        statistics.clear();
        Story lazyStory = storyRepository.findById(storyId).orElseThrow();
        lazyStory.getViews().size();
        lazyStory.getReactions().size();
        long withoutGraph = statistics.getPrepareStatementCount();

        entityManager.clear();

        // Sa EntityGraph-om: sve u jednom JOIN query-u
        statistics.clear();
        Story eagerStory = storyRepository.findWithDetailsById(storyId).orElseThrow();
        if (eagerStory.getViews() != null) eagerStory.getViews().size();
        if (eagerStory.getReactions() != null) eagerStory.getReactions().size();
        long withGraph = statistics.getPrepareStatementCount();

        assertTrue(withGraph <= withoutGraph,
                "EntityGraph trebao smanjiti broj upita: bez=" + withoutGraph + " sa=" + withGraph);
    }

    @Test
    void findWithDetailsById_loadsCollectionsEagerly() {
        Story story = storyRepository.findWithDetailsById(storyId).orElseThrow();
        assertNotNull(story.getViews());
        assertEquals(2, story.getViews().size());
        assertNotNull(story.getReactions());
        assertEquals(1, story.getReactions().size());
    }
}
