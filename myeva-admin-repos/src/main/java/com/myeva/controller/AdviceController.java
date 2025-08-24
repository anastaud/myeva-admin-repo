package com.myeva.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.myeva.service.contract.HealthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class AdviceController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AdviceController.class);
    
    @Autowired
    HealthService HEALTH_SERVICE;


    @ModelAttribute
    public void advice(HttpServletRequest request, Model model, HttpServletResponse response) throws IOException {
        final String currentUrl = request.getRequestURI();
        model.addAttribute("currentUrl", currentUrl);
        
        final boolean serviceIsDown = !HEALTH_SERVICE.isUp();
        model.addAttribute("serviceIsDown", serviceIsDown);
        
        // Redirection si service est down et pas déjà sur une page d'authentification
        if (serviceIsDown && !currentUrl.startsWith("/public/authentication/login")) {
            LOGGER.warn(">>> <<<< Service is down - HttpSession.invalidate() then redirecting to /public/authentication/login");
            response.sendRedirect("/private/authentication/logout");
            return;
        }
    }
}