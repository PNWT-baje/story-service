package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.model.StoryReaction;
import ba.unsa.etf.storyservice.repository.StoryReactionRepository;
import ba.unsa.etf.storyservice.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories/{storyId}/reactions")
@RequiredArgsConstructor
public class StoryReactionController {

    private final StoryRepository storyRepository;
    private final StoryReactionRepository reactionRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'CONTENT_MANAGER', 'USER')")
    public ResponseEntity<StoryReaction> addReaction(@PathVariable Long storyId,
                                                      @RequestParam Long userId,
                                                      @RequestParam String emoji) {
        Story story = storyRepository.findById(storyId).orElse(null);
        if (story == null) return ResponseEntity.notFound().build();

        StoryReaction reaction = StoryReaction.builder()
                .story(story)
                .userId(userId)
                .emoji(emoji)
                .build();
        return ResponseEntity.ok(reactionRepository.save(reaction));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StoryReaction>> getReactions(@PathVariable Long storyId) {
        return ResponseEntity.ok(reactionRepository.findByStoryId(storyId));
    }

    @DeleteMapping("/{reactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'CONTENT_MANAGER', 'USER')")
    public ResponseEntity<Void> removeReaction(@PathVariable Long storyId,
                                                @PathVariable Long reactionId) {
        if (!reactionRepository.existsById(reactionId)) return ResponseEntity.notFound().build();
        reactionRepository.deleteById(reactionId);
        return ResponseEntity.noContent().build();
    }
}
