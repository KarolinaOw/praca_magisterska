package pl.karolinamichalska.logo.logo.request;

import java.util.Optional;
import java.util.Set;

public interface LogoRequestStorage {

    LogoRequest store(LogoRequest logoRequest);

    Optional<LogoRequest> find(long id);

    Set<LogoRequest> findAll(Set<LogoRequestStatus> statuses);
}
