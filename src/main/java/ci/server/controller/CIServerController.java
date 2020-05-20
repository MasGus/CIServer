package ci.server.controller;

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
        model.addAttribute("isFinished", bisectionService.isFinished);
        model.addAttribute("result", bisectionService.result);
        model.addAttribute("exception", bisectionService.exception);
        return "bisectionInfo";
    }
}
