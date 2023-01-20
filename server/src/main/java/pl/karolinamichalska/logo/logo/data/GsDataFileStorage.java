package pl.karolinamichalska.logo.logo.data;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GsDataFileStorage implements DataFileStorage {

    private static final String BUCKET_NAME = "logo-seq-creator";
    private static final Path PATH_PREFIX = Path.of("logo-requests", "input-files");
    private static final Path OUTPUT_PATH_PREFIX = Path.of("logo-requests", "output");

    private final Storage storage;

    @Inject
    public GsDataFileStorage(Storage storage) {
        this.storage = requireNonNull(storage, "storage is null");
    }

    @Override
    public DataFileHandle storeData(Stream<InputStream> inputStreams) {
        String fileId = UUID.randomUUID().toString();
        Blob blob = storage.create(
                BlobInfo.newBuilder(BUCKET_NAME, PATH_PREFIX.resolve(fileId).toString())
                        .build());
        try (OutputStream output = Channels.newOutputStream((blob.writer()))) {
            inputStreams.forEach(is -> {
                try {
                    is.transferTo(output);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return DataFileHandle.of(fileId);
    }

    @Override
    public String getPath(DataFileHandle handle) {
        return "gs://%s/%s".formatted(BUCKET_NAME, PATH_PREFIX.resolve(handle.fileId()));
    }

    @Override
    public String getOutputFilePath(DataFileHandle handle) {
        return "gs://%s/%s".formatted(BUCKET_NAME, OUTPUT_PATH_PREFIX.resolve(handle.fileId()));
    }

    @Override
    public InputStream getOutputData(DataFileHandle handle) {
        return Channels.newInputStream(storage.get(
                        BlobId.of(BUCKET_NAME, OUTPUT_PATH_PREFIX.resolve(handle.fileId()).toString()))
                .reader());
    }

    @Override
    public boolean exists(DataFileHandle handle) {
        return storage.get(BlobId.of(BUCKET_NAME, PATH_PREFIX.resolve(handle.fileId()).toString())).exists();
    }
}
