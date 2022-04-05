package lookoutyourwindows.server.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Account {

    @Id
    @GeneratedValue
    @Column(name = "account_id")
    private Long id;

    private String username;

    private String password;

    private String email;

    private String role;

    protected Account() {}

    protected Account(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public static Account createAccount(String username, String password, String email) {
        Account account = new Account(username, password, email);
        account.role = "USER";

        return account;
    }


}
