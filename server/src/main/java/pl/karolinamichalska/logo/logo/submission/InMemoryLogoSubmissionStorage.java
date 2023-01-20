package pl.karolinamichalska.logo.logo.submission;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryLogoSubmissionStorage implements LogoSubmissionStorage {

    private final Map<Long, LogoSubmission> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public LogoSubmission store(LogoSubmission logoSubmission) {
        return storage.compute(
                logoSubmission.id().orElseGet(idSequence::getAndIncrement),
                (id, ignored) -> logoSubmission.withId(id));
    }

    @Override
    public Optional<LogoSubmission> findByRequestId(long logoRequestId) {
        return storage.values().stream()
                .filter(val -> val.logoRequestId() == logoRequestId)
                .findFirst();
    }
}
