package com.havluj.github.languageanalyzer.api;

import com.havluj.github.languageanalyzer.api.dto.LanguageStats;
import lombok.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class LanguageStatsController {

    @GetMapping("/org/{company}/languages")
    public LanguageStats getLanguageStatistics(@NonNull @PathVariable(value = "company") String company) {
        return new LanguageStats(Map.of("Javascript", 34.0, "C++", 66.0));
    }
}
