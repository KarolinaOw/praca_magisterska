package pl.karolinamichalska.logo.logo.submission;

import com.google.common.base.Preconditions;
import com.google.common.collect.MoreCollectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.Update;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RdbmsLogoSubmissionStorage implements LogoSubmissionStorage {

    private final Jdbi jdbi;

    @Inject
    public RdbmsLogoSubmissionStorage(Jdbi jdbi) {
        this.jdbi = requireNonNull(jdbi);
    }

    @Override
    public LogoSubmission store(LogoSubmission logoSubmission) {
        return jdbi.withHandle(handle -> {
            Update update = handle.createUpdate("""
                            INSERT INTO logo_submission (logo_request_id, job_id, output_file_path, logo_file_path)
                            VALUES (:logoRequestId, :jobId, :outputFilePath, :logoFilePath)
                            ON CONFLICT (logo_request_id)
                            DO UPDATE SET output_file_path = :outputFilePath, logo_file_path = :logoFilePath
                            """)
                    .bind("logoRequestId", logoSubmission.logoRequestId())
                    .bind("jobId", logoSubmission.jobId())
                    .bind("outputFilePath", logoSubmission.outputFilePath())
                    .bind("logoFilePath", logoSubmission.logoFilePath());
            if (logoSubmission.id().isEmpty()) {
                Long id = update.executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .collect(MoreCollectors.onlyElement());
                return logoSubmission.withId(id);
            }
            int updatedCount = update.execute();
            Preconditions.checkState(updatedCount == 1);
            return logoSubmission;
        });
    }

    @Override
    public Optional<LogoSubmission> findByRequestId(long logoRequestId) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM logo_submission WHERE logo_request_id = :logoRequestId")
                .bind("logoRequestId", logoRequestId)
                .map(new LogoSubmissionRowMapper())
                .findOne());
    }

    private static class LogoSubmissionRowMapper implements RowMapper<LogoSubmission> {

        @Override
        public LogoSubmission map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new LogoSubmission(
                    Optional.of(rs.getLong("id")),
                    rs.getLong("logo_request_id"),
                    rs.getString("job_id"),
                    Optional.ofNullable(rs.getString("output_file_path")),
                    Optional.ofNullable(rs.getString("logo_file_path"))
            );
        }
    }
}
