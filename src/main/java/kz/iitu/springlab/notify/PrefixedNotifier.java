package kz.iitu.springlab.notify;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("prefixed")
@Order(3)
public class PrefixedNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(PrefixedNotifier.class);

    @Value("${app.notifier.prefix:[CUSTOM-PREFIX]}")
    private String prefix;

    @PostConstruct
    public void init() {
        log.info("PrefixedNotifier initialized with prefix: {}", prefix);
    }

    @Override
    public String channel() {
        return "prefixed";
    }

    @Override
    public String send(String message) {
        if (message == null) {
            return prefix + " ";
        }
        return prefix + " " + message;
    }
}