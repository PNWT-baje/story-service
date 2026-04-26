package ba.unsa.etf.storyservice.repository;

import ba.unsa.etf.storyservice.model.StoryReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoryReactionRepository extends JpaRepository<StoryReaction, Long> {
    List<StoryReaction> findByStoryId(Long storyId);
    List<StoryReaction> findByStoryIdAndUserId(Long storyId, Long userId);
}
