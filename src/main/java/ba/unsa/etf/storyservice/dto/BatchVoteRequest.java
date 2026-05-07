package ba.unsa.etf.storyservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BatchVoteRequest {

    @NotEmpty(message = "Lista glasova ne smije biti prazna")
    private List<VoteItem> votes;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class VoteItem {
        @NotNull(message = "optionId je obavezan")
        private Long optionId;

        @NotNull(message = "userId je obavezan")
        private Long userId;
    }
}
