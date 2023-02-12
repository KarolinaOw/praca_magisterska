package pl.karolinamichalska.logo.logo.data;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class DataService {

    private final DataFileStorage dataFileStorage;

    @Inject
    public DataService(DataFileStorage dataFileStorage) {
        this.dataFileStorage = requireNonNull(dataFileStorage, "dataFileStorage is null");
    }

    public DataFileHandle storeRawData(String data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
            return dataFileStorage.storeData(Stream.of(inputStream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public UploadableDataFileHandle storeDataFile() {
        return dataFileStorage.getSignedUrl();
    }
}
