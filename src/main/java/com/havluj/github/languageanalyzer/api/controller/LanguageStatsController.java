package com.havluj.github.languageanalyzer.api.controller;

import com.havluj.github.languageanalyzer.api.dto.LanguageStatsDto;
import com.havluj.github.languageanalyzer.logic.ComputeLanguageLogic;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@Slf4j
public class LanguageStatsController {

    @Autowired
    ComputeLanguageLogic languageLogic;

    @GetMapping("/org/{company}/languages")
    public LanguageStatsDto getLanguageStatistics(@NonNull @PathVariable(value = "company") String company) {
        log.info(String.format("Getting language stats for org [%s].", company));
        return languageLogic.computeLanguageStatsForOrg(company);
    }
}
