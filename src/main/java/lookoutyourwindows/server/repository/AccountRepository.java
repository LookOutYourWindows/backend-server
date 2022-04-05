package lookoutyourwindows.server.repository;

import lombok.RequiredArgsConstructor;
import lookoutyourwindows.server.domain.Account;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepository {

    private final EntityManager em;

    public void save(Account account) {
        em.persist(account);
    }

    public Account findById(Long id) {
        return em.find(Account.class, id);
    }

    public List<Account> findByUsername(String username) {
        return em.createQuery("select a from Account a where a.username = :username", Account.class)
                .setParameter("username", username)
                .getResultList();
    }

}
