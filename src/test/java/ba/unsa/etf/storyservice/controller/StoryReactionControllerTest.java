package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.model.StoryReaction;
import ba.unsa.etf.storyservice.repository.StoryReactionRepository;
import ba.unsa.etf.storyservice.repository.StoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoryReactionController.class)
class StoryReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoryRepository storyRepository;

    @MockitoBean
    private StoryReactionRepository reactionRepository;

    private Story storyWithId(Long id) {
        Story s = new Story();
        s.setId(id);
        s.setUserId(1L);
        s.setMediaUrl("http://example.com/img.jpg");
        s.setExpiresAt(LocalDateTime.now().plusHours(24));
        return s;
    }

    // ✅ POST /api/stories/{storyId}/reactions — dodaj reakciju
    @Test
    void addReaction_success_returns200() throws Exception {
        StoryReaction reaction = new StoryReaction();
        reaction.setId(1L);
        reaction.setEmoji("❤️");

        when(storyRepository.findById(1L)).thenReturn(Optional.of(storyWithId(1L)));
        when(reactionRepository.save(any())).thenReturn(reaction);

        mockMvc.perform(post("/api/stories/1/reactions?userId=2&emoji=❤️"))
                .andExpect(status().isOk());
    }

    // ❌ POST /api/stories/{storyId}/reactions — story ne postoji
    @Test
    void addReaction_storyNotFound_returns404() throws Exception {
        when(storyRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/stories/99/reactions?userId=2&emoji=❤️"))
                .andExpect(status().isNotFound());
    }

    // ✅ GET /api/stories/{storyId}/reactions — lista reakcija
    @Test
    void getReactions_success_returns200() throws Exception {
        StoryReaction reaction = new StoryReaction();
        reaction.setEmoji("❤️");
        when(reactionRepository.findByStoryId(1L)).thenReturn(List.of(reaction));

        mockMvc.perform(get("/api/stories/1/reactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ GET /api/stories/{storyId}/reactions — prazna lista
    @Test
    void getReactions_empty_returns200() throws Exception {
        when(reactionRepository.findByStoryId(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/stories/2/reactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ✅ DELETE /api/stories/{storyId}/reactions/{reactionId} — ukloni reakciju
    @Test
    void removeReaction_success_returns204() throws Exception {
        when(reactionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reactionRepository).deleteById(1L);

        mockMvc.perform(delete("/api/stories/1/reactions/1"))
                .andExpect(status().isNoContent());
    }

    // ❌ DELETE — reakcija ne postoji
    @Test
    void removeReaction_notFound_returns404() throws Exception {
        when(reactionRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/stories/1/reactions/99"))
                .andExpect(status().isNotFound());
    }
}
