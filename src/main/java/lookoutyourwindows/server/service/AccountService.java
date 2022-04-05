package lookoutyourwindows.server.service;

import lombok.RequiredArgsConstructor;
import lookoutyourwindows.server.domain.Account;
import lookoutyourwindows.server.dto.RegisterRequest;
import lookoutyourwindows.server.exception.AccountException;
import lookoutyourwindows.server.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long register(RegisterRequest registerRequest) {
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        Account account = Account.createAccount(registerRequest.getUsername(),
                                                encodedPassword,
                                                registerRequest.getEmail());

        validateDuplicateUsername(account);
        createS3Bucket(account);

        accountRepository.save(account);
        return account.getId();
    }

    private void validateDuplicateUsername(Account account) {
        List<Account> existingAccounts = accountRepository.findByUsername(account.getUsername());
        if (!existingAccounts.isEmpty()) {
            throw new AccountException("The username is duplicated.");
        }
    }

    private void createS3Bucket(Account account) {

    }

}
