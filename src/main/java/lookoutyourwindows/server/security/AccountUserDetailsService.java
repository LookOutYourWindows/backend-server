package lookoutyourwindows.server.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lookoutyourwindows.server.domain.Account;
import lookoutyourwindows.server.repository.AccountRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<Account> accounts = accountRepository.findByUsername(username);
        if (accounts.isEmpty()) {
            throw new UsernameNotFoundException("UsernameNotFound");
        } else if (accounts.size() > 2) {
            throw new IllegalStateException("Duplicate users exist");
        }

        Account account = accounts.get(0);
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(account.getRole()));
        log.info("Account id = {}, username = {}, role = {}",
                account.getId(), account.getUsername(), account.getRole());

        AccountContext accountContext = new AccountContext(account, roles);
        log.info("Authorities: {}", accountContext.getAuthorities());
        return accountContext;
    }
}
