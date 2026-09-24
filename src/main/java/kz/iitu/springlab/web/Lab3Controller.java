package kz.iitu.springlab.web;

import kz.iitu.springlab.config.*;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/lab3")
public class Lab3Controller {

    private final AppProperties props;
    private final EnvironmentBanner banner;
    private final Environment environment;

    public Lab3Controller(AppProperties props, EnvironmentBanner banner, Environment environment) {
        this.props = props;
        this.banner = banner;
        this.environment = environment;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("owner", props.owner());
        response.put("group", props.group());
        response.put("mailFrom", props.mail().from());
        response.put("mailRetryCount", props.mail().retryCount());
        response.put("mailTimeout", props.mail().timeout().toString());
        response.put("mailEnabled", props.mail().enabled());
        response.put("featuresBetaEnabled", props.features() != null ? props.features().betaEnabled() : false);
        response.put("featuresMaxExperimentalUsers", props.features() != null ? props.features().maxExperimentalUsers() : 10);
        response.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
        response.put("serverPort", environment.getProperty("server.port"));
        response.put("banner", banner.describe());
        return response;
    }
}