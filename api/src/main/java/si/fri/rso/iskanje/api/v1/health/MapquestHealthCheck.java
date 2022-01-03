package si.fri.rso.iskanje.api.v1.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import si.fri.rso.iskanje.services.RestProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.HttpURLConnection;
import java.net.URL;

@Readiness
@ApplicationScoped
public class MapquestHealthCheck implements HealthCheck {

    private static final String url = "https://www.mapquest.com/";

    @Inject
    private RestProperties restProperties;

    @Override
    public HealthCheckResponse call() {
        if (restProperties.getMaintenanceModeQuestApi()) {
            return HealthCheckResponse.down(MapquestHealthCheck.class.getSimpleName());
        }

        try {

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");

            if (connection.getResponseCode() == 200) {
                return HealthCheckResponse.named(MapquestHealthCheck.class.getSimpleName()).up().build();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return HealthCheckResponse.named(MapquestHealthCheck.class.getSimpleName()).down().build();
    }
}