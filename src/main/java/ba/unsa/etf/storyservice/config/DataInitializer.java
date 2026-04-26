package ba.unsa.etf.storyservice.config;

import ba.unsa.etf.storyservice.enums.StoryType;
import ba.unsa.etf.storyservice.model.*;
import ba.unsa.etf.storyservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StoryRepository storyRepository;
    private final StoryViewRepository storyViewRepository;
    private final StoryReactionRepository storyReactionRepository;
    private final StoryPollOptionRepository pollOptionRepository;
    private final StoryPollVoteRepository pollVoteRepository;

    @Override
    public void run(String... args) {

        // Kreiranje storija — foto story (user 1)
        Story story1 = Story.builder()
                .userId(1L)
                .type(StoryType.PHOTO)
                .mediaUrl("https://example.com/media/story1.jpg")
                .caption("Lijep dan u Sarajevu! ☀️")
                .hasPoll(false)
                .build();

        // Video story (user 2)
        Story story2 = Story.builder()
                .userId(2L)
                .type(StoryType.VIDEO)
                .mediaUrl("https://example.com/media/story2.mp4")
                .caption("Novi projekat u toku 💻")
                .hasPoll(false)
                .build();

        // Story sa anketom (user 1)
        Story story3 = Story.builder()
                .userId(1L)
                .type(StoryType.PHOTO)
                .mediaUrl("https://example.com/media/story3.jpg")
                .caption("Glasajte! 🗳️")
                .hasPoll(true)
                .pollQuestion("Koji programski jezik preferirate?")
                .build();

        // Story koji je istekao (user 3)
        Story storyExpired = Story.builder()
                .userId(3L)
                .type(StoryType.PHOTO)
                .mediaUrl("https://example.com/media/old_story.jpg")
                .caption("Stari story")
                .hasPoll(false)
                .expiresAt(LocalDateTime.now().minusHours(2))
                .build();

        storyRepository.save(story1);
        storyRepository.save(story2);
        storyRepository.save(story3);
        storyRepository.save(storyExpired);

        // Pregledi storija
        StoryView view1 = StoryView.builder()
                .story(story1)
                .viewerUserId(2L)
                .build();

        StoryView view2 = StoryView.builder()
                .story(story1)
                .viewerUserId(3L)
                .build();

        StoryView view3 = StoryView.builder()
                .story(story2)
                .viewerUserId(1L)
                .build();

        storyViewRepository.save(view1);
        storyViewRepository.save(view2);
        storyViewRepository.save(view3);

        // Reakcije na storije
        StoryReaction reaction1 = StoryReaction.builder()
                .story(story1)
                .userId(2L)
                .emoji("❤️")
                .build();

        StoryReaction reaction2 = StoryReaction.builder()
                .story(story1)
                .userId(3L)
                .emoji("😍")
                .build();

        StoryReaction reaction3 = StoryReaction.builder()
                .story(story2)
                .userId(1L)
                .emoji("🔥")
                .build();

        storyReactionRepository.save(reaction1);
        storyReactionRepository.save(reaction2);
        storyReactionRepository.save(reaction3);

        // Opcije ankete za story3
        StoryPollOption option1 = StoryPollOption.builder()
                .story(story3)
                .optionText("Java")
                .voteCount(0)
                .displayOrder(1)
                .build();

        StoryPollOption option2 = StoryPollOption.builder()
                .story(story3)
                .optionText("Python")
                .voteCount(0)
                .displayOrder(2)
                .build();

        StoryPollOption option3 = StoryPollOption.builder()
                .story(story3)
                .optionText("JavaScript")
                .voteCount(0)
                .displayOrder(3)
                .build();

        pollOptionRepository.save(option1);
        pollOptionRepository.save(option2);
        pollOptionRepository.save(option3);

        // Glasovi za anketu
        StoryPollVote vote1 = StoryPollVote.builder()
                .pollOption(option1)
                .userId(2L)
                .build();
        option1.setVoteCount(1);

        StoryPollVote vote2 = StoryPollVote.builder()
                .pollOption(option2)
                .userId(3L)
                .build();
        option2.setVoteCount(1);

        StoryPollVote vote3 = StoryPollVote.builder()
                .pollOption(option1)
                .userId(4L)
                .build();
        option1.setVoteCount(2);

        pollVoteRepository.save(vote1);
        pollVoteRepository.save(vote2);
        pollVoteRepository.save(vote3);
        pollOptionRepository.save(option1);
        pollOptionRepository.save(option2);

        System.out.println("Story Service — seed podaci uspješno dodani!");
    }
}
