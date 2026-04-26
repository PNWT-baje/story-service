package ba.unsa.etf.storyservice.repository;

import ba.unsa.etf.storyservice.model.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, Long> {
    List<StoryView> findByStoryId(Long storyId);
    boolean existsByStoryIdAndViewerUserId(Long storyId, Long viewerUserId);
    long countByStoryId(Long storyId);
}
