package pl.karolinamichalska.logo.logo.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

public class RdbmsLogoRequestStorage implements LogoRequestStorage {

    private final Jdbi jdbi;
    private final ObjectMapper objectMapper;

    @Inject
    public RdbmsLogoRequestStorage(Jdbi jdbi, ObjectMapper objectMapper) {
        this.jdbi = requireNonNull(jdbi);
        this.objectMapper = requireNonNull(objectMapper);
    }

    @Override
    public LogoRequest store(LogoRequest logoRequest) {
        return jdbi.withHandle(handle -> {
            Update update = handle.createUpdate("""
                            INSERT INTO logo_request (file_id, params, status)
                            VALUES (:fileId, :params, :status)
                            ON CONFLICT (file_id)
                            DO UPDATE SET params = :params, status = :status
                            """)
                    .bind("fileId", logoRequest.fileId())
                    .bind("params", serializeParams(logoRequest))
                    .bind("status", logoRequest.status().name());
            if (logoRequest.id().isEmpty()) {
                Long id = update.executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .collect(MoreCollectors.onlyElement());
                return logoRequest.withId(id);
            }
            int updatedCount = update.execute();
            Preconditions.checkState(updatedCount == 1);
            return logoRequest;
        });
    }


    @Override
    public Optional<LogoRequest> find(long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM logo_request WHERE id = :id")
                        .bind("id", id)
                        .map(new LogoRequestRowMapper(objectMapper))
                        .findOne());
    }

    @Override
    public Set<LogoRequest> findAll(Set<LogoRequestStatus> statuses) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM logo_request WHERE status IN (<statuses>)")
                        .bindList("statuses", statuses)
                        .map(new LogoRequestRowMapper(objectMapper))
                        .stream()
                        .collect(toImmutableSet()));
    }

    private String serializeParams(LogoRequest logoRequest) {
        try {
            return objectMapper.writeValueAsString(logoRequest.params());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LogoRequestRowMapper implements RowMapper<LogoRequest> {

        private final ObjectMapper objectMapper;

        public LogoRequestRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public LogoRequest map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new LogoRequest(
                    Optional.of(rs.getLong("id")),
                    rs.getString("file_id"),
                    deserializeParams(rs.getString("params")),
                    LogoRequestStatus.valueOf(rs.getString("status"))
            );
        }

        private LogoParams deserializeParams(String json) {
            try {
                return objectMapper.readValue(json, LogoParams.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
