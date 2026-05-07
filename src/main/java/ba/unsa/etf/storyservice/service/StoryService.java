package ba.unsa.etf.storyservice.service;

import ba.unsa.etf.storyservice.dto.BatchVoteRequest;
import ba.unsa.etf.storyservice.dto.StoryPatchDto;
import ba.unsa.etf.storyservice.exception.ResourceNotFoundException;
import ba.unsa.etf.storyservice.exception.StoryExpiredException;
import ba.unsa.etf.storyservice.model.*;
import ba.unsa.etf.storyservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final StoryReactionRepository reactionRepository;
    private final StoryPollOptionRepository pollOptionRepository;
    private final StoryPollVoteRepository pollVoteRepository;

    public Story createStory(Story story) {
        return storyRepository.save(story);
    }

    public Story getStoryById(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story sa ID " + id + " nije pronađen"));
    }

    public List<Story> getActiveStoriesByUser(Long userId) {
        return storyRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
                userId, LocalDateTime.now());
    }

    // Pageable — Task 4: paginacija i sortiranje
    public Page<Story> getActiveStoriesByUserPaged(Long userId, Pageable pageable) {
        return storyRepository.findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now(), pageable);
    }

    // Custom @Query — Task 4
    public List<Story> getActiveStoriesWithMinViews(int minViews) {
        return storyRepository.findActiveStoriesWithMinViews(LocalDateTime.now(), minViews);
    }

    // PATCH — Task 4: parcijalni update
    public Story patchStory(Long id, StoryPatchDto patch) {
        Story story = getStoryById(id);
        if (patch.getCaption() != null) story.setCaption(patch.getCaption());
        if (patch.getExpiresAt() != null) story.setExpiresAt(patch.getExpiresAt());
        if (patch.getMediaUrl() != null) story.setMediaUrl(patch.getMediaUrl());
        return storyRepository.save(story);
    }

    public void deleteStory(Long id) {
        if (!storyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Story sa ID " + id + " nije pronađen");
        }
        storyRepository.deleteById(id);
    }

    public StoryView viewStory(Long storyId, Long viewerUserId) {
        Story story = getStoryById(storyId);
        if (storyViewRepository.existsByStoryIdAndViewerUserId(storyId, viewerUserId)) {
            return null;
        }
        StoryView view = StoryView.builder()
                .story(story)
                .viewerUserId(viewerUserId)
                .build();
        return storyViewRepository.save(view);
    }

    // Multi-repo @Transactional — Task 4: view + reakcija u jednoj transakciji
    @Transactional
    public StoryView viewAndReact(Long storyId, Long viewerUserId, String emoji) {
        Story story = getStoryById(storyId);
        if (story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new StoryExpiredException("Story je istekao");
        }
        StoryView view = null;
        if (!storyViewRepository.existsByStoryIdAndViewerUserId(storyId, viewerUserId)) {
            view = storyViewRepository.save(StoryView.builder()
                    .story(story).viewerUserId(viewerUserId).build());
        }
        if (emoji != null && !emoji.isBlank()) {
            reactionRepository.save(StoryReaction.builder()
                    .story(story).userId(viewerUserId).emoji(emoji).build());
        }
        return view;
    }

    public List<StoryView> getViewers(Long storyId, Long requesterId) {
        Story story = getStoryById(storyId);
        if (!story.getUserId().equals(requesterId)) {
            throw new ResourceNotFoundException("Nemate pristup ovim podacima");
        }
        return storyViewRepository.findByStoryId(storyId);
    }

    public long getViewCount(Long storyId) {
        return storyViewRepository.countByStoryId(storyId);
    }

    public StoryReaction addReaction(Long storyId, Long userId, String emoji) {
        Story story = getStoryById(storyId);
        StoryReaction reaction = StoryReaction.builder()
                .story(story).userId(userId).emoji(emoji).build();
        return reactionRepository.save(reaction);
    }

    public List<StoryReaction> getReactions(Long storyId) {
        return reactionRepository.findByStoryId(storyId);
    }

    public void removeReaction(Long reactionId) {
        if (!reactionRepository.existsById(reactionId)) {
            throw new ResourceNotFoundException("Reakcija nije pronađena");
        }
        reactionRepository.deleteById(reactionId);
    }

    public List<StoryPollOption> getPollOptions(Long storyId) {
        return pollOptionRepository.findByStoryIdOrderByDisplayOrder(storyId);
    }

    public StoryPollVote vote(Long storyId, Long optionId, Long userId) {
        Story story = getStoryById(storyId);
        if (story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new StoryExpiredException("Story je istekao, glasanje nije moguće");
        }
        if (pollVoteRepository.existsByPollOptionIdAndUserId(optionId, userId)) {
            throw new RuntimeException("Već si glasao za ovu opciju");
        }
        StoryPollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opcija nije pronađena"));
        option.setVoteCount(option.getVoteCount() + 1);
        pollOptionRepository.save(option);
        StoryPollVote pollVote = StoryPollVote.builder()
                .pollOption(option).userId(userId).build();
        return pollVoteRepository.save(pollVote);
    }

    // Batch vote — Task 4: batch unos u transakciji
    @Transactional
    public List<StoryPollVote> batchVote(Long storyId, List<BatchVoteRequest.VoteItem> voteItems) {
        Story story = getStoryById(storyId);
        if (story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new StoryExpiredException("Story je istekao, glasanje nije moguće");
        }
        List<StoryPollVote> votes = voteItems.stream()
                .filter(v -> !pollVoteRepository.existsByPollOptionIdAndUserId(v.getOptionId(), v.getUserId()))
                .map(v -> {
                    StoryPollOption option = pollOptionRepository.findById(v.getOptionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Opcija nije pronađena"));
                    option.setVoteCount(option.getVoteCount() + 1);
                    pollOptionRepository.save(option);
                    return StoryPollVote.builder()
                            .pollOption(option).userId(v.getUserId()).build();
                })
                .collect(Collectors.toList());
        return pollVoteRepository.saveAll(votes);
    }
}
