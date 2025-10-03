package dsa.sheduler.dsa_sheduler_app.Repository;

import dsa.sheduler.dsa_sheduler_app.Entity.Problem;
import dsa.sheduler.dsa_sheduler_app.Entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem,Long> {


    List<Problem> findByOrderByUpdatedAtAsc();
    @Query(value = "SELECT * FROM problems WHERE LOWER(title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%'))", nativeQuery = true)
    List<Problem> findByTitleOrDescriptionContaining(@Param("keyword") String keyword);
    List<Problem> findByStatus(Problem.Status status);
    List<Problem> findByTopic(Topic topic);
    List<Problem> findByDifficulty(Problem.Difficulty difficulty);
    List<Problem> findByNextReviewDateLessThanEqual(LocalDate date);
    List<Problem> findByCompletedFalseAndNextReviewDateLessThanEqual(LocalDate date);
    List<Problem> findByCompletedTrueAndNextReviewDateLessThanEqual(LocalDate date);
}
