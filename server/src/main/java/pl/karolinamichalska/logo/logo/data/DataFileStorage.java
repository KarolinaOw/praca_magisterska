package pl.karolinamichalska.logo.logo.data;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface DataFileStorage {

    /**
     * Stores data in a file.
     */
    DataFileHandle storeData(Stream<InputStream> inputStreams);

    UploadableDataFileHandle getSignedUrl();

    String getPath(DataFileHandle handle);

    String getOutputFilePath(DataFileHandle handle);

    InputStream getOutputData(DataFileHandle handle);

    boolean outputExists(DataFileHandle handle);

    void storeLogo(DataFileHandle handle, InputStream inputStream);

    InputStream getLogo(DataFileHandle handle);

    String getLogoFilePath(DataFileHandle handle);

    boolean exists(DataFileHandle handle);
}
