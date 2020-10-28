package no.fint.arkiv;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class CaseProperties {
    private String administrativEnhet;
    private String journalenhet;
    private String arkivdel;
    private String[] noekkelord;
    private List<Klassifikasjon> klassifikasjon = new LinkedList<>();
    private String saksstatus;
    private String korrespondansepartType;
    private String journalpostType;
    private String journalstatus;
    private String dokumentstatus;
    private String dokumentType;
    private String tilknyttetRegistreringSom;
    private Skjermingskontekst[] skjermingskontekst;
    private String tilgangsrestriksjon;
    private String skjermingshjemmel;
    private String saksmappeType;

    public enum Skjermingskontekst {SAK, JOURNALPOST, DOKUMENT}

    @Data
    public static class Klassifikasjon {
        private String
                system,
                klasse,
                tittel;
    }
}
