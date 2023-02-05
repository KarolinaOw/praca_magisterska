package pl.karolinamichalska.logo.logo.data;

import org.apache.commons.lang3.NotImplementedException;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class TempDataFileStorage implements DataFileStorage {

    private final Path tempDir;
    private final Set<String> fileIds = Collections.synchronizedSet(new HashSet<>());

    @Inject
    public TempDataFileStorage() throws IOException {
        this.tempDir = Files.createTempDirectory("logo-data-files-");
    }

    @Override
    public DataFileHandle storeData(Stream<InputStream> inputStreams) {
        try {
            String fileId = UUID.randomUUID().toString();
            fileIds.add(fileId);
            Path filepath = Files.createFile(tempDir.resolve(fileId));
            try (BufferedOutputStream outputStream = new BufferedOutputStream(
                    new FileOutputStream(filepath.toFile(), true))) {
                inputStreams.forEach(is -> {
                    try {
                        is.transferTo(outputStream);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
            return DataFileHandle.of(fileId);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getPath(DataFileHandle handle) {
        checkArgument(exists(handle), "File not found");
        return tempDir.resolve(handle.fileId()).toString();
    }

    @Override
    public String getOutputFilePath(DataFileHandle handle) {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getOutputData(DataFileHandle handle) {
        throw new NotImplementedException();
    }

    @Override
    public boolean exists(DataFileHandle handle) {
        return fileIds.contains(handle.fileId());
    }

    @Override
    public void storeLogo(DataFileHandle handle, InputStream inputStream) {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getLogo(DataFileHandle handle) {
        throw new NotImplementedException();
    }

    @Override
    public String getLogoFilePath(DataFileHandle handle) {
        throw new NotImplementedException();
    }
}
