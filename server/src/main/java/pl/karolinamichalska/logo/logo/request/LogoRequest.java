package pl.karolinamichalska.logo.logo.request;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

public record LogoRequest(
        Optional<Long> id,
        String fileId,
        LogoParams params,
        LogoRequestStatus status
) {

    public LogoRequest withId(long id) {
        return new LogoRequest(Optional.of(id), fileId, params, status);
    }

    public LogoRequest withStatus(LogoRequestStatus status) {
        return new LogoRequest(id, fileId, params, status);
    }
}
