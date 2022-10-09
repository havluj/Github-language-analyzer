package com.havluj.github.languageanalyzer.api.controller;

import com.havluj.github.languageanalyzer.BaseTest;
import com.havluj.github.languageanalyzer.exceptions.GitHubIoErrorException;
import com.havluj.github.languageanalyzer.logic.LanguageStatsLogic;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class LanguageStatsControllerTest extends BaseTest {

    @MockBean
    private LanguageStatsLogic languageStatsLogicMock;

    @Autowired
    private MockMvc mvc;

    @Test
    public void testUnknownOrg() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/org/invalid/languages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        // We can't test for error messages here because Springs' error handling is based on Servlet container error
        // mappings and those are different/not present in MockMvc. We would likely want to cover this in integration
        // tests anyway.

        //.andExpect(jsonPath("$.status", is(400)))
        //.andExpect(jsonPath("$.error", is("Bad Request")))
        //.andExpect(jsonPath("$.message", is("Organization not found.")));
    }

    @Test
    void testGithubIoException() throws Exception {
        Mockito.when(languageStatsLogicMock.getLanguageStatsForOrg(Mockito.any()))
                .thenThrow(GitHubIoErrorException.class);

        mvc.perform(MockMvcRequestBuilders.get("/org/productboard/languages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testHappyCase() throws Exception {
        Mockito.when(languageStatsLogicMock.getLanguageStatsForOrg(Mockito.any()))
                .thenReturn(LANGUAGE_STATS);

        mvc.perform(MockMvcRequestBuilders.get("/org/productboard/languages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Java", is("0.40")))
                .andExpect(jsonPath("$.Typescript", is("0.60")));
    }

    @Test
    void testEmptyLanguageCase() throws Exception {
        Mockito.when(languageStatsLogicMock.getLanguageStatsForOrg(Mockito.any()))
                .thenReturn(EMPTY_LANGUAGE_STATS);

        mvc.perform(MockMvcRequestBuilders.get("/org/productboard/languages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }
}