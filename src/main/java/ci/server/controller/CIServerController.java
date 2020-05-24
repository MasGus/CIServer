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
        model.addAttribute("status", bisectionService.status);
        model.addAttribute("result", bisectionService.result);
        model.addAttribute("bisectStartedCommit", bisectionService.bisectStartedCommit);
        model.addAttribute("commitCount", bisectionService.commitCount);
        Long timeLeft = null;
        if (bisectionService.commitCount != null && bisectionService.startedTime != null) {
            timeLeft = bisectionService.commitCount * 5 - (System.currentTimeMillis() - bisectionService.startedTime) / 60000;
            timeLeft = timeLeft > 0 ? timeLeft : 1;
        }
        model.addAttribute("bisectionTimeLeft", !bisectionService.status.equals(BisectionStatus.processing) ? null : timeLeft);
        model.addAttribute("exception", bisectionService.exception);
        model.addAttribute("repoPath", bisectionService.repoPath);
        model.addAttribute("branchName", bisectionService.branchName);
        return "bisectionInfo";
    }
}
