package si.fri.rso.iskanje.api.v1.health;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import si.fri.rso.iskanje.services.RestProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.HttpURLConnection;
import java.net.URL;
@Readiness
@ApplicationScoped
@ConfigBundle("external-api")
public class PolnilniceHealthCheck implements HealthCheck{

    @ConfigValue(watch = true)
    private String polnilniceapi;

    public String getPolnilniceapi() {
        return this.polnilniceapi;
    }

    public void setPolnilniceapi(final String polnilniceapi) {
        this.polnilniceapi = polnilniceapi;
    }


    @Override
    public HealthCheckResponse call() {

        if(polnilniceapi == null)  {
            System.out.println("its null");
            return HealthCheckResponse.named(PolnilniceHealthCheck.class.getSimpleName()).down().build();
        }

        try {
            System.out.println(polnilniceapi);
            HttpURLConnection connection = (HttpURLConnection) new URL(polnilniceapi).openConnection();
            connection.setRequestMethod("HEAD");

            if (connection.getResponseCode() == 200) {
                return HealthCheckResponse.named(PolnilniceHealthCheck.class.getSimpleName()).up().build();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return HealthCheckResponse.named(PolnilniceHealthCheck.class.getSimpleName()).down().build();
    }
}
