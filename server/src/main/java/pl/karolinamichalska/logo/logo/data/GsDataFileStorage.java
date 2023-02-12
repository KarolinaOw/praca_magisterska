package pl.karolinamichalska.logo.logo.data;

import com.google.cloud.storage.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GsDataFileStorage implements DataFileStorage {

    private static final String BUCKET_NAME = "logo-seq-creator";
    private static final Path PATH_PREFIX = Path.of("logo-requests", "input-files");
    private static final Path OUTPUT_PATH_PREFIX = Path.of("logo-requests", "output");
    private static final Path LOGO_PATH_PREFIX = Path.of("logo-requests", "logos");

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
    public UploadableDataFileHandle getSignedUrl() {
        String fileId = UUID.randomUUID().toString();
        Blob blob = storage.create(
                BlobInfo.newBuilder(BUCKET_NAME, PATH_PREFIX.resolve(fileId).toString())
                        .build());
        URL url = blob.signUrl(60 * 10, TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT));
        return new UploadableDataFileHandle(fileId, url.toString());
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
    public boolean outputExists(DataFileHandle handle) {
        Blob blob = storage.get(BlobId.of(BUCKET_NAME, OUTPUT_PATH_PREFIX.resolve(handle.fileId()).toString()));
        return blob != null && blob.exists();
    }

    @Override
    public boolean exists(DataFileHandle handle) {
        Blob blob = storage.get(BlobId.of(BUCKET_NAME, PATH_PREFIX.resolve(handle.fileId()).toString()));
        return blob != null && blob.exists();
    }

    @Override
    public void storeLogo(DataFileHandle handle, InputStream inputStream) {
        Blob blob = storage.create(
                BlobInfo.newBuilder(BUCKET_NAME, LOGO_PATH_PREFIX.resolve(handle.fileId()).toString())
                        .build());
        try (OutputStream output = Channels.newOutputStream((blob.writer()))) {
            inputStream.transferTo(output);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream getLogo(DataFileHandle handle) {
        return Channels.newInputStream(storage.get(
                        BlobId.of(BUCKET_NAME, LOGO_PATH_PREFIX.resolve(handle.fileId()).toString()))
                .reader());
    }

    @Override
    public String getLogoFilePath(DataFileHandle handle) {
        return "gs://%s/%s".formatted(BUCKET_NAME, LOGO_PATH_PREFIX.resolve(handle.fileId()));
    }
}
