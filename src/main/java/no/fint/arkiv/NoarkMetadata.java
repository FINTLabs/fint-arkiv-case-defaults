package no.fint.arkiv;

import lombok.Data;

@Data
public class NoarkMetadata {
    private String kode;
    private String navn;
    private String[] verdier;
}
