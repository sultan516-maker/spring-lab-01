package kz.iitu.springlab.notify;

public interface Notifier {
    String send(String message);
    default String channel() {
        return "default";
    }
}