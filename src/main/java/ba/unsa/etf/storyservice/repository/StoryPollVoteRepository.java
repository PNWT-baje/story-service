package ba.unsa.etf.storyservice.repository;

import ba.unsa.etf.storyservice.model.StoryPollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryPollVoteRepository extends JpaRepository<StoryPollVote, Long> {
    boolean existsByPollOptionIdAndUserId(Long pollOptionId, Long userId);
    long countByPollOptionId(Long pollOptionId);
}
