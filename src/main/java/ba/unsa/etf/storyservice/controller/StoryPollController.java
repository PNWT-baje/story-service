package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.dto.BatchVoteRequest;
import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.model.StoryPollOption;
import ba.unsa.etf.storyservice.model.StoryPollVote;
import ba.unsa.etf.storyservice.repository.StoryPollOptionRepository;
import ba.unsa.etf.storyservice.repository.StoryPollVoteRepository;
import ba.unsa.etf.storyservice.repository.StoryRepository;
import ba.unsa.etf.storyservice.service.StoryService;
import jakarta.validation.Valid;
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
    private final StoryService storyService;

    @GetMapping("/options")
    public ResponseEntity<List<StoryPollOption>> getPollOptions(@PathVariable Long storyId) {
        return ResponseEntity.ok(pollOptionRepository.findByStoryIdOrderByDisplayOrder(storyId));
    }

    @PostMapping("/vote/{optionId}")
    public ResponseEntity<StoryPollVote> vote(@PathVariable Long storyId,
                                               @PathVariable Long optionId,
                                               @RequestParam Long userId) {
        Story story = storyRepository.findById(storyId).orElse(null);
        if (story == null) return ResponseEntity.notFound().build();
        if (story.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest().build();
        }
        if (pollVoteRepository.existsByPollOptionIdAndUserId(optionId, userId)) {
            return ResponseEntity.badRequest().build();
        }
        StoryPollOption option = pollOptionRepository.findById(optionId).orElse(null);
        if (option == null) return ResponseEntity.notFound().build();
        option.setVoteCount(option.getVoteCount() + 1);
        pollOptionRepository.save(option);
        StoryPollVote vote = StoryPollVote.builder().pollOption(option).userId(userId).build();
        return ResponseEntity.ok(pollVoteRepository.save(vote));
    }

    @GetMapping("/options/{optionId}/votes")
    public ResponseEntity<Long> getVoteCount(@PathVariable Long storyId,
                                              @PathVariable Long optionId) {
        return ResponseEntity.ok(pollVoteRepository.countByPollOptionId(optionId));
    }

    // Batch vote — Task 4: batch unos
    @PostMapping("/votes/batch")
    public ResponseEntity<List<StoryPollVote>> batchVote(@PathVariable Long storyId,
                                                          @Valid @RequestBody BatchVoteRequest request) {
        return ResponseEntity.ok(storyService.batchVote(storyId, request.getVotes()));
    }
}
