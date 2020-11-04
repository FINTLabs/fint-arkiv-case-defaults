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
    private String[] noekkelord;
    private String[] klassifikasjon;
    private String[] klasse;
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

    public enum Skjermingskontekst { SAK, JOURNALPOST, DOKUMENT }
}
