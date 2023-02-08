package pl.karolinamichalska.logo.googlecloud;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class GoogleCloudModule extends AbstractModule {

    private static final String DB_NAME = "logo";

    @Provides
    @Singleton
    public Storage googleStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Provides
    @Singleton
    public DataSource dataSource() {
        // Set up URL parameters
        String jdbcURL = String.format("jdbc:postgresql:///%s", DB_NAME);
        Properties connProps = new Properties();
        connProps.setProperty("user", System.getenv("DB_USER"));
        connProps.setProperty("password", System.getenv("DB_PASS"));
        connProps.setProperty("sslmode", "disable");
        connProps.setProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
        connProps.setProperty("cloudSqlInstance", "amplified-bee-374812:europe-central2:logo-seq-creator");
        connProps.setProperty("enableIamAuth", "true");

        // Initialize connection pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcURL);
        config.setDataSourceProperties(connProps);
        config.setConnectionTimeout(10000); // 10s

        return new HikariDataSource(config);
    }
}
