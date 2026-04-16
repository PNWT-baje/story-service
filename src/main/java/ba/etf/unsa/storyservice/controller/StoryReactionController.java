package ba.etf.unsa.storyservice.controller;

import ba.etf.unsa.storyservice.model.Story;
import ba.etf.unsa.storyservice.model.StoryReaction;
import ba.etf.unsa.storyservice.repository.StoryRepository;
import ba.etf.unsa.storyservice.repository.StoryReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories/{storyId}/reactions")
@RequiredArgsConstructor
public class StoryReactionController {

    private final StoryRepository storyRepository;
    private final StoryReactionRepository reactionRepository;

    // Dodaj reakciju na story
    @PostMapping
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

    // Dohvati sve reakcije na story
    @GetMapping
    public ResponseEntity<List<StoryReaction>> getReactions(@PathVariable Long storyId) {
        return ResponseEntity.ok(reactionRepository.findByStoryId(storyId));
    }

    // Ukloni reakciju
    @DeleteMapping("/{reactionId}")
    public ResponseEntity<Void> removeReaction(@PathVariable Long storyId,
                                                @PathVariable Long reactionId) {
        if (!reactionRepository.existsById(reactionId)) return ResponseEntity.notFound().build();
        reactionRepository.deleteById(reactionId);
        return ResponseEntity.noContent().build();
    }
}
