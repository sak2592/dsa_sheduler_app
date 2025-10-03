package dsa.sheduler.dsa_sheduler_app.Service;

import dsa.sheduler.dsa_sheduler_app.Entity.Problem;
import dsa.sheduler.dsa_sheduler_app.Entity.Topic;
import dsa.sheduler.dsa_sheduler_app.Repository.ProblemRepository;
import dsa.sheduler.dsa_sheduler_app.util.Constants;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProblemService {

  @Autowired
  private ProblemRepository problemRepository;


    public void saveProblem(@Valid Problem problem) {
        problemRepository.save(problem);
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findByOrderByUpdatedAtAsc();
    }

    public Optional<Problem> getProblemById(Long id) {
        return problemRepository.findById(id);
    }


    public List<Problem> getProblemsByTopic(Topic topic) {
        return problemRepository.findByTopic(topic);
    }

    public List<Problem> getProblemsByDifficulty(Problem.Difficulty difficulty) {
        return problemRepository.findByDifficulty(difficulty);
    }

    // Add to your existing ProblemService
    public List<Problem> getProblemsDueForReview() {
        return problemRepository.findByNextReviewDateLessThanEqual(LocalDate.now());
    }

    public List<Problem> getPendingProblems() {
        return problemRepository.findByCompletedFalseAndNextReviewDateLessThanEqual(LocalDate.now());
    }

    public List<Problem> getCompletedProblemsDueForReview() {
        return problemRepository.findByCompletedTrueAndNextReviewDateLessThanEqual(LocalDate.now());
    }

    public Problem updateProblem(Long id, Problem problemDetails) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));

        problem.setTitle(problemDetails.getTitle());
        problem.setDifficulty(problemDetails.getDifficulty());
        problem.setDescription(problemDetails.getDescription());
        problem.setTopic(problemDetails.getTopic());
        problem.setProblemUrl(problemDetails.getProblemUrl());
        problem.setStatus(problemDetails.getStatus());

        return problemRepository.save(problem);
    }

    public void deleteProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
        problemRepository.delete(problem);
    }

    public List<Problem> searchProblems(String keyword) {
        return problemRepository.findByTitleOrDescriptionContaining(keyword);
    }


    public Problem markProblemAsCompleted(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
        int reviewC=problem.getReviewCount();
        int strk=problem.getStreak();
        problem.setCompleted(true);
        problem.setStatus(Problem.Status.COMPLETED);
        problem.setLastReviewed(LocalDate.now());
        reviewC=reviewC+1;
        problem.setReviewCount(reviewC);
        problem.setStreak(strk++);


        // Calculate next review date based on spaced repetition
        if (reviewC <= Constants.REVIEW_INTERVALS.length) {
            LocalDate nextReviewDate = LocalDate.now().plusDays(Constants.REVIEW_INTERVALS[reviewC - 1]);
            problem.setNextReviewDate(nextReviewDate);
        } else {
            // After going through all intervals, review monthly
            LocalDate nextReviewDate = LocalDate.now().plusDays(30);
            problem.setNextReviewDate(nextReviewDate);
        }
        return  problemRepository.save(problem);
    }

    public Problem changeStatusPending(Long id){
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
        if (problem != null) {
            problem.setStatus(Problem.Status.PENDING);
            problemRepository.save(problem);
        }
        return problem;
    }

    public Problem markProblemAsNotCompleted(Long id){
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
        int reviewC=problem.getReviewCount();
        problem.setCompleted(false);
        problem.setStatus(Problem.Status.IN_PROGRESS);
        problem.setNextReviewDate(LocalDate.now());
        problem.setReviewCount(reviewC);
        problem.setStreak(0);

        return  problemRepository.save(problem);
    }


    public List<Problem> getProblemsByStatus(Problem.Status status) {
        return problemRepository.findByStatus(status);
    }





}


