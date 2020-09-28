package no.fint.arkiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.administrasjon.arkiv.JournalpostTypeResource;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartTypeResource;
import no.fint.model.resource.administrasjon.arkiv.TilknyttetRegistreringSomResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NoarkMetadataService {

    @Value("classpath:noark-metadata.json")
    Resource resource;

    @Getter
    private Map<String, NoarkMetadata> metadata;

    @PostConstruct
    public void init() throws IOException {
        NoarkMetadata[] noarkMetadata = new ObjectMapper().readValue(resource.getInputStream(), NoarkMetadata[].class);
        metadata = Arrays.stream(noarkMetadata).collect(Collectors.toMap(NoarkMetadata::getKode, Function.identity()));
    }

    public Stream<JournalpostTypeResource> getJournalpostType() {
        return metadata.get("M082").getVerdier().entrySet().stream()
                .map(create(JournalpostTypeResource::new));
    }

    public Stream<TilknyttetRegistreringSomResource> getTilknyttetRegistreringSom() {
        return metadata.get("M217").getVerdier().entrySet().stream()
                .map(create(TilknyttetRegistreringSomResource::new));
    }

    public Stream<KorrespondansepartTypeResource> getKorrespondansepartType() {
        return metadata.get("M087").getVerdier().entrySet().stream()
                .map(create(KorrespondansepartTypeResource::new));
    }

    private static <T extends Begrep> Function<Map.Entry<String, String>, T> create(Supplier<T> supplier) {
        return item -> {
            T r = supplier.get();
            r.setKode(item.getKey());
            r.setNavn(item.getValue());
            Identifikator i = new Identifikator();
            i.setIdentifikatorverdi(item.getKey());
            r.setSystemId(i);
            return r;
        };
    }

}
