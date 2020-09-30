package no.fint.arkiv;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties("fint.case.formats")
public class CustomFormats {
    private Map<String, Title> title;
    private Map<String, Map<String, String>> field;
}
