package ba.unsa.etf.storyservice.repository;

import ba.unsa.etf.storyservice.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // Aktivni storiji korisnika (nisu istekli)
    List<Story> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime now);

    // Svi storiji korisnika (uključujući istekle)
    List<Story> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Svi istekli storiji (za čišćenje)
    List<Story> findByExpiresAtBefore(LocalDateTime now);
}
