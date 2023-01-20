package pl.karolinamichalska.logo.logo.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.karolinamichalska.logo.logo.data.DataFileHandle;
import pl.karolinamichalska.logo.logo.data.DataFileStorage;
import pl.karolinamichalska.logo.logo.submission.LogoSubmissionService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class LogoRequestService {

    private static final Logger log = LoggerFactory.getLogger(LogoRequestService.class);

    private final LogoRequestStorage storage;
    private final DataFileStorage dataFileStorage;
    private final LogoSubmissionService logoSubmissionService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public LogoRequestService(LogoRequestStorage storage, DataFileStorage dataFileStorage, LogoSubmissionService logoSubmissionService) {
        this.storage = requireNonNull(storage, "storage is null");
        this.dataFileStorage = requireNonNull(dataFileStorage, "dataFileStorage is null");
        this.logoSubmissionService = requireNonNull(logoSubmissionService, "logoSubmissionService is null");
    }

    @PostConstruct
    public void start() {
        executor.submit(this::submitNewJobs);
    }

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
        try {
            if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public LogoRequest create(String fileId, LogoParams logoParams) {
        checkArgument(dataFileStorage.exists(DataFileHandle.of(fileId)),
                "File with ID %s not found".formatted(fileId));
        LogoRequest logoRequest = new LogoRequest(Optional.empty(), fileId, logoParams, LogoRequestStatus.NEW);
        return storage.store(logoRequest);
    }

    private void submitNewJobs() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (LogoRequest newRequest : storage.findAll(Set.of(LogoRequestStatus.NEW))) {
                    logoSubmissionService.submit(newRequest);
                    storage.store(newRequest.withStatus(LogoRequestStatus.PENDING));
                }
            } catch (Exception e) {
                log.warn("Exception thrown in job submitting thread", e);
                throw e;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
