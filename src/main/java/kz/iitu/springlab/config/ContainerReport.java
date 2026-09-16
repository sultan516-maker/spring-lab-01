package kz.iitu.springlab.config;

import kz.iitu.springlab.notify.Notifier;
import kz.iitu.springlab.notify.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ContainerReport implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContainerReport.class);
    private final ApplicationContext ctx;

    public ContainerReport(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("================ CONTAINER REPORT ================");

        log.info("Bean definitions in total: {}", ctx.getBeanDefinitionCount());

        Map<String, Notifier> notifiers = ctx.getBeansOfType(Notifier.class);
        log.info("Notifier implementations: {}", notifiers.keySet());

        Object notificationService = ctx.getBean("notificationService");
        log.info("Type of the notificationService bean: {}", notificationService.getClass().getName());

        log.info("--- Custom Beans ---");
        String[] allBeans = ctx.getBeanDefinitionNames();
        for (String beanName : allBeans) {
            if (beanName.startsWith("kz.iitu") || beanName.contains("Notifier")
                    || beanName.contains("Service") || beanName.contains("Config")
                    || beanName.contains("Demo") || beanName.contains("Office")) {
                log.info(" bean: {}", beanName);
            }
        }

        log.info("==================================================");
    }
}