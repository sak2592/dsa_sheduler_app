package dsa.sheduler.dsa_sheduler_app.Service;

import dsa.sheduler.dsa_sheduler_app.Entity.Problem;
import dsa.sheduler.dsa_sheduler_app.Entity.Topic;
import dsa.sheduler.dsa_sheduler_app.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulerService {

    @Autowired
    private ProblemService problemService;


        /**
         * Generate daily problem set - ALWAYS include problems due for review
         */
        public List<Problem> generateDailyProblemSet() {
            List<Problem> allProblems = problemService.getAllProblems();
            LocalDate today = LocalDate.now();

            // Get problems that are due for review today
            List<Problem> dueProblems = allProblems.stream()
                    .filter(problem -> problem.getNextReviewDate() != null)
                    .filter(problem -> !problem.getNextReviewDate().isAfter(today))
                    .collect(Collectors.toList());

            System.out.println("Due problems: " + dueProblems.size());

            // Separate by completion status
            List<Problem> pendingProblems = dueProblems.stream()
                    .filter(p -> p.getStatus() == Problem.Status.PENDING && !p.isCompleted())
                    .collect(Collectors.toList());

            System.out.println("Pending problems: " + pendingProblems.size());

            List<Problem> inProgressProblems = dueProblems.stream()
                    .filter(p -> p.getStatus() == Problem.Status.IN_PROGRESS)
                    .collect(Collectors.toList());

            System.out.println("In-progress problems: " + inProgressProblems.size());

            List<Problem> reviewProblems = dueProblems.stream()
                    .filter(Problem::isCompleted)
                    .collect(Collectors.toList());

            System.out.println("Review problems: " + reviewProblems.size());

            List<Problem> dailySet = new ArrayList<>();

            // Priority 1: All pending problems (incomplete ones)
            dailySet.addAll(pendingProblems);

            // Priority 2: Problems that are in progress
            if (dailySet.size() < Constants.DAILY_PROBLEM_COUNT) {
                int remainingSlots = Constants.DAILY_PROBLEM_COUNT - dailySet.size();
                List<Problem> selectedInProgress = inProgressProblems.stream()
                        .limit(remainingSlots)
                        .collect(Collectors.toList());
                dailySet.addAll(selectedInProgress);
            }

            // Priority 3: Fill remaining slots with review problems
            if (dailySet.size() < Constants.DAILY_PROBLEM_COUNT) {
                int remainingSlots = Constants.DAILY_PROBLEM_COUNT - dailySet.size();
                List<Problem> selectedReviews = selectMixedReviewProblems(reviewProblems, remainingSlots);
                dailySet.addAll(selectedReviews);
            }

            // Priority 4: If still not enough, add new unscheduled problems
            if (dailySet.size() < Constants.DAILY_PROBLEM_COUNT) {
                int remainingSlots = Constants.DAILY_PROBLEM_COUNT - dailySet.size();
                List<Problem> newProblems = selectNewProblems(allProblems, dailySet, remainingSlots);
                dailySet.addAll(newProblems);
            }

            System.out.println("Final daily set: " + dailySet.size() + " problems");
            return dailySet.stream()
                    .limit(Constants.DAILY_PROBLEM_COUNT)
                    .collect(Collectors.toList());
        }

        /**
         * Scheduled task to ensure daily problems are ready
         * Runs every day at 6 AM
         */
        @Scheduled(cron = "0 0 6 * * ?")
        public void generateDailySchedule() {
            System.out.println("Generating daily schedule at " + LocalDate.now());
            List<Problem> dailyProblems = generateDailyProblemSet();

            // Log the schedule for debugging
            dailyProblems.forEach(problem ->
                    System.out.println("Scheduled: " + problem.getTitle() + " - " + problem.getStatus())
            );
        }

        /**
         * Initialize scheduling for new problems
         */
        public void initializeScheduling(Problem problem) {
            if (problem.getNextReviewDate() == null) {
                problem.setNextReviewDate(LocalDate.now());
                problemService.saveProblem(problem);
                System.out.println("Scheduling initialized for: " + problem.getTitle());
            }
        }

        /**
         * Manually trigger schedule generation (for testing/admin purposes)
         */
        public void forceGenerateSchedule() {
            generateDailySchedule();
        }

    /**
     * Select mixed review problems with variety of topics and difficulties
     */
    private List<Problem> selectMixedReviewProblems(List<Problem> reviewProblems, int count) {
        if (reviewProblems.isEmpty()) return new ArrayList<>();

        // Group by topic and difficulty for variety
        Map<Topic, List<Problem>> byTopic = reviewProblems.stream()
                .collect(Collectors.groupingBy(Problem::getTopic));

        Map<Problem.Difficulty, List<Problem>> byDifficulty = reviewProblems.stream()
                .collect(Collectors.groupingBy(Problem::getDifficulty));

        List<Problem> selected = new ArrayList<>();
        Random random = new Random();

        while (selected.size() < count && !reviewProblems.isEmpty()) {
            // Try to get variety in topics
            for (Topic topic : Topic.values()) {
                List<Problem> topicProblems = byTopic.get(topic);
                if (topicProblems != null && !topicProblems.isEmpty()) {
                    Problem problem = topicProblems.remove(random.nextInt(topicProblems.size()));
                    if (!selected.contains(problem)) {
                        selected.add(problem);
                        if (selected.size() >= count) break;
                    }
                }
            }

            // If still need more, get variety in difficulties
            if (selected.size() < count) {
                for (Problem.Difficulty difficulty : Problem.Difficulty.values()) {
                    List<Problem> difficultyProblems = byDifficulty.get(difficulty);
                    if (difficultyProblems != null && !difficultyProblems.isEmpty()) {
                        for (Problem problem : difficultyProblems) {
                            if (!selected.contains(problem)) {
                                selected.add(problem);
                                if (selected.size() >= count) break;
                            }
                        }
                    }
                    if (selected.size() >= count) break;
                }
            }
        }

        return selected;
    }

    /**
     * Select new problems that haven't been scheduled yet
     */
    private List<Problem> selectNewProblems(List<Problem> allProblems,
                                            List<Problem> alreadySelected,
                                            int count) {
        List<Problem> newProblems = allProblems.stream()
                .filter(p -> p.getNextReviewDate() == null) // Never scheduled
                .filter(p -> !alreadySelected.contains(p))
                .collect(Collectors.toList());

        Collections.shuffle(newProblems);
        return newProblems.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Update problem completion status
     */
    public void updateProblemStatus(Long problemId, boolean completed) {

        if(completed){
            problemService.markProblemAsCompleted(problemId);
        }else{
            problemService.markProblemAsNotCompleted(problemId);
        }

    }

    /**
     * Get today's scheduled problems
     */
    public List<Problem> getTodaysProblems() {
        return generateDailyProblemSet();
    }

}