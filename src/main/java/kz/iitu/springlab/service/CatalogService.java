package kz.iitu.springlab.service;

import kz.iitu.springlab.audit.Audited;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import kz.iitu.springlab.audit.Sensitive;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class CatalogService {

    @Autowired
    @Lazy
    private CatalogService self;
    
    public String removeTwice(long id) {
        self.remove(id);
        self.remove(id);
        return "Executed remove twice for id: " + id;
    }
    public String findById(long id) {
        sleep(50);
        return "Item no. " + id;
    }
    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300);
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> "Item no. " + i)
                .toList();
    }
    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public String updateSecretKey(long id, @Sensitive String secretKey) {
        sleep(50);
        return "Updated secret key for item no. " + id;
    }
}