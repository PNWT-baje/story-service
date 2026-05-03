package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.dto.CreateStoryRequest;
import ba.unsa.etf.storyservice.enums.StoryType;
import ba.unsa.etf.storyservice.model.*;
import ba.unsa.etf.storyservice.repository.StoryViewRepository;
import ba.unsa.etf.storyservice.service.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final StoryViewRepository storyViewRepository;

    @PostMapping
    public ResponseEntity<Story> createStory(@Valid @RequestBody CreateStoryRequest request) {
        Story story = Story.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .mediaUrl(request.getMediaUrl())
                .caption(request.getCaption())
                .hasPoll(request.getHasPoll() != null ? request.getHasPoll() : false)
                .pollQuestion(request.getPollQuestion())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(storyService.createStory(story));
    }

    @GetMapping("/user/{userId}")
    public List<Story> getActiveStoriesByUser(@PathVariable Long userId) {
        return storyService.getActiveStoriesByUser(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Story> getStoryById(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getStoryById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{storyId}/view")
    public ResponseEntity<StoryView> viewStory(@PathVariable Long storyId,
                                               @RequestParam Long viewerUserId) {
        StoryView view = storyService.viewStory(storyId, viewerUserId);
        return ResponseEntity.ok(view);
    }

    @GetMapping("/{storyId}/viewers")
    public ResponseEntity<List<StoryView>> getStoryViewers(@PathVariable Long storyId,
                                                           @RequestParam Long requesterId) {
        return ResponseEntity.ok(storyService.getViewers(storyId, requesterId));
    }

    @GetMapping("/{storyId}/views/count")
    public ResponseEntity<Long> getViewCount(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getViewCount(storyId));
    }
}