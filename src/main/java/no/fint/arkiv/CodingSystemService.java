package no.fint.arkiv;

import no.fint.model.felles.basisklasser.Begrep;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.administrasjon.arkiv.JournalpostTypeResource;
import no.fint.model.resource.administrasjon.arkiv.KorrespondansepartTypeResource;
import no.fint.model.resource.administrasjon.arkiv.TilknyttetRegistreringSomResource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class CodingSystemService {
    private final CodingSystemDefaults codingSystemDefaults;

    public CodingSystemService(CodingSystemDefaults codingSystemDefaults) {
        this.codingSystemDefaults = codingSystemDefaults;
    }

    public Stream<JournalpostTypeResource> getJournalpostType() {
        return Arrays.stream(codingSystemDefaults.getJournalposttype())
                .map(create(JournalpostTypeResource::new));
    }

    public Stream<TilknyttetRegistreringSomResource> getTilknyttetRegistreringSom() {
        return Arrays.stream(codingSystemDefaults.getTilknyttetregistreringsom())
                .map(create(TilknyttetRegistreringSomResource::new));
    }

    public Stream<KorrespondansepartTypeResource> getKorrespondansepartType() {
        return Arrays.stream(codingSystemDefaults.getKorrespondanseparttype())
                .map(create(KorrespondansepartTypeResource::new));
    }

    private static <T extends Begrep> Function<CodeValue, T> create(Supplier<T> supplier) {
        return item -> {
            T r = supplier.get();
            r.setKode(item.getCode());
            r.setNavn(item.getValue());
            Identifikator i = new Identifikator();
            i.setIdentifikatorverdi(item.getCode());
            r.setSystemId(i);
            return r;
        };
    }

}
