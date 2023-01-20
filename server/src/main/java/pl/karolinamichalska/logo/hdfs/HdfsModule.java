package pl.karolinamichalska.logo.hdfs;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.web.WebHdfsFileSystem;

import java.io.IOException;
import java.net.URI;

public class HdfsModule extends AbstractModule {
    private static final String NAMENODE_HOST = "localhost";
    private static final int WEBHDFS_PORT = 50070;

    @Provides
    @Singleton
    @WebHdfs
    public FileSystem webHdfsFileSystem(@WebHdfsConfig Configuration configuration) throws IOException, InterruptedException {
        FileSystem fileSystem = WebHdfsFileSystem.get(
                URI.create("webhdfs://%s:%s".formatted(NAMENODE_HOST, WEBHDFS_PORT)),
                configuration,
                "admin");
        return fileSystem;
    }

    @Provides
    @Singleton
    @WebHdfsConfig
    public Configuration hdfsConfiguration() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "webhdfs://%s:%s".formatted(NAMENODE_HOST, WEBHDFS_PORT));
        return configuration;
    }
}
