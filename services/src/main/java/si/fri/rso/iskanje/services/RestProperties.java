package si.fri.rso.iskanje.services;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;
import com.kumuluz.ee.configuration.cdi.producers.ConfigurationUtilProducer;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ConfigBundle("rest-properties")
@ApplicationScoped
public class RestProperties {

    @ConfigValue(watch = true)
    private Boolean maintenanceMode;

    @ConfigValue(watch = true)
    private Boolean maintenanceModeQuestApi;

    @ConfigValue(watch = true)
    private Boolean broken;

    public Boolean getMaintenanceModeQuestApi() {
        return this.maintenanceModeQuestApi;
    }

    public void setMaintenanceModeQuestApi(final Boolean maintenanceModeQuestApi) {
        this.maintenanceModeQuestApi = maintenanceModeQuestApi;
    }


    public Boolean getMaintenanceMode() {
        return this.maintenanceMode;
    }

    public void setMaintenanceMode(final Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public Boolean getBroken() {
        return broken;
    }

    public void setBroken(final Boolean broken) {
        this.broken = broken;
    }
}