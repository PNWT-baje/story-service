package ba.etf.unsa.storyservice.controller;

import ba.etf.unsa.storyservice.model.Story;
import ba.etf.unsa.storyservice.model.StoryPollOption;
import ba.etf.unsa.storyservice.model.StoryPollVote;
import ba.etf.unsa.storyservice.repository.StoryPollOptionRepository;
import ba.etf.unsa.storyservice.repository.StoryRepository;
import ba.etf.unsa.storyservice.repository.StoryPollVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories/{storyId}/poll")
@RequiredArgsConstructor
public class StoryPollController {

    private final StoryRepository storyRepository;
    private final StoryPollOptionRepository pollOptionRepository;
    private final StoryPollVoteRepository pollVoteRepository;

    // Dohvati opcije ankete
    @GetMapping("/options")
    public ResponseEntity<List<StoryPollOption>> getPollOptions(@PathVariable Long storyId) {
        return ResponseEntity.ok(pollOptionRepository.findByStoryIdOrderByDisplayOrder(storyId));
    }

    // Glasaj za opciju
    @PostMapping("/vote/{optionId}")
    public ResponseEntity<StoryPollVote> vote(@PathVariable Long storyId,
                                              @PathVariable Long optionId,
                                              @RequestParam Long userId) {
        // Provjeri da story postoji i nije istekao
        Story story = storyRepository.findById(storyId).orElse(null);
        if (story == null) return ResponseEntity.notFound().build();
        if (story.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().build();
        }

        // Provjeri duplikat glasa za ovu opciju
        if (pollVoteRepository.existsByPollOptionIdAndUserId(optionId, userId)) {
            return ResponseEntity.badRequest().build();
        }

        StoryPollOption option = pollOptionRepository.findById(optionId).orElse(null);
        if (option == null) return ResponseEntity.notFound().build();

        // Povećaj broj glasova
        option.setVoteCount(option.getVoteCount() + 1);
        pollOptionRepository.save(option);

        StoryPollVote vote = StoryPollVote.builder()
                .pollOption(option)
                .userId(userId)
                .build();
        return ResponseEntity.ok(pollVoteRepository.save(vote));
    }

    // Broj glasova po opciji
    @GetMapping("/options/{optionId}/votes")
    public ResponseEntity<Long> getVoteCount(@PathVariable Long storyId,
                                              @PathVariable Long optionId) {
        return ResponseEntity.ok(pollVoteRepository.countByPollOptionId(optionId));
    }
}
