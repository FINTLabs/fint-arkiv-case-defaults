package no.fint.arkiv;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "fint.case.coding")
public class CodingSystemDefaults {
    private CodeValue[]
            journalposttype,
            journalstatus,
            tilknyttetregistreringsom,
            korrespondanseparttype,
            variantformat;

}
