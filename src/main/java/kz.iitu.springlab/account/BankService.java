package kz.iitu.springlab.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BankService {

    private final AccountRepository accountRepository;

    public BankService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);

        accountRepository.save(from);
        accountRepository.save(to);
    }
    @Transactional(rollbackFor = Exception.class)
    public void transferWithCheckedException(
            Long fromId,
            Long toId,
            BigDecimal amount
    ) throws Exception {

        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);

        accountRepository.save(from);

        throw new Exception("Simulated checked exception");
    }
    @Transactional
    public void transferWithException(Long fromId, Long toId, BigDecimal amount, boolean throwError) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);

        accountRepository.save(from);

        if (throwError) {
            throw new RuntimeException("Simulated error after debit");
        }

        accountRepository.save(to);
    }
}