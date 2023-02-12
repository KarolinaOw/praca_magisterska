package pl.karolinamichalska.logo.logo.submission;

import com.google.cloud.dataproc.v1.Job;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.karolinamichalska.logo.lock.LockManager;
import pl.karolinamichalska.logo.logo.data.DataFileHandle;
import pl.karolinamichalska.logo.logo.data.DataFileStorage;
import pl.karolinamichalska.logo.logo.request.LogoRequest;
import pl.karolinamichalska.logo.logo.request.LogoRequestStatus;
import pl.karolinamichalska.logo.logo.request.LogoRequestStorage;
import pl.karolinamichalska.logo.spark.SparkJobService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

public class LogoSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(LogoSubmissionService.class);

    private final LogoSubmissionStorage storage;
    private final SparkJobService sparkJobService;
    private final DataFileStorage dataFileStorage;
    private final LogoRequestStorage logoRequestStorage;
    private final LockManager lockManager;
    private final Set<LogoSubmission> trackedLogoSubmissions = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public LogoSubmissionService(LogoSubmissionStorage storage,
                                 SparkJobService sparkJobService,
                                 DataFileStorage dataFileStorage,
                                 LogoRequestStorage logoRequestStorage,
                                 LockManager lockManager) {
        this.storage = requireNonNull(storage, "storage is null");
        this.sparkJobService = requireNonNull(sparkJobService, "sparkJobService is null");
        this.dataFileStorage = requireNonNull(dataFileStorage, "dataFileStorage is null");
        this.logoRequestStorage = requireNonNull(logoRequestStorage, "logoRequestStorage is null");
        this.lockManager = requireNonNull(lockManager, "lockManager is null");
    }

    @PostConstruct
    public void start() {
        trackedLogoSubmissions.addAll(logoRequestStorage.findAll(
                        ImmutableSet.of(LogoRequestStatus.PENDING, LogoRequestStatus.IN_PROGRESS)).stream()
                .map(request -> storage.findByRequestId(request.id().orElseThrow()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toImmutableSet()));
        executor.submit(this::trackLogoSubmissions);
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

    public void submit(LogoRequest logoRequest) {
        Map<String, String> args = new HashMap<>();
        args.put("--filename", dataFileStorage.getPath(DataFileHandle.of(logoRequest.fileId())));
        String jobId = sparkJobService.submitJob(args);
        LogoSubmission logoSubmission = storage.store(new LogoSubmission(
                Optional.empty(),
                logoRequest.id().orElseThrow(),
                jobId,
                Optional.empty(),
                Optional.empty()));
        trackedLogoSubmissions.add(logoSubmission);
    }

    private void trackLogoSubmissions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (LogoSubmission logoSubmission : ImmutableSet.copyOf(trackedLogoSubmissions)) {
                    Boolean removeSubmission = lockManager.performLockedOrIgnore(logoSubmission.jobId(), () -> {
                        Job job = sparkJobService.getJob(logoSubmission.jobId());
                        Function<LogoRequest, LogoRequest> transformer =
                                switch (job.getStatus().getState()) {
                                    case STATE_UNSPECIFIED -> Function.identity();
                                    case PENDING, SETUP_DONE -> req -> req.withStatus(LogoRequestStatus.PENDING);
                                    case RUNNING, CANCEL_PENDING, CANCEL_STARTED ->
                                            req -> req.withStatus(LogoRequestStatus.IN_PROGRESS);
                                    case CANCELLED, DONE, ERROR, ATTEMPT_FAILURE ->
                                            req -> req.withStatus(LogoRequestStatus.FINISHED);
                                    case UNRECOGNIZED -> throw new IllegalArgumentException("Unrecognized status");
                                };
                        Optional<LogoRequest> logoRequest = logoRequestStorage.find(logoSubmission.logoRequestId())
                                .map(transformer);

                        boolean shouldRemove = false;
                        if (logoRequest.isEmpty()) {
                            shouldRemove = true;
                        }

                        if (logoRequest.isPresent() && logoRequest.get().status().equals(LogoRequestStatus.FINISHED)) {
                            DataFileHandle fileHandle = DataFileHandle.of(logoRequest.get().fileId());
                            if (dataFileStorage.outputExists(fileHandle)) {
                                String outputFilePath = dataFileStorage.getOutputFilePath(fileHandle);
                                String formattedLogoFilePath = formatLogo(fileHandle);
                                storage.store(logoSubmission.withOutputFilePath(outputFilePath).withLogoFilePath(formattedLogoFilePath));
                                shouldRemove = true;
                            }
                        }

                        logoRequest.ifPresent(logoRequestStorage::store);

                        return shouldRemove;
                    });

                    if (removeSubmission != null && removeSubmission) {
                        trackedLogoSubmissions.remove(logoSubmission);
                    }
                }
            } catch (Exception e) {
                log.warn("Exception thrown in submission tracking thread", e);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String formatLogo(DataFileHandle handle) {
        try (InputStream outputData = dataFileStorage.getOutputData(handle)) {
            try (ByteArrayOutputStream logoOutStream = new ByteArrayOutputStream()) {

                String params = ";";
                // TODO params
                try (SequenceInputStream sequenceInputStream = new SequenceInputStream(
                        new ByteArrayInputStream(params.getBytes(StandardCharsets.UTF_8)),
                        outputData)) {
                    PumpStreamHandler streamHandler = new PumpStreamHandler(logoOutStream, System.err, sequenceInputStream);

                    DefaultExecutor executor = new DefaultExecutor();
                    executor.setStreamHandler(streamHandler);

                    CommandLine cmd = CommandLine.parse("python3 -m logoformatter");
                    int exitCode;
                    try {
                        exitCode = executor.execute(cmd);
                    } catch (ExecuteException e) {
                        throw new IllegalStateException(
                                "Logo formatter failed, stdout: %s"
                                        .formatted(logoOutStream.toString()),
                                e);
                    }
                    if (exitCode != 0) {
                        throw new IllegalStateException();
                    }

                    try (ByteArrayInputStream logoInputStream = new ByteArrayInputStream(logoOutStream.toByteArray())) {
                        dataFileStorage.storeLogo(handle, logoInputStream);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return dataFileStorage.getLogoFilePath(handle);
    }
}
