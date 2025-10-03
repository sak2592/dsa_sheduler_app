package dsa.sheduler.dsa_sheduler_app.config;


import dsa.sheduler.dsa_sheduler_app.Entity.Problem;
import dsa.sheduler.dsa_sheduler_app.Entity.Topic;
import dsa.sheduler.dsa_sheduler_app.Repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProblemRepository repository;

    @Override
    public void run(String... args) throws Exception {
        // Add sample data only if the database is empty
        if (repository.count() == 0) {
            System.out.println("Initializing database with sample data...");

            Problem problem1 = new Problem();
            problem1.setTitle("Two Sum");
            problem1.setDescription("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.");
            problem1.setProblemUrl("https://leetcode.com/problems/two-sum/");
            problem1.setSolutionUrl("https://github.com/solutions/two-sum");
            problem1.setDifficulty(Problem.Difficulty.EASY);
            problem1.setTopic(Topic.ARRAY);
            problem1.setStatus(Problem.Status.COMPLETED);
            //problem1.setScheduledDate(LocalDate.now().minusDays(2));
            problem1.setNotes("Used HashMap for O(n) time complexity solution. Space complexity O(n).");

            Problem problem2 = new Problem();
            problem2.setTitle("Binary Tree Level Order Traversal");
            problem2.setDescription("Given the root of a binary tree, return the level order traversal of its nodes' values.");
            problem2.setProblemUrl("https://leetcode.com/problems/binary-tree-level-order-traversal/");
            problem2.setDifficulty(Problem.Difficulty.MEDIUM);
            problem2.setTopic(Topic.TREE);
            problem2.setStatus(Problem.Status.IN_PROGRESS);
           // problem2.setScheduledDate(LocalDate.now().plusDays(1));
            problem2.setNotes("Use BFS with queue. Time complexity O(n), space complexity O(n).");

            Problem problem3 = new Problem();
            problem3.setTitle("Magnetic Force Between Two Balls");
            problem3.setDescription("In the universe Earth C-137, Rick discovered a special form of magnetic force between two balls if they are put in his new invented basket. Rick has n empty");
            problem3.setProblemUrl("https://leetcode.com/problems/binary-tree-level-order-traversal/");
            problem3.setDifficulty(Problem.Difficulty.MEDIUM);
            problem3.setTopic(Topic.ARRAY);
            problem3.setStatus(Problem.Status.IN_PROGRESS);
           // problem3.setScheduledDate(LocalDate.now().plusDays(1));
            problem3.setNotes("Use BFS with queue. Time complexity O(n), space complexity O(n).");


            Problem problem4 = new Problem();
            problem4.setTitle("problem 4 ");
            problem4.setDescription("Between two balls if they are put in his new invented basket. Rick has n empty");
            problem4.setProblemUrl("https://leetcode.com/problems/binary-tree-level-order-traversal/");
            problem4.setDifficulty(Problem.Difficulty.MEDIUM);
            problem4.setTopic(Topic.ARRAY);
            problem4.setStatus(Problem.Status.IN_PROGRESS);
            //problem4.setScheduledDate(LocalDate.now().plusDays(1));
            problem4.setNotes("Use BFS with queue. Time complexity O(n), space complexity O(n).");


            repository.save(problem1);
            repository.save(problem2);
            repository.save(problem3);
            repository.save(problem4);

            System.out.println("Sample data initialized successfully!");
        }
    }
}
