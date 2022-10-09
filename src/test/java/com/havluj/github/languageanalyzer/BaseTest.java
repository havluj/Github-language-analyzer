package com.havluj.github.languageanalyzer;

import com.havluj.github.languageanalyzer.model.LanguageStats;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class BaseTest {

    protected static final String JAVA = "Java";
    protected static final String TYPESCRIPT = "Typescript";
    protected static final Map<String, Long> REPO_STAT_1 = Map.of(JAVA, 20l, TYPESCRIPT, 60l);
    protected static final Map<String, Long> REPO_STAT_2 = Map.of(JAVA, 20l);
    protected static final Map<String, String> LANGUAGE_STAT_MAP = Map.of(JAVA, "0.40", TYPESCRIPT, "0.60");
    protected static final LanguageStats LANGUAGE_STATS = new LanguageStats(LANGUAGE_STAT_MAP);
    protected static final Map<String, String> EMPTY_LANGUAGE_STAT_MAP = Map.of();

    protected static final LanguageStats EMPTY_LANGUAGE_STATS = new LanguageStats(EMPTY_LANGUAGE_STAT_MAP);

}
