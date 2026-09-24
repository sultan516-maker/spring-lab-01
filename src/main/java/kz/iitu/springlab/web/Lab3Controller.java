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
        return Map.of(
                "owner", props.owner(),
                "group", props.group(),
                "mailFrom", props.mail().from(),
                "mailRetryCount", props.mail().retryCount(),
                "mailTimeout", props.mail().timeout().toString(),
                "mailEnabled", props.mail().enabled(),
                "activeProfiles", Arrays.asList(environment.getActiveProfiles()),
                "serverPort", environment.getProperty("server.port"),
                "banner", banner.describe()
        );
    }
}