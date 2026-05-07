package ba.unsa.etf.storyservice.repository;

import ba.unsa.etf.storyservice.model.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    List<Story> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime now);

    List<Story> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Story> findByExpiresAtBefore(LocalDateTime now);

    // Pageable — Task 4: paginacija i sortiranje
    Page<Story> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now, Pageable pageable);

    // Custom @Query — Task 4: aktivni storiji sa min. brojem pregleda
    @Query("SELECT s FROM Story s WHERE s.expiresAt > :now AND SIZE(s.views) >= :min ORDER BY SIZE(s.views) DESC")
    List<Story> findActiveStoriesWithMinViews(@Param("now") LocalDateTime now, @Param("min") int min);

    // EntityGraph — Task 4: ucitava views u jednom JOIN query-u (N+1 fix)
    // Napomena: Hibernate ne dopusta simultani eager-load dvije List kolekcije (MultipleBagFetchException)
    @EntityGraph(attributePaths = {"views"})
    Optional<Story> findWithDetailsById(Long id);
}
