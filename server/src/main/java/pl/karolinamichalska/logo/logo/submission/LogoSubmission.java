package pl.karolinamichalska.logo.logo.submission;

import java.util.Optional;

public record LogoSubmission(
        Optional<Long> id,
        long logoRequestId,
        String jobId,
        Optional<String> outputFilePath) {

    public LogoSubmission withId(long id) {
        return new LogoSubmission(Optional.of(id), logoRequestId, jobId, outputFilePath);
    }

    public LogoSubmission withOutputFilePath(String filepath) {
        return new LogoSubmission(id, logoRequestId, jobId, Optional.of(filepath));
    }
}
