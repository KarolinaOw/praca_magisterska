package pl.karolinamichalska.logo.logo.data;

import com.google.cloud.dataproc.v1.ClusterControllerClient;
import com.google.cloud.dataproc.v1.JobControllerClient;
import com.google.inject.AbstractModule;
import pl.karolinamichalska.logo.googlecloud.GoogleCloudModule;
import pl.karolinamichalska.logo.hdfs.HdfsModule;

import static com.google.inject.Scopes.SINGLETON;

public class LogoDataModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new HdfsModule());
        install(new GoogleCloudModule());
        bind(DataFileStorage.class).to(GsDataFileStorage.class).in(SINGLETON);

        bind(DataService.class).in(SINGLETON);

        bind(DataResource.class);
    }
}
