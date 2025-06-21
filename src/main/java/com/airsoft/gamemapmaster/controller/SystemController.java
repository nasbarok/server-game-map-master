package com.airsoft.gamemapmaster.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private Environment environment;

    @GetMapping("/env")
    public Map<String, String> getEnvironmentInfo() {
        Map<String, String> envInfo = new HashMap<>();
        envInfo.put("activeProfile", environment.getActiveProfiles().length > 0 ?
                environment.getActiveProfiles()[0] : "default");
        envInfo.put("serverPort", environment.getProperty("server.port"));
        envInfo.put("databaseUrl", environment.getProperty("spring.datasource.url"));
        // Ne pas inclure d'informations sensibles comme les mots de passe
        return envInfo;
    }
}