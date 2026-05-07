package ba.unsa.etf.storyservice.controller;

import ba.unsa.etf.storyservice.dto.CreateStoryRequest;
import ba.unsa.etf.storyservice.enums.StoryType;
import ba.unsa.etf.storyservice.exception.ResourceNotFoundException;
import ba.unsa.etf.storyservice.model.Story;
import ba.unsa.etf.storyservice.service.StoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoryController.class)
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoryService storyService;

    @Autowired
    private ObjectMapper objectMapper;

    private Story sampleStory;

    @BeforeEach
    void setUp() {
        sampleStory = new Story();
        sampleStory.setId(1L);
        sampleStory.setUserId(1L);
        sampleStory.setType(StoryType.PHOTO);
        sampleStory.setMediaUrl("http://example.com/img.jpg");
        sampleStory.setExpiresAt(LocalDateTime.now().plusHours(24));
    }

    // ✅ POST /api/stories — kreiranje storija
    @Test
    void createStory_success_returns201() throws Exception {
        when(storyService.createStory(any())).thenReturn(sampleStory);

        mockMvc.perform(post("/api/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 1,
                                    "type": "PHOTO",
                                    "mediaUrl": "http://example.com/img.jpg"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    // ❌ POST /api/stories — validation fail (bez mediaUrl)
    @Test
    void createStory_missingMediaUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 1,
                                    "type": "PHOTO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    // ❌ POST /api/stories — validation fail (bez userId)
    @Test
    void createStory_missingUserId_returns400() throws Exception {
        mockMvc.perform(post("/api/stories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "PHOTO",
                                    "mediaUrl": "http://example.com/img.jpg"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    // ✅ GET /api/stories/user/{userId} — lista aktivnih storija
    @Test
    void getActiveStoriesByUser_success_returns200() throws Exception {
        when(storyService.getActiveStoriesByUser(1L)).thenReturn(List.of(sampleStory));

        mockMvc.perform(get("/api/stories/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ GET /api/stories/user/{userId}/paged — paginirana lista
    @Test
    void getStoriesByUserPaged_success_returns200() throws Exception {
        when(storyService.getActiveStoriesByUserPaged(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleStory)));

        mockMvc.perform(get("/api/stories/user/1/paged?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    // ✅ GET /api/stories/{id} — dohvat po ID-u
    @Test
    void getStoryById_success_returns200() throws Exception {
        when(storyService.getStoryById(1L)).thenReturn(sampleStory);

        mockMvc.perform(get("/api/stories/1"))
                .andExpect(status().isOk());
    }

    // ❌ GET /api/stories/{id} — nije pronađen
    @Test
    void getStoryById_notFound_returns404() throws Exception {
        when(storyService.getStoryById(99L))
                .thenThrow(new ResourceNotFoundException("Story sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/stories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    // ✅ PATCH /api/stories/{id} — parcijalni update
    @Test
    void patchStory_success_returns200() throws Exception {
        sampleStory.setCaption("Novi caption");
        when(storyService.patchStory(eq(1L), any())).thenReturn(sampleStory);

        mockMvc.perform(patch("/api/stories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caption\": \"Novi caption\"}"))
                .andExpect(status().isOk());
    }

    // ❌ PATCH /api/stories/{id} — nije pronađen
    @Test
    void patchStory_notFound_returns404() throws Exception {
        when(storyService.patchStory(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Story sa ID 99 nije pronađen"));

        mockMvc.perform(patch("/api/stories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"caption\": \"test\"}"))
                .andExpect(status().isNotFound());
    }

    // ✅ DELETE /api/stories/{id} — brisanje
    @Test
    void deleteStory_success_returns204() throws Exception {
        doNothing().when(storyService).deleteStory(1L);

        mockMvc.perform(delete("/api/stories/1"))
                .andExpect(status().isNoContent());
    }

    // ❌ DELETE /api/stories/{id} — nije pronađen
    @Test
    void deleteStory_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Story sa ID 99 nije pronađen"))
                .when(storyService).deleteStory(99L);

        mockMvc.perform(delete("/api/stories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    // ✅ GET /api/stories/trending — trending storiji
    @Test
    void getTrendingStories_success_returns200() throws Exception {
        when(storyService.getActiveStoriesWithMinViews(1)).thenReturn(List.of(sampleStory));

        mockMvc.perform(get("/api/stories/trending?minViews=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
