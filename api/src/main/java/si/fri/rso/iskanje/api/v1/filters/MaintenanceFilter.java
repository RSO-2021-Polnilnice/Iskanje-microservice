package si.fri.rso.iskanje.api.v1.filters;

import si.fri.rso.iskanje.services.RestProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class MaintenanceFilter implements ContainerRequestFilter {

    @Inject
    private RestProperties restProperties;

    @Override
    public void filter(ContainerRequestContext ctx)  {
        if (restProperties.getMaintenanceMode()) {

            ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"message\" : \"Maintenance mode enabled\"}")
                    .build());
        }
    }
}