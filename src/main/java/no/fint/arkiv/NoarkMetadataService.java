package no.fint.arkiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NoarkMetadataService {

    @Value("classpath:noark-metadata.json")
    Resource resource;

    @Getter
    private Map<String,NoarkMetadata> metadata;

    @PostConstruct
    public void init() throws IOException {
        NoarkMetadata[] noarkMetadata = new ObjectMapper().readValue(resource.getInputStream(), NoarkMetadata[].class);
        metadata = Arrays.stream(noarkMetadata).collect(Collectors.toMap(NoarkMetadata::getKode, Function.identity()));
    }
}
