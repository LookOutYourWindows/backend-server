package lookoutyourwindows.server.repository;

import lookoutyourwindows.server.domain.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void saveAndFindById() {
        Account account = Account.createAccount("test1", "1234", "a@a.com");
        accountRepository.save(account);

        Account result = accountRepository.findById(account.getId());

        assertThat(account).isEqualTo(result);
    }

    @Test
    void noResultFindByIdResult() {
        Account result = accountRepository.findById(72515123141L);

        assertThat(result).isEqualTo(null);
    }


    @Test
    void findByUsername() {
        Account account = Account.createAccount("test1", "1234", "a@a.com");
        accountRepository.save(account);

        List<Account> result = accountRepository.findByUsername(account.getUsername());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(account);
    }
}