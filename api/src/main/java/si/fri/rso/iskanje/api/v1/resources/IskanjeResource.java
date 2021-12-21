package si.fri.rso.iskanje.api.v1.resources;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.discovery.exceptions.ServiceNotFoundException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import si.fri.rso.iskanje.lib.Polnilnica;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@ConfigBundle("external-api")
@ApplicationScoped
@Path("/iskanje")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IskanjeResource {

    // Dependency on polnilnice microservice
    @Inject
    @DiscoverService(value = "polnilnice-service", environment = "dev", version = "1.0.0")
    private Optional<String> polnilnice_host;


    // Api url for calculating time between coordinates
    @ConfigValue(watch = true)
    private String distanceapi;

    public String getDistanceapi() {
        return this.distanceapi;
    }

    public void setDistanceapi(final String distanceapi) {
        this.distanceapi = distanceapi;
    }


    CloseableHttpClient httpClient = HttpClients.createDefault();
    ObjectMapper mapper = new ObjectMapper();


    /** GET full polnilnice list  with calculated distances**/
    @GET
    @Path("/curr_latlng={curr_lat},{curr_lng}")
    public Response getPolnilnice(@PathParam("curr_lat") Double curr_lat, @PathParam("curr_lng") Double curr_lng) {
        // Default url?? idk
        if (!polnilnice_host.isPresent()) {
            System.out.println("Polnilnice_host unavailable");
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }

        List<Polnilnica> polnilniceList = null;

        // TODO make service discovery actually work lmao
        // Print the ip provided by consul but then override cus it doesn't actually work.
        System.out.println("Consul polnilnice ms is on this ip: " + polnilnice_host.get() );
        polnilnice_host =  Optional.of("http://192.168.99.100:8080");

        // =========== Call polnilnice and get the original list ===========
        String polnilniceString = myHttpGet(polnilnice_host.get() + "/v1/polnilnice", null);
        // Create polnilnice list from API response
        try {
            polnilniceList = mapper.readValue(polnilniceString, new TypeReference<List<Polnilnica>>() {});
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        // =========== Call external api (in groups of 20) ===========
        int i = 0;
        // Shouldn't be < 2 !!!
        int groupSize = 20;

        String jsonAll;
        while (i < polnilniceList.size()) {
            // Create json body for external api
            JSONObject jsonBody = new JSONObject();
            JSONArray latLngArr = new JSONArray();
            // First latlng is the one passed in parameters
            JSONObject currLatLng = new JSONObject()
                    .put("latLng", new JSONObject()
                            .put("lat", curr_lat)
                            .put("lng", curr_lng));
            latLngArr.put(currLatLng);
            // "manyToOne" option so we can query multiple locations at once
            JSONObject options = new JSONObject().put("manyToOne", true);
            jsonBody.put("options", options);

            // Read locations for each polnilnica and build a json array
            for (int j = i; j < polnilniceList.size() && j < i + groupSize; j++) {
                JSONObject latLng = new JSONObject()
                        .put("latLng", new JSONObject()
                            .put("lng", polnilniceList.get(j).getLokacijaLng())
                            .put("lat", polnilniceList.get(j).getLokacijaLat()));
                latLngArr.put(latLng);
            }
            jsonBody.put("locations", latLngArr);

            // Finally query external api
            String mapQuestApiResponse = myHttpPost(distanceapi, jsonBody.toString());

            JSONObject mapQuestJson;
            JSONArray razdalje;
            JSONArray casi;
            JSONArray lokacije;
            // Check if valid json
            try {
                mapQuestJson = new JSONObject(mapQuestApiResponse);
                razdalje = (JSONArray) mapQuestJson.get("distance");
                casi = (JSONArray) mapQuestJson.get("time");
                lokacije = (JSONArray) mapQuestJson.get("locations");
            } catch (JSONException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Something went wrong while parsing JSON response from our external api :(").build();
            }

            // Read response from external api and set distance, time of travel, street, city
            int k = 0;
            for (int j = i; j < polnilniceList.size() && j < i + groupSize; j++) {
                Polnilnica listEl = polnilniceList.get(j);
                // k + 1 because the first one in the response is our location!
                listEl.setRazdalja((Double) razdalje.get(k + 1));
                listEl.setČas((Integer) casi.get(k + 1));
                JSONObject lokacija = (JSONObject) lokacije.get(k + 1);
                listEl.setMesto((String) lokacija.get("adminArea5"));
                listEl.setUlica((String) lokacija.get("street"));

                k++;
            }

            i+= groupSize;
        }

        return Response.status(Response.Status.OK).entity(polnilniceList).build();
    }

    private String myHttpPost(String url, String jsonbody) {
        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = null;
        try {
            request.setEntity(new StringEntity(jsonbody));
        } catch (UnsupportedEncodingException e) {
            return e.getMessage();
        }
        try {
            response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            return  e.getMessage();
        }

    }

    private String myHttpGet(String url, String body) {
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            return  e.getMessage();
        }
    }


}

