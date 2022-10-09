package com.havluj.github.languageanalyzer.controller;

import com.havluj.github.languageanalyzer.logic.LanguageStatsLogic;
import com.havluj.github.languageanalyzer.model.SupportedOrg;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class LanguageStatsController {

    @Autowired
    LanguageStatsLogic languageStatsLogic;

    @GetMapping("/org/{org}/languages")
    public Map<String, String> getLanguageStatistics(@NonNull @PathVariable(value = "org") String org) {
        log.info(String.format("Serving /org/%s/languages.", org));
        SupportedOrg supportedOrg = SupportedOrg.fromName(org);
        return languageStatsLogic.getLanguageStatsForOrg(supportedOrg).getLanguageMap();
    }
}
