package com.kould.entity;

import java.util.regex.Pattern;

public class RegexEntity {

    private final Pattern selectRegexPattern;

    private final Pattern insertRegexPattern;

    private final Pattern deleteRegexPattern;

    private final Pattern updateRegexPattern;

    private final Pattern selectStatusByIdRegexPattern;

    public RegexEntity(String selectRegex, String insertRegex, String deleteRegex, String updateRegex, String selectStatusByIdRegex) {
        this.selectRegexPattern = Pattern.compile(selectRegex);
        this.insertRegexPattern = Pattern.compile(insertRegex);
        this.deleteRegexPattern = Pattern.compile(deleteRegex);
        this.updateRegexPattern = Pattern.compile(updateRegex);
        this.selectStatusByIdRegexPattern = Pattern.compile(selectStatusByIdRegex);
    }

    public boolean selectRegexPatternMatch(String methodName) {
        return selectRegexPattern.matcher(methodName).lookingAt();
    }

    public boolean insertRegexPatternMatch(String methodName) {
        return insertRegexPattern.matcher(methodName).lookingAt();
    }

    public boolean deleteRegexPatternMatch(String methodName) {
        return deleteRegexPattern.matcher(methodName).lookingAt();
    }

    public boolean updateRegexPatternMatch(String methodName) {
        return updateRegexPattern.matcher(methodName).lookingAt();
    }

    public boolean selectStatusByIdRegexPatternMatch(String methodName) {
        return selectStatusByIdRegexPattern.matcher(methodName).lookingAt();
    }
}
