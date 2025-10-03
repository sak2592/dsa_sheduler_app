package dsa.sheduler.dsa_sheduler_app.Entity;

import dsa.sheduler.dsa_sheduler_app.util.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
@AllArgsConstructor
@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    //@NotBlank(message = "Problem URL is mandatory")
    //@Column(nullable = false, length = 1000)
    private String problemUrl;
    //@Column(length = 1000)
    private String solutionUrl;
    private Topic topic; // Array, String, Tree, Graph, etc.
    private Platform platform; // LeetCode, HackerRank, CodeForces, etc.
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    private Status status;
    @Column(columnDefinition = "TEXT")
    private String notes;
    // New scheduling fields
    private LocalDate lastReviewed;
    private LocalDate nextReviewDate;
    private int reviewCount = 0;
    private boolean completed = false;
    private int streak = 0; // consecutive days completed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enum for difficulty
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum Status{
        PENDING, IN_PROGRESS, COMPLETED, REVISIT
    }


    // Constructors
    public Problem() {
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    // Spaced repetition intervals (in days)
    //private static final int[] REVIEW_INTERVALS = {3, 4, 7, 14, 30};




    public void markAsCompleted() {
        this.completed = true;
        this.status = Status.COMPLETED;
        this.lastReviewed = LocalDate.now();
        this.reviewCount++;
        this.streak++;

        // Calculate next review date based on spaced repetition
        if (reviewCount <= Constants.REVIEW_INTERVALS.length) {
            this.nextReviewDate = LocalDate.now().plusDays(Constants.REVIEW_INTERVALS[reviewCount - 1]);
        } else {
            // After going through all intervals, review monthly
            this.nextReviewDate = LocalDate.now().plusDays(30);
        }
    }

    public void markAsNotCompleted() {
        this.completed = false;
        this.status = Status.IN_PROGRESS;
        this.streak = 0;
        // Keep it in daily schedule until completed
        this.nextReviewDate = LocalDate.now().plusDays(1);
    }


}