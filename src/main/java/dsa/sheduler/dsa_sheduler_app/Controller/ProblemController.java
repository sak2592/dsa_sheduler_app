package dsa.sheduler.dsa_sheduler_app.Controller;

import dsa.sheduler.dsa_sheduler_app.Entity.Platform;
import dsa.sheduler.dsa_sheduler_app.Entity.Problem;
import dsa.sheduler.dsa_sheduler_app.Entity.Topic;
import dsa.sheduler.dsa_sheduler_app.Service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;


    @GetMapping
    public String getAllProblems(Model model) {
        model.addAttribute("problems", problemService.getAllProblems());
        model.addAttribute("today", LocalDate.now());
        return "problems/list";
    }



    // Show form for creating new problem
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("problem", new Problem());
        model.addAttribute("difficulties", Problem.Difficulty.values());
        model.addAttribute("topics", Topic.values());
        model.addAttribute("statuses", Problem.Status.values());
        model.addAttribute("platforms", Platform.values());
        return "problems/form";
    }

    // Create new problem
    @PostMapping
    public String createProblem(@Valid @ModelAttribute("problem") Problem problem,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("difficulties", Problem.Difficulty.values());
           model.addAttribute("topics", Topic.values());
            model.addAttribute("statuses", Problem.Status.values());
            model.addAttribute("platforms", Platform.values());
            return "problems/form";
        }
        problemService.saveProblem(problem);
        return "redirect:/problems";
    }


    // Show form for editing problem
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Problem> problem = problemService.getProblemById(id);
        if (problem.isPresent()) {
            model.addAttribute("problem", problem.get());
            model.addAttribute("difficulties", Problem.Difficulty.values());
            model.addAttribute("topics", Topic.values());
            model.addAttribute("statuses", Problem.Status.values());
            model.addAttribute("platforms", Platform.values());
            return "problems/form";
        }
        return "redirect:/problems";
    }

    // Update problem
    @PostMapping("/{id}")
    public String updateProblem(@PathVariable Long id,
                                @Valid @ModelAttribute("problem") Problem problem,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("difficulties", Problem.Difficulty.values());
           // model.addAttribute("topics", Problem.Topic.values());
            model.addAttribute("statuses", Problem.Status.values());
            return "problems/form";
        }
        problem.setId(id);
        problemService.saveProblem(problem);
        return "redirect:/problems";
    }

    // Delete problem
    @GetMapping("/{id}/delete")
    public String deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return "redirect:/problems";
    }

    // View problem details
    @GetMapping("/{id}")
    public String viewProblem(@PathVariable Long id, Model model) {
        Optional<Problem> problem = problemService.getProblemById(id);
        if (problem.isPresent()) {
            model.addAttribute("problem", problem.get());
            return "problems/view";
        }
        return "redirect:/problems";
    }

    // Filter problems by topic
    @GetMapping("/topic/{topic}")
    public String getProblemsByTopic(@PathVariable Topic topic, Model model) {
        model.addAttribute("problems", problemService.getProblemsByTopic(topic));
        model.addAttribute("selectedTopic", topic);
        model.addAttribute("today", LocalDate.now());
        return "problems/list";
    }

    // Filter problems by difficulty
    @GetMapping("/difficulty/{difficulty}")
    public String getProblemsByDifficulty(@PathVariable Problem.Difficulty difficulty, Model model) {
        model.addAttribute("problems", problemService.getProblemsByDifficulty(difficulty));
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("today", LocalDate.now());
        return "problems/list";
    }

    // Filter problems by difficulty
    @GetMapping("/status/{status}")
    public String getProblemsByStatus(@PathVariable Problem.Status status, Model model) {
        model.addAttribute("problems", problemService.getProblemsByStatus(status));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("today", LocalDate.now());
        return "problems/list";
    }


    // Show upcoming problems
    @GetMapping("/upcoming")
    public String getUpcomingProblems(Model model) {
       // model.addAttribute("problems", problemService.getUpcomingProblems());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("showUpcoming", true);
        return "problems/list";
    }

    // Search problems
    @GetMapping("/search")
    public String searchProblems(@RequestParam String keyword, Model model) {
        model.addAttribute("problems", problemService.searchProblems(keyword));
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("today", LocalDate.now());
        return "problems/list";
    }

    //timer


    @PostMapping("/{id}/stop-timer")
    public String stopTimer(@PathVariable Long id) {
        //problemService.changeStatusPending(id);
        return "redirect:/problems/" + id;
    }

    @PostMapping("/{id}/reset-status")
    public String resetStatus(@PathVariable Long id) {
        problemService.changeStatusPending(id);
        return "redirect:/problems/" + id;
    }


    @PostMapping("/{id}/start-timer")
    @ResponseBody
    public ResponseEntity<String> startTimer(@PathVariable Long id) {
        Optional<Problem> problemOpt = problemService.getProblemById(id);
        if (problemOpt.isPresent()) {
            Problem problem = problemOpt.get();
            problem.setStatus(Problem.Status.IN_PROGRESS);
            problemService.saveProblem(problem);
            return ResponseEntity.ok("Timer started");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/complete")
    @ResponseBody
    public ResponseEntity<String> completeProblem(@PathVariable Long id) {

        Problem problem=problemService.markProblemAsCompleted(id);
        if (problem != null) {
            return ResponseEntity.ok("Problem completed");
        }
        return ResponseEntity.notFound().build();
    }

}
