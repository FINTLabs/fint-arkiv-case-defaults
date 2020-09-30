package no.fint.arkiv;

import lombok.Data;

import java.util.Map;

@Data
public class NoarkMetadata {
    private String kode;
    private String navn;
    private boolean obligatorisk;
    private Map<String,String> verdier;
}
