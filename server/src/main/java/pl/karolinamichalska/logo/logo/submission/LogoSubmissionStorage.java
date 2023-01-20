package pl.karolinamichalska.logo.logo.submission;

import java.util.Optional;

public interface LogoSubmissionStorage {

    LogoSubmission store(LogoSubmission logoSubmission);

    Optional<LogoSubmission> findByRequestId(long logoRequestId);
}
