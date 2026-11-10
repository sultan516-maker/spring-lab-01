package kz.iitu.springlab.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveRequired(String message) {
        auditLogRepository.save(new AuditLog(message));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRequiresNew(String message) {
        auditLogRepository.save(new AuditLog(message));
    }
}