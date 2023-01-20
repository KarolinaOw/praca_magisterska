package pl.karolinamichalska.logo.logo.request;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class InMemoryLogoRequestStorage implements LogoRequestStorage {

    private final Map<Long, LogoRequest> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public LogoRequest store(LogoRequest logoRequest) {
        return storage.compute(
                logoRequest.id().orElseGet(idSequence::getAndIncrement),
                (id, ignored) -> logoRequest.withId(id));
    }

    @Override
    public Optional<LogoRequest> find(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Set<LogoRequest> findAll(Set<LogoRequestStatus> statuses) {
        return storage.values().stream()
                .filter(request -> statuses.stream().anyMatch(status -> request.status().equals(status)))
                .collect(toImmutableSet());
    }
}
