package dsa.sheduler.dsa_sheduler_app.Controller;

import dsa.sheduler.dsa_sheduler_app.Entity.Problem;
import dsa.sheduler.dsa_sheduler_app.Service.ProblemService;
import dsa.sheduler.dsa_sheduler_app.Service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/schedule")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ProblemService problemService;

    @GetMapping("/daily")
    public String getDailyProblems(Model model) {
        List<Problem> dailyProblems = schedulerService.getTodaysProblems();

        // Debug logging
        System.out.println("Total daily problems: " + dailyProblems.size());
        long completedCount = dailyProblems.stream().filter(Problem::isCompleted).count();
        System.out.println("Completed problems: " + completedCount);

        model.addAttribute("dailyProblems", dailyProblems);
        model.addAttribute("today", java.time.LocalDate.now());
        return "schedule/daily";
    }

    @GetMapping("/refresh")
    public String refreshDailySchedule() {
        // Manual refresh endpoint
        schedulerService.generateDailySchedule();
        return "redirect:/schedule/daily";
    }

    @PostMapping("/complete/{id}")
    public String markProblemCompleted(@PathVariable Long id) {
        schedulerService.updateProblemStatus(id, true);

        // Debug: Verify the update
        Optional<Problem> updatedProblem = problemService.getProblemById(id);
        updatedProblem.ifPresent(problem ->
                System.out.println("After completion - Problem: " + problem.getTitle() +
                        ", Completed: " + problem.isCompleted()));

        return "redirect:/schedule/daily";
    }

    @PostMapping("/incomplete/{id}")
    public String markProblemIncomplete(@PathVariable Long id) {
        schedulerService.updateProblemStatus(id, false);

        // Debug: Verify the update
        Optional<Problem> updatedProblem = problemService.getProblemById(id);
        updatedProblem.ifPresent(problem ->
                System.out.println("After incomplete - Problem: " + problem.getTitle() +
                        ", Completed: " + problem.isCompleted()));

        return "redirect:/schedule/daily";
    }

}
