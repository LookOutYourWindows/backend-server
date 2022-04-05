package lookoutyourwindows.server.contoller;

import lombok.RequiredArgsConstructor;
import lookoutyourwindows.server.dto.*;
//import lookoutyourwindows.server.exception.ImageException;
import lookoutyourwindows.server.security.AccountContext;
import lookoutyourwindows.server.service.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static java.util.Base64.*;

@RestController
@RequiredArgsConstructor
public class ImageApiController {

    private final ImageService imageService;

    @PostMapping("/api/v1/images")
    public UploadImageResponse uploadImage(@Valid UploadImageRequest request,
                                           @AuthenticationPrincipal AccountContext accountContext) {

        String username = accountContext.getUsername();

        String uploadFileName = imageService.uploadImage(username, request.getMultipartFile());

        return new UploadImageResponse(uploadFileName);
    }

    @GetMapping("/api/v1/images/{b64ImageName}")
    public ResponseEntity<Object> downloadImage(@PathVariable String b64ImageName,
                                         @AuthenticationPrincipal AccountContext accountContext) {

        String username = accountContext.getUsername();

        return imageService.downloadImage(username, decodeBase64(b64ImageName));
    }

    @GetMapping("/api/v1/images/{b64ImageName}/outputs")
    public ResponseEntity<Object> downloadImages(@PathVariable String b64ImageName,
                                         @AuthenticationPrincipal AccountContext accountContext) {

        String username = accountContext.getUsername();

        List<String> outputImageNames = imageService.getOutputImageNames(username, decodeBase64(b64ImageName));

        if (outputImageNames.size() < 5) {
            return new ResponseEntity<>("The images have not been created yet.", HttpStatus.NOT_FOUND);
        }

        return imageService.downloadImages(username, outputImageNames);
    }

    @GetMapping("/api/v1/images/{b64ImageName}/thumbnail")
    public ResponseEntity<Object> downloadThumbnail(@PathVariable String b64ImageName,
                                            @AuthenticationPrincipal AccountContext accountContext) {

        String username = accountContext.getUsername();
        String imageName = decodeBase64(b64ImageName);

        List<String> outputImageNames = imageService.getOutputImageNames(username, imageName);

        for (String outputImageName : outputImageNames) {
            if (outputImageName.endsWith("_thumbnail.jpg")) {
                return imageService.downloadImage(username, outputImageName);
            }
        }

        return imageService.downloadImage(username, imageName);
    }

    @DeleteMapping("/api/v1/images")
    public DeleteImageResponse deleteImage(@RequestBody @Valid DeleteImageRequest request,
                                           @AuthenticationPrincipal AccountContext accountContext) {

        String username = accountContext.getUsername();

        String deleteFileName = imageService.deleteImage(username, request.getDeleteFileName());

        return new DeleteImageResponse(deleteFileName);
    }

    @GetMapping("/api/v1/images")
    public ListImagesResponse listImages(@AuthenticationPrincipal AccountContext accountContext) {

        String username = accountContext.getUsername();

        List<String> fileNames = imageService.getImageNames(username);

        return new ListImagesResponse(fileNames);
    }

//    private void validateUploadImageRequest(UploadImageRequest request) {
//        MultipartFile multipartFile = request.getMultipartFile();
//        String contentType = multipartFile.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new ImageException("The file is not an image.");
//        }
//    }

    private String decodeBase64(String src) {
        Decoder decoder = getDecoder();

        return new String(decoder.decode(src));
    }

}
