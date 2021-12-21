package si.fri.rso.iskanje.api.v1;

import com.kumuluz.ee.discovery.annotations.RegisterService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@RegisterService(value = "iskanje-service", ttl = 20, pingInterval = 15, environment = "dev", version = "1.0.0", singleton = false)
@ApplicationPath("/v1")
public class IskanjeApplication extends Application {

}
