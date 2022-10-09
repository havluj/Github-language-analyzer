package com.havluj.github.languageanalyzer.model;

import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
public class LanguageStats {
    @NonNull
    private final Map<String, String> languageMap;
}
