package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.model.StoryView;
import ba.unsa.etf.storyservice.repository.StoryRepository;
import ba.unsa.etf.storyservice.repository.StoryViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;

    // Kreiranje novog storija
    @PostMapping
    public ResponseEntity<Story> createStory(@RequestBody Story story) {
        return ResponseEntity.ok(storyRepository.save(story));
    }

    // Dohvati aktivne storije korisnika
    @GetMapping("/user/{userId}")
    public List<Story> getActiveStoriesByUser(@PathVariable Long userId) {
        return storyRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
                userId, LocalDateTime.now());
    }

    // Dohvati jedan story po ID-u
    @GetMapping("/{id}")
    public ResponseEntity<Story> getStoryById(@PathVariable Long id) {
        return storyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obriši story
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        if (!storyRepository.existsById(id)) return ResponseEntity.notFound().build();
        storyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Zabilježi pregled storija
    @PostMapping("/{storyId}/view")
    public ResponseEntity<StoryView> viewStory(@PathVariable Long storyId,
                                                @RequestParam Long viewerUserId) {
        Story story = storyRepository.findById(storyId).orElse(null);
        if (story == null) return ResponseEntity.notFound().build();

        // Ne bilježi duplikat
        if (storyViewRepository.existsByStoryIdAndViewerUserId(storyId, viewerUserId)) {
            return ResponseEntity.ok().build();
        }

        StoryView view = StoryView.builder()
                .story(story)
                .viewerUserId(viewerUserId)
                .build();
        return ResponseEntity.ok(storyViewRepository.save(view));
    }

    // Ko je pregledao story (samo vlasnik može vidjeti)
    @GetMapping("/{storyId}/viewers")
    public ResponseEntity<List<StoryView>> getStoryViewers(@PathVariable Long storyId,
                                                            @RequestParam Long requesterId) {
        Story story = storyRepository.findById(storyId).orElse(null);
        if (story == null) return ResponseEntity.notFound().build();

        if (!story.getUserId().equals(requesterId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(storyViewRepository.findByStoryId(storyId));
    }

    // Broj pregleda storija
    @GetMapping("/{storyId}/views/count")
    public ResponseEntity<Long> getViewCount(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyViewRepository.countByStoryId(storyId));
    }
}
