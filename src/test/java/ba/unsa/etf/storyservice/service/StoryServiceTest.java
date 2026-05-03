package ba.unsa.etf.storyservice.service;

import ba.unsa.etf.storyservice.enums.StoryType;
import ba.unsa.etf.storyservice.exception.ResourceNotFoundException;
import ba.unsa.etf.storyservice.exception.StoryExpiredException;
import ba.unsa.etf.storyservice.model.*;
import ba.unsa.etf.storyservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock private StoryRepository storyRepository;
    @Mock private StoryViewRepository storyViewRepository;
    @Mock private StoryReactionRepository reactionRepository;
    @Mock private StoryPollOptionRepository pollOptionRepository;
    @Mock private StoryPollVoteRepository pollVoteRepository;

    @InjectMocks
    private StoryService storyService;

    private Story activeStory;
    private Story expiredStory;

    @BeforeEach
    void setUp() {
        activeStory = Story.builder()
                .id(1L)
                .userId(1L)
                .type(StoryType.PHOTO)
                .mediaUrl("https://example.com/photo.jpg")
                .caption("Test story")
                .hasPoll(false)
                .expiresAt(LocalDateTime.now().plusHours(12))
                .createdAt(LocalDateTime.now())
                .build();

        expiredStory = Story.builder()
                .id(2L)
                .userId(2L)
                .type(StoryType.VIDEO)
                .mediaUrl("https://example.com/video.mp4")
                .hasPoll(false)
                .expiresAt(LocalDateTime.now().minusHours(2))
                .createdAt(LocalDateTime.now().minusHours(26))
                .build();
    }

    @Test
    void createStory_uspjesan() {
        when(storyRepository.save(any(Story.class))).thenReturn(activeStory);

        Story result = storyService.createStory(activeStory);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(storyRepository, times(1)).save(any(Story.class));
    }

    @Test
    void getStoryById_uspjesan() {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(activeStory));

        Story result = storyService.getStoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getStoryById_nijePronadjen_bacaGresku() {
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storyService.getStoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nije pronađen");
    }

    @Test
    void getActiveStoriesByUser_vratiAktivne() {
        when(storyRepository.findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
                eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(activeStory));

        List<Story> result = storyService.getActiveStoriesByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void viewStory_uspjesan_noviPregled() {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(activeStory));
        when(storyViewRepository.existsByStoryIdAndViewerUserId(1L, 2L)).thenReturn(false);
        StoryView view = StoryView.builder().id(1L).story(activeStory).viewerUserId(2L).build();
        when(storyViewRepository.save(any(StoryView.class))).thenReturn(view);

        StoryView result = storyService.viewStory(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getViewerUserId()).isEqualTo(2L);
    }

    @Test
    void viewStory_duplikatPregled_vratiNull() {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(activeStory));
        when(storyViewRepository.existsByStoryIdAndViewerUserId(1L, 2L)).thenReturn(true);

        StoryView result = storyService.viewStory(1L, 2L);

        assertThat(result).isNull();
        verify(storyViewRepository, never()).save(any());
    }

    @Test
    void vote_istekaoStory_bacaGresku() {
        when(storyRepository.findById(2L)).thenReturn(Optional.of(expiredStory));

        assertThatThrownBy(() -> storyService.vote(2L, 1L, 1L))
                .isInstanceOf(StoryExpiredException.class)
                .hasMessageContaining("istekao");
    }

    @Test
    void deleteStory_uspjesan() {
        when(storyRepository.existsById(1L)).thenReturn(true);
        doNothing().when(storyRepository).deleteById(1L);

        assertThatCode(() -> storyService.deleteStory(1L)).doesNotThrowAnyException();
        verify(storyRepository).deleteById(1L);
    }

    @Test
    void deleteStory_nijePronadjen_bacaGresku() {
        when(storyRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> storyService.deleteStory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
