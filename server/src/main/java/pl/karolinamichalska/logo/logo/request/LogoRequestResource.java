package pl.karolinamichalska.logo.logo.request;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("api/logo-requests")
public class LogoRequestResource {

    private final LogoRequestService logoRequestService;
    private final LogoRequestStorage logoRequestStorage;

    @Inject
    public LogoRequestResource(LogoRequestService logoRequestService,
                               LogoRequestStorage logoRequestStorage) {
        this.logoRequestService = requireNonNull(logoRequestService, "logoRequestService is null");
        this.logoRequestStorage = requireNonNull(logoRequestStorage, "logoRequestStorage is null");
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public LogoRequest createLogoRequest(LogoRequestInput input) {
        return logoRequestService.create(input.fileId(), input.params());
    }

    @GET()
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public LogoRequest getLogoRequest(@PathParam("id") long id) {
        return logoRequestStorage.find(id)
                .orElseThrow(() -> new NotFoundException("Logo request with id %s not found".formatted(id)));
    }
}
