package pl.karolinamichalska.logo.lock;

import com.google.cloud.storage.*;

import javax.inject.Inject;
import java.util.function.Supplier;

public class GcsLockManager implements LockManager {

    private static final String BUCKET = "logo-seq-creator";

    private final Storage storage;

    @Inject
    public GcsLockManager(Storage storage) {
        this.storage = storage;
    }

    @Override
    public <T> T performLockedOrIgnore(String key, Supplier<T> action) {
        try {
            Blob blob = storage.create(
                    BlobInfo.newBuilder(BlobId.of(BUCKET, "locks/%s".formatted(key), 0L))
                            .setCacheControl("no-store")
                            .build(),
                    Storage.BlobTargetOption.generationMatch());
            T result;
            try {
                result = action.get();
            } finally {
                blob.delete();
            }
            return result;
        } catch (StorageException e) {
            return null;
        }
    }
}
