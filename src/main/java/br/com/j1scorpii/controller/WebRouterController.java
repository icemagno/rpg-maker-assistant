package br.com.j1scorpii.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebRouterController {

    @Value("${spring.application.name}")
    String appName;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        return "index";
    }	

    @GetMapping("/addtext")
    public String addText(Model model) {
        model.addAttribute("appName", appName);
        return "addtext";
    }	
    
}
