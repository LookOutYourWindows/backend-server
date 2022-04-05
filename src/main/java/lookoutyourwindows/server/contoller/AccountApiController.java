package lookoutyourwindows.server.contoller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lookoutyourwindows.server.dto.RegisterRequest;
import lookoutyourwindows.server.exception.AccountException;
import lookoutyourwindows.server.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountService accountService;

    @PostMapping("/api/v1/login")
    public String login() {
        return "Accepted";
    }

    @PostMapping("/api/v1/users")
    public String register(@RequestBody @Valid RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           HttpServletResponse response) throws IOException {

        try {
            Long accountId = accountService.register(registerRequest);
            log.info("Account [" + accountId + "] is registered.");
        } catch (AccountException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate username.", new AccountException());
        }

        response.setStatus(201);
        return "Accepted";
    }

    @GetMapping("/api/v1/myPage")
    public String getMyPage() {
        return "Accepted";
    }
}
