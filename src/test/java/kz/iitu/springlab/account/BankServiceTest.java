package kz.iitu.springlab.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import kz.iitu.springlab.audit.AuditLogRepository;
import kz.iitu.springlab.audit.PropagationService;
import kz.iitu.springlab.account.IsolationService;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BankServiceTest {

    @Autowired
    private BankService bankService;

    @Autowired
    private IsolationService isolationService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PropagationService propagationService;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        auditLogRepository.deleteAll();

        Account alice = new Account("Alice", new BigDecimal("1000.00"));
        Account bob = new Account("Bob", new BigDecimal("500.00"));

        accountRepository.save(alice);
        accountRepository.save(bob);
    }
    @Test
    void testRequiredRollback() {
        assertThrows(RuntimeException.class, () ->
                propagationService.testRequired()
        );

        assertEquals(0, auditLogRepository.count());
    }
    @Test
    void testRequiresNewCommit() {
        assertThrows(RuntimeException.class, () ->
                propagationService.testRequiresNew()
        );

        assertEquals(1, auditLogRepository.count());
    }
    @Test
    void testReadCommitted() throws Exception {
        Account alice = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Alice"))
                .findFirst()
                .orElseThrow();

        CountDownLatch firstRead = new CountDownLatch(1);
        CountDownLatch secondRead = new CountDownLatch(1);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<List<BigDecimal>> future = executor.submit(() ->
                    isolationService.readCommitted(
                            alice.getId(), firstRead, secondRead
                    )
            );

            assertTrue(firstRead.await(5, TimeUnit.SECONDS));

            isolationService.updateBalance(
                    alice.getId(), new BigDecimal("100.00")
            );

            secondRead.countDown();

            List<BigDecimal> result = future.get(5, TimeUnit.SECONDS);

            assertEquals(0, new BigDecimal("1000.00").compareTo(result.get(0)));
            assertEquals(0, new BigDecimal("1100.00").compareTo(result.get(1)));

        } finally {
            secondRead.countDown();
            executor.shutdownNow();
        }
    }
    @Test
    void testOptimisticLocking() {
        Account staleAlice = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Alice"))
                .findFirst()
                .orElseThrow();

        isolationService.updateBalance(
                staleAlice.getId(),
                new BigDecimal("100.00")
        );

        staleAlice.credit(new BigDecimal("50.00"));

        assertThrows(
                OptimisticLockingFailureException.class,
                () -> accountRepository.saveAndFlush(staleAlice)
        );

        Account updatedAlice = accountRepository
                .findById(staleAlice.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("1100.00").compareTo(updatedAlice.getBalance())
        );
    }
    @Test
    void testRepeatableRead() throws Exception {
        Account alice = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Alice"))
                .findFirst()
                .orElseThrow();

        CountDownLatch firstRead = new CountDownLatch(1);
        CountDownLatch secondRead = new CountDownLatch(1);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<List<BigDecimal>> future = executor.submit(() ->
                    isolationService.repeatableRead(
                            alice.getId(), firstRead, secondRead
                    )
            );

            assertTrue(firstRead.await(5, TimeUnit.SECONDS));

            isolationService.updateBalance(
                    alice.getId(), new BigDecimal("100.00")
            );

            secondRead.countDown();

            List<BigDecimal> result = future.get(5, TimeUnit.SECONDS);

            assertEquals(0, new BigDecimal("1000.00").compareTo(result.get(0)));
            assertEquals(0, new BigDecimal("1000.00").compareTo(result.get(1)));

        } finally {
            secondRead.countDown();
            executor.shutdownNow();
        }
    }
    @Test
    void testSuccessfulTransfer() {

        Account alice = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Alice"))
                .findFirst().orElseThrow();

        Account bob = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Bob"))
                .findFirst().orElseThrow();

        bankService.transfer(
                alice.getId(),
                bob.getId(),
                new BigDecimal("100.00")
        );

        Account updatedAlice = accountRepository.findById(alice.getId()).orElseThrow();
        Account updatedBob = accountRepository.findById(bob.getId()).orElseThrow();

        assertThat(updatedAlice.getBalance()).isEqualByComparingTo("900.00");
        assertThat(updatedBob.getBalance()).isEqualByComparingTo("600.00");
    }

    @Test
    void testInsufficientFunds() {

        Account alice = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Alice"))
                .findFirst().orElseThrow();

        Account bob = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Bob"))
                .findFirst().orElseThrow();

        assertThrows(IllegalStateException.class, () ->
                bankService.transfer(
                        alice.getId(),
                        bob.getId(),
                        new BigDecimal("2000.00")
                )
        );

        Account updatedAlice = accountRepository.findById(alice.getId()).orElseThrow();
        Account updatedBob = accountRepository.findById(bob.getId()).orElseThrow();

        assertThat(updatedAlice.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(updatedBob.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void testRollbackOnException() {

        Account alice = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Alice"))
                .findFirst().orElseThrow();

        Account bob = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Bob"))
                .findFirst().orElseThrow();

        assertThrows(RuntimeException.class, () ->
                bankService.transferWithException(
                        alice.getId(),
                        bob.getId(),
                        new BigDecimal("100.00"),
                        true
                )
        );

        Account updatedAlice = accountRepository.findById(alice.getId()).orElseThrow();
        Account updatedBob = accountRepository.findById(bob.getId()).orElseThrow();

        assertThat(updatedAlice.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(updatedBob.getBalance()).isEqualByComparingTo("500.00");
    }
    @Test
    void testRollbackOnCheckedException() throws Exception {
        Account alice = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Alice"))
                .findFirst()
                .orElseThrow();

        Account bob = accountRepository.findAll().stream()
                .filter(a -> a.getOwnerName().equals("Bob"))
                .findFirst()
                .orElseThrow();

        assertThrows(Exception.class, () ->
                bankService.transferWithCheckedException(
                        alice.getId(),
                        bob.getId(),
                        new BigDecimal("100.00")
                )
        );

        Account updatedAlice = accountRepository.findById(alice.getId()).orElseThrow();
        Account updatedBob = accountRepository.findById(bob.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("1000.00").compareTo(updatedAlice.getBalance()));
        assertEquals(0, new BigDecimal("500.00").compareTo(updatedBob.getBalance()));
    }
}
