package no.fint.arkiv;

import lombok.Data;

import java.util.Map;

@Data
public class CaseProperties {
    private Title title;
    private Map<String,String> field;
    private String administrativEnhet;
    private String journalenhet;
    private String arkivdel;
    private String saksansvarlig;
    private String[] noekkelord;
    private Map<String,Klasse> klassifikasjon;
    private String saksstatus;
    private String korrespondansepartType;
    private String journalpostType;
    private String journalstatus;
    private String saksbehandler;
    private String dokumentstatus;
    private String dokumentType;
    private String tilknyttetRegistreringSom;
    private Skjermingskontekst[] skjermingskontekst;
    private String tilgangsrestriksjon;
    private String skjermingshjemmel;
    private String saksmappeType;

    public enum Skjermingskontekst { SAK, JOURNALPOST, DOKUMENT }

    @Data
    public static class Title {
        private String cases, records, documents;
    }

    @Data
    public static class Klasse {
        private int rekkefolge;
        private String[] verdi;
    }

}
