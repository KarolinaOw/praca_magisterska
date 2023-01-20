package pl.karolinamichalska.logo.server;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("api/health")
public class HealthResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getHealth() {
        return "OK\n";
    }
}
