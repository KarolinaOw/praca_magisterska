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

    String getPath(DataFileHandle handle);

    String getOutputFilePath(DataFileHandle handle);

    InputStream getOutputData(DataFileHandle handle);

    boolean exists(DataFileHandle handle);
}
