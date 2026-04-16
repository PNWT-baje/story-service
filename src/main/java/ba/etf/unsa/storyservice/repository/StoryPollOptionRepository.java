package ba.etf.unsa.storyservice.repository;

import ba.etf.unsa.storyservice.model.StoryPollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoryPollOptionRepository extends JpaRepository<StoryPollOption, Long> {
    List<StoryPollOption> findByStoryIdOrderByDisplayOrder(Long storyId);
}
