package lookoutyourwindows.server.service;

import lookoutyourwindows.server.domain.Account;
import lookoutyourwindows.server.dto.UploadImageRequest;
import lookoutyourwindows.server.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(MockitoExtension.class)
class ImageServiceTest {


    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ImageService imageService;



    @Test
    void uploadImageS3Bucket() {
        Account account = Account.createAccount("testuser", "123", "a@b.com");


    }
}