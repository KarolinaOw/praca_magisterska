package pl.karolinamichalska.logo.logo.data;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import pl.karolinamichalska.logo.hdfs.WebHdfs;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class HdfsDataFileStorage implements DataFileStorage {

    private final FileSystem fileSystem;
    private final org.apache.hadoop.fs.Path basePath;

    @Inject
    public HdfsDataFileStorage(@WebHdfs FileSystem fileSystem) {
        this.fileSystem = requireNonNull(fileSystem, "fileSystem is null");
        this.basePath = new org.apache.hadoop.fs.Path("logo-requests/input-files");
    }

    @Override
    public DataFileHandle storeData(Stream<InputStream> inputStreams) {
        String fileId = UUID.randomUUID().toString();
        try (FSDataOutputStream output = fileSystem.createFile(new org.apache.hadoop.fs.Path(getAbsoluteBasePath(), fileId))
                .recursive()
                .build()) {
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
        checkArgument(exists(handle), "File not found");
        try {
            return Path.of(fileSystem.resolvePath(new org.apache.hadoop.fs.Path(getAbsoluteBasePath(), handle.fileId())).toUri()).toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        try {
            return fileSystem.exists(new org.apache.hadoop.fs.Path(getAbsoluteBasePath(), handle.fileId()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private org.apache.hadoop.fs.Path getAbsoluteBasePath() {
        return new org.apache.hadoop.fs.Path(fileSystem.getWorkingDirectory(), basePath);
    }
}
