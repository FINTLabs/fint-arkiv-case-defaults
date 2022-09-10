package no.fint.arkiv;

import lombok.Data;

import java.util.Map;

@Data
public class CaseProperties {
    private Title title;
    private Map<String,String> field;
    private String administrativEnhet;
    private String journalenhet;
    // TODO Can this be different values for case and registry entry?
    private String arkivdel;
    private String saksansvarlig;
    private String[] noekkelord;
    private Map<Integer,Klasse> klassifikasjon;
    private String saksstatus;
    private String korrespondansepartType;
    private String journalpostType;
    private String journalstatus;
    private Map<Journalposttype, Journalstatus> journalpost;
    private String saksbehandler;
    private String avskrivningsmaate;
    private String dokumentstatus;
    private String dokumentType;
    private String format;
    private String variantFormat;
    private String tilknyttetRegistreringSom;
    private Skjermingskontekst[] skjermingskontekst;
    private String tilgangsrestriksjon;
    private String skjermingshjemmel;
    private String saksmappeType;

    public enum Skjermingskontekst { SAK, JOURNALPOST, DOKUMENT, KORRESPONDANSEPART }
    public enum Journalposttype {I, U, N, X, S}

    @Data
    public static class Title {
        private String cases, records, documents;
    }

    @Data
    public static class Klasse {
        private String ordning, verdi, tittel;
    }

    @Data
    public static class Journalstatus {
        private String status;
    }
}
