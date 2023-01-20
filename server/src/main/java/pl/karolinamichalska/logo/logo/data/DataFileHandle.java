package pl.karolinamichalska.logo.logo.data;

import static java.util.Objects.requireNonNull;

public record DataFileHandle(String fileId) {

    public DataFileHandle {
        requireNonNull(fileId, "fileId is null");
    }

    public static DataFileHandle of(String fileId) {
        return new DataFileHandle(fileId);
    }
}
