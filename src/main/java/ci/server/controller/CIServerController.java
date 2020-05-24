package ci.server.controller;

import ci.server.entity.BisectionStatus;
import ci.server.service.BisectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CIServerController {
    @Autowired
    private BisectionService bisectionService;

    @GetMapping("/bisectionInfo")
    public String getBisectionInfo(Model model) {
        model.addAttribute("status", bisectionService.getStatus());
        model.addAttribute("result", bisectionService.getResult());
        model.addAttribute("bisectStartedCommit", bisectionService.getBisectStartedCommit());
        model.addAttribute("commitCount", bisectionService.getCommitCount());
        Long timeLeft = null;
        if (bisectionService.getCommitCount() != null && bisectionService.getStartedTime() != null) {
            timeLeft = bisectionService.getCommitCount() * 5 - (System.currentTimeMillis() - bisectionService.getStartedTime()) / 60000;
            timeLeft = timeLeft > 0 ? timeLeft : 1;
        }
        model.addAttribute("bisectionTimeLeft", !bisectionService.getStatus().equals(BisectionStatus.processing) ? null : timeLeft);
        model.addAttribute("exception", bisectionService.getException());
        model.addAttribute("repoPath", bisectionService.getRepoPath());
        model.addAttribute("branchName", bisectionService.getBranchName());
        model.addAttribute("isBadCommitReverted", bisectionService.isBadCommitReverted());
        return "bisectionInfo";
    }
}
