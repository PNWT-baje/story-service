package ba.unsa.etf.storyservice.repository;

import ba.unsa.etf.storyservice.model.StoryPollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoryPollOptionRepository extends JpaRepository<StoryPollOption, Long> {
    List<StoryPollOption> findByStoryIdOrderByDisplayOrder(Long storyId);
}
