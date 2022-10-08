package com.havluj.github.languageanalyzer.api.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
public class LanguageStats {
    @NonNull
    private final Map<String, Double> languageMap;

    // to flatten out the serialized response, since @JsonUnwrapped doesn't work on maps: https://stackoverflow.com/a/18043785
    @JsonAnyGetter
    public Map<String, Double> getLanguageMap() {
        return languageMap;
    }
}
