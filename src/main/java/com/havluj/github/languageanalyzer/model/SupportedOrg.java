package com.havluj.github.languageanalyzer.model;

import com.google.common.collect.Maps;
import com.havluj.github.languageanalyzer.exceptions.OrgNotFoundException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public enum SupportedOrg {

    PRODUCTBOARD("productboard"),
    DN("deepnote-classroom");

    private static final Map<String, SupportedOrg> LOOKUP = Maps.uniqueIndex(
            Arrays.asList(SupportedOrg.values()),
            SupportedOrg::getOrgName
    );

    @Getter
    private final String orgName;

    public static SupportedOrg fromName(@NonNull final String name) {
        final String lCaseName = name.toLowerCase();
        if (LOOKUP.containsKey(lCaseName)) {
            return LOOKUP.get(lCaseName);
        } else {
            final String message = String.format("Organization [%s] not supported", name);
            log.error(message);
            throw new OrgNotFoundException(message);
        }
    }
}
