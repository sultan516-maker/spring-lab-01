package kz.iitu.springlab.account;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class IsolationService {

    private final AccountRepository accountRepository;
    private final EntityManager entityManager;

    public IsolationService(
            AccountRepository accountRepository,
            EntityManager entityManager) {
        this.accountRepository = accountRepository;
        this.entityManager = entityManager;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<BigDecimal> readCommitted(
            Long accountId,
            CountDownLatch firstRead,
            CountDownLatch secondRead) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow();

        BigDecimal first = account.getBalance();
        firstRead.countDown();

        waitForUpdate(secondRead);

        entityManager.refresh(account);

        BigDecimal second = account.getBalance();

        return Arrays.asList(first, second);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<BigDecimal> repeatableRead(
            Long accountId,
            CountDownLatch firstRead,
            CountDownLatch secondRead) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow();

        BigDecimal first = account.getBalance();
        firstRead.countDown();

        waitForUpdate(secondRead);

        entityManager.refresh(account);

        BigDecimal second = account.getBalance();

        return Arrays.asList(first, second);
    }

    @Transactional
    public void updateBalance(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow();

        account.credit(amount);
        accountRepository.save(account);
    }

    private void waitForUpdate(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}