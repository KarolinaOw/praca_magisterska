package pl.karolinamichalska.logo.logo.data;

import static java.util.Objects.requireNonNull;

public record UploadableDataFileHandle(String fileId, String signedUrl) {

    public UploadableDataFileHandle {
        requireNonNull(fileId, "fileId is null");
        requireNonNull(signedUrl, "signedUrl is null");
    }
}
