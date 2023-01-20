package pl.karolinamichalska.logo.logo.data;


import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pl.karolinamichalska.logo.logo.request.LogoRequest;
import pl.karolinamichalska.logo.logo.request.LogoRequestStorage;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("api/data")
public class DataResource {

    private final DataService dataService;
    private final LogoRequestStorage logoRequestStorage;
    private final DataFileStorage dataFileStorage;

    @Inject
    public DataResource(DataService dataService, LogoRequestStorage logoRequestStorage, DataFileStorage dataFileStorage) {
        this.dataService = requireNonNull(dataService, "dataService is null");
        this.logoRequestStorage = requireNonNull(logoRequestStorage, "logoRequestStorage is null");
        this.dataFileStorage = requireNonNull(dataFileStorage, "dataFileStorage is null");
    }

    @POST
    @Path("raw")
    @Consumes(TEXT_PLAIN)
    @Produces(APPLICATION_JSON)
    public DataFileHandle uploadRawData(String data) {
        return dataService.storeRawData(data);
    }

    @POST
    @Path("file")
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    public DataFileHandle uploadDataFile(MultipartFormDataInput input) {

        Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        List<InputPart> file = formDataMap.get("file");
        checkArgument(file != null && !file.isEmpty(), "Uploaded file cannot be empty");

        return dataService.storeDataFile(input.getParts().stream().map(DataResource::toInputStream));
    }

    @GET
    @Path("{id}/output")
    @Produces("text/csv")
    public String getOutputData(@PathParam("id") long logoRequestId) {
        LogoRequest logoRequest = logoRequestStorage.find(logoRequestId)
                .orElseThrow(() -> new NotFoundException("Logo Request not found"));
        InputStream dataInput = dataFileStorage.getOutputData(DataFileHandle.of(logoRequest.fileId()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(dataInput, StandardCharsets.UTF_8));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    private static InputStream toInputStream(InputPart inputPart) {
        try {
            return inputPart.getBody(InputStream.class, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
