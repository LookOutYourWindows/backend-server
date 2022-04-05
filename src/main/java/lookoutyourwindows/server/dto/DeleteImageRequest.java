package lookoutyourwindows.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteImageRequest {

    @NotEmpty
    private String deleteFileName;
}
