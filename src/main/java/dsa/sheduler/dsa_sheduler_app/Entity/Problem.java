package dsa.sheduler.dsa_sheduler_app.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;



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
    @Enumerated(EnumType.STRING)
    private Topic topic; // Array, String, Tree, Graph, etc.

    @Enumerated(EnumType.STRING)
    private Platform platform; // LeetCode, HackerRank, CodeForces, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
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
        PENDING, IN_PROGRESS, COMPLETED
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



}