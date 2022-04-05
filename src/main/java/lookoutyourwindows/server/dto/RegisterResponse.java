package lookoutyourwindows.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse<T> {
    private T data;
}
