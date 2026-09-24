package kz.iitu.springlab.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Fallback;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("noop")
@Fallback
@Order(99)
public class NoopNotifier implements Notifier {

    private static final Logger log = LoggerFactory.getLogger(NoopNotifier.class);

    @Override
    public String send(String message) {
        log.info("NOOP >> {}", message);
        return "noop";
    }
}