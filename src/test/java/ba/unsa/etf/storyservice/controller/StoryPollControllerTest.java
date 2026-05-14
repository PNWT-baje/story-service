package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.model.StoryPollOption;
import ba.unsa.etf.storyservice.model.StoryPollVote;
import ba.unsa.etf.storyservice.repository.StoryPollOptionRepository;
import ba.unsa.etf.storyservice.repository.StoryPollVoteRepository;
import ba.unsa.etf.storyservice.repository.StoryRepository;
import ba.unsa.etf.storyservice.service.StoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoryPollController.class)
class StoryPollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoryRepository storyRepository;

    @MockitoBean
    private StoryPollOptionRepository pollOptionRepository;

    @MockitoBean
    private StoryPollVoteRepository pollVoteRepository;

    @MockitoBean
    private StoryService storyService;

    private Story activeStory(Long id) {
        Story s = new Story();
        s.setId(id);
        s.setUserId(1L);
        s.setMediaUrl("http://example.com/img.jpg");
        s.setExpiresAt(LocalDateTime.now().plusHours(24));
        return s;
    }

    private Story expiredStory(Long id) {
        Story s = activeStory(id);
        s.setExpiresAt(LocalDateTime.now().minusHours(1));
        return s;
    }

    // ✅ GET /api/stories/{storyId}/poll/options — lista opcija
    @Test
    void getPollOptions_success_returns200() throws Exception {
        StoryPollOption opt = new StoryPollOption();
        opt.setId(1L);
        opt.setOptionText("Da");
        opt.setDisplayOrder(1);
        when(pollOptionRepository.findByStoryIdOrderByDisplayOrder(1L)).thenReturn(List.of(opt));

        mockMvc.perform(get("/api/stories/1/poll/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ GET /api/stories/{storyId}/poll/options — prazna lista
    @Test
    void getPollOptions_empty_returns200() throws Exception {
        when(pollOptionRepository.findByStoryIdOrderByDisplayOrder(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/stories/2/poll/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ✅ POST /api/stories/{storyId}/poll/vote/{optionId} — glasanje
    @Test
    void vote_success_returns200() throws Exception {
        StoryPollOption opt = new StoryPollOption();
        opt.setId(1L);
        opt.setVoteCount(0);
        opt.setDisplayOrder(1);
        StoryPollVote vote = new StoryPollVote();
        vote.setId(1L);

        when(storyRepository.findById(1L)).thenReturn(Optional.of(activeStory(1L)));
        when(pollVoteRepository.existsByPollOptionIdAndUserId(1L, 2L)).thenReturn(false);
        when(pollOptionRepository.findById(1L)).thenReturn(Optional.of(opt));
        when(pollVoteRepository.save(any())).thenReturn(vote);

        mockMvc.perform(post("/api/stories/1/poll/vote/1?userId=2"))
                .andExpect(status().isOk());
    }

    // ❌ POST vote — story istekao
    @Test
    void vote_storyExpired_returns400() throws Exception {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(expiredStory(1L)));

        mockMvc.perform(post("/api/stories/1/poll/vote/1?userId=2"))
                .andExpect(status().isBadRequest());
    }

    // ❌ POST vote — već glasao
    @Test
    void vote_alreadyVoted_returns400() throws Exception {
        when(storyRepository.findById(1L)).thenReturn(Optional.of(activeStory(1L)));
        when(pollVoteRepository.existsByPollOptionIdAndUserId(1L, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/stories/1/poll/vote/1?userId=2"))
                .andExpect(status().isBadRequest());
    }

    // ✅ GET /api/stories/{storyId}/poll/options/{optionId}/votes — broj glasova
    @Test
    void getVoteCount_success_returns200() throws Exception {
        when(pollVoteRepository.countByPollOptionId(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/stories/1/poll/options/1/votes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    // ✅ POST /api/stories/{storyId}/poll/votes/batch — batch glasanje
    @Test
    void batchVote_success_returns200() throws Exception {
        StoryPollVote vote = new StoryPollVote();
        vote.setId(1L);
        when(storyService.batchVote(eq(1L), any())).thenReturn(List.of(vote));

        mockMvc.perform(post("/api/stories/1/poll/votes/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "votes": [
                                        {"optionId": 1, "userId": 2},
                                        {"optionId": 1, "userId": 3}
                                    ]
                                }
                                """))
                .andExpect(status().isOk());
    }

    // ❌ POST /batch — validation fail (prazna lista)
    @Test
    void batchVote_emptyVotes_returns400() throws Exception {
        mockMvc.perform(post("/api/stories/1/poll/votes/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"votes": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }
}
