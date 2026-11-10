package kz.iitu.springlab.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropagationService {

    private final AuditService auditService;

    public PropagationService(AuditService auditService) {
        this.auditService = auditService;
    }

    @Transactional
    public void testRequired() {
        auditService.saveRequired("REQUIRED audit record");

        throw new RuntimeException("Outer transaction failed");
    }

    @Transactional
    public void testRequiresNew() {
        auditService.saveRequiresNew("REQUIRES_NEW audit record");

        throw new RuntimeException("Outer transaction failed");
    }
}