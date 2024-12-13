package no.fint.arkiv;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "fint.case.defaults")
public class CaseDefaults {
    @NestedConfigurationProperty
    private CaseProperties
            tilskuddfartoy,
            tilskuddfredabygningprivateie,
            dispensasjonautomatiskfredakulturminne,
            personalmappe,
            soknaddrosjeloyve,
            sak;
}
