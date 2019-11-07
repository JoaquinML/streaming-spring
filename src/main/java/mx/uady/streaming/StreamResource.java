package mx.uady.streaming;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StreamResource {

    @GetMapping("/videos/{nombre}/completo")
    public ResponseEntity<UrlResource> getVideo(@PathVariable String nombre) throws MalformedURLException {

        String uriVideo = "videos/" + nombre;
        UrlResource video = new UrlResource("file:" + uriVideo);

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(video);

    }

    @GetMapping("/videos/{nombre}")
    public ResponseEntity<ResourceRegion> getVideoPorPartes(@PathVariable String nombre,
            @RequestHeader HttpHeaders headers) throws IOException {

        String ubicacion = "videos/" + nombre;
        UrlResource video = new UrlResource("file:" + ubicacion);

        ResourceRegion region = partirVideo(video, headers);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);

    }

    private ResourceRegion partirVideo(UrlResource video, HttpHeaders headers) throws IOException {
        long longitudVideo = video.contentLength();
        HttpRange rango = headers.getRange().size() > 0 ? headers.getRange().get(0) : null;

        if(rango == null){
            long paso = Math.min(1024, longitudVideo);
            return new ResourceRegion(video, 0, paso);
        }

        long minimo = rango.getRangeStart(longitudVideo);
        long maximo =  rango.getRangeEnd(longitudVideo);
        long paso = Math.min(1024, maximo-minimo);

        return new ResourceRegion(video, minimo, paso);
    }
}