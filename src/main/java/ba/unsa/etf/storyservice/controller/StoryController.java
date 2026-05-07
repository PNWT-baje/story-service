package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.dto.CreateStoryRequest;
import ba.unsa.etf.storyservice.dto.StoryPatchDto;
import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.model.StoryView;
import ba.unsa.etf.storyservice.service.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

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

    // Pageable — Task 4: paginacija i sortiranje
    @GetMapping("/user/{userId}/paged")
    public Page<Story> getStoriesByUserPaged(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return storyService.getActiveStoriesByUserPaged(userId, pageable);
    }

    // Custom @Query endpoint — Task 4
    @GetMapping("/trending")
    public List<Story> getTrendingStories(@RequestParam(defaultValue = "1") int minViews) {
        return storyService.getActiveStoriesWithMinViews(minViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Story> getStoryById(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getStoryById(id));
    }

    // PATCH — Task 4: parcijalni update
    @PatchMapping("/{id}")
    public ResponseEntity<Story> patchStory(@PathVariable Long id,
                                             @RequestBody StoryPatchDto patch) {
        return ResponseEntity.ok(storyService.patchStory(id, patch));
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

    // Multi-repo transakcija — Task 4: view + reakcija u jednoj transakciji
    @PostMapping("/{storyId}/view-and-react")
    public ResponseEntity<StoryView> viewAndReact(@PathVariable Long storyId,
                                                   @RequestParam Long viewerUserId,
                                                   @RequestParam(required = false) String emoji) {
        return ResponseEntity.ok(storyService.viewAndReact(storyId, viewerUserId, emoji));
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
