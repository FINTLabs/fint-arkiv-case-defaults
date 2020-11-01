package no.fint.arkiv;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "fint.case.coding")
public class CodingSystemDefaults {
    private boolean fatal;
    private Map<String,String>
            saksstatus,
            journalstatus,
            dokumentstatus,
            journalposttype,
            dokumentType,
            klassifikasjonstype,
            korrespondanseparttype,
            tilknyttetRegistreringSom,
            partRolle,
            skjermingMetadata,
            skjermingDokument,
            gradering,
            variantFormat;

}
