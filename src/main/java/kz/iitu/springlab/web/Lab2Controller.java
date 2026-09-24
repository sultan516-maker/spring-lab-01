package kz.iitu.springlab.web;

import kz.iitu.springlab.lifecycle.LifecycleDemo;
import kz.iitu.springlab.notify.NotificationService;
import kz.iitu.springlab.notify.Notifier;
import kz.iitu.springlab.scope.TicketOffice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final Notifier customNotifier;
    private final NotificationService notifications;
    private final LifecycleDemo lifecycle;
    private final TicketOffice ticketOffice;

    public Lab2Controller(
            @Qualifier("prefixed") Notifier customNotifier,
            NotificationService notifications,
            LifecycleDemo lifecycle,
            TicketOffice ticketOffice
    ) {
        this.customNotifier = customNotifier;
        this.notifications = notifications;
        this.lifecycle = lifecycle;
        this.ticketOffice = ticketOffice;
    }

    @GetMapping("/notify")
    public Map<String, Object> notify(@RequestParam(defaultValue = "Hello") String text) {
        return Map.of(
                "1_primary", notifications.viaPrimary(text),
                "2_console", notifications.viaConsole(text),
                "3_all", notifications.viaAll(text),
                "4_beanNames", notifications.names()
        );
    }

    @GetMapping("/lifecycle")
    public List<String> lifecycle() {
        return lifecycle.events();
    }
    @GetMapping("/custom")
    public String sendCustomNotification(@RequestParam(defaultValue = "Default test message") String text) {
        return customNotifier.send(text);
    }
    @GetMapping("/scopes")
    public Map<String, Object> scopes() {
        return ticketOffice.demo();
    }
}