package pl.karolinamichalska.logo.logo.submission;

import java.util.Optional;

public record LogoSubmission(
        Optional<Long> id,
        long logoRequestId,
        String jobId,
        Optional<String> outputFilePath,
        Optional<String> logoFilePath) {

    public LogoSubmission withId(long id) {
        return new LogoSubmission(Optional.of(id), logoRequestId, jobId, outputFilePath, logoFilePath);
    }

    public LogoSubmission withOutputFilePath(String filepath) {
        return new LogoSubmission(id, logoRequestId, jobId, Optional.of(filepath), logoFilePath);
    }

    public LogoSubmission withLogoFilePath(String filepath) {
        return new LogoSubmission(id, logoRequestId, jobId, outputFilePath, Optional.of(filepath));
    }
}
