package no.fint.arkiv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.arkiv.kodeverk.*;
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
        metadata = Arrays.stream(noarkMetadata).collect(Collectors.toMap(NoarkMetadata::getNavn, Function.identity()));
    }

    public Stream<SaksstatusResource> getSaksStatus() {
        return getEntries("saksstatus")
                .map(create(SaksstatusResource::new));
    }

    public Stream<JournalStatusResource> getJournalStatus() {
        return getEntries("journalstatus")
                .map(create(JournalStatusResource::new));
    }

    public Stream<DokumentStatusResource> getDokumentStatus() {
        return getEntries("dokumentstatus")
                .map(create(DokumentStatusResource::new));
    }

    public Stream<JournalpostTypeResource> getJournalpostType() {
        return getEntries("journalposttype")
                .map(create(JournalpostTypeResource::new));
    }

    public Stream<DokumentTypeResource> getDokumentType() {
        return getEntries("dokumentType")
                .map(create(DokumentTypeResource::new));
    }

    public Stream<KorrespondansepartTypeResource> getKorrespondansepartType() {
        return getEntries("korrespondanseparttype")
                .map(create(KorrespondansepartTypeResource::new));
    }

    public Stream<TilknyttetRegistreringSomResource> getTilknyttetRegistreringSom() {
        return getEntries("tilknyttetRegistreringSom")
                .map(create(TilknyttetRegistreringSomResource::new));
    }

    public Stream<PartRolleResource> getPartRolle() {
        return getEntries("partRolle")
                .map(create(PartRolleResource::new));
    }

    public Stream<FormatResource> getFormat() {
        return getEntries("format")
                .map(create(FormatResource::new));
    }

    public Stream<VariantformatResource> getVariantformat() {
        return getEntries("variantFormat")
                .map(create(VariantformatResource::new));
    }

    public Stream<TilgangsrestriksjonResource> getTilgangsrestriksjon() {
        return getEntries("tilgangsrestriksjon")
                .map(create(TilgangsrestriksjonResource::new));
    }

    public Stream<SkjermingshjemmelResource> getSkjermingshjemmel() {
        return getEntries("skjermingshjemmel")
                .map(create(SkjermingshjemmelResource::new));
    }

    private Stream<Map.Entry<String, String>> getEntries(String name) {
        return metadata.get(name).getVerdier().entrySet().stream();
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
