package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.model.resource.FintLinks;
import no.novari.fint.model.resource.Link;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CodingSystemService {
    private final CodingSystemDefaults codingSystemDefaults;
    private final NoarkMetadataService noarkMetadataService;

    public CodingSystemService(
            CodingSystemDefaults codingSystemDefaults,
            NoarkMetadataService noarkMetadataService) {
        this.codingSystemDefaults = codingSystemDefaults;
        this.noarkMetadataService = noarkMetadataService;
        validate();
    }

    private void validate() {
        final int errors = noarkMetadataService.getMetadata().values().stream().filter(NoarkMetadata::isObligatorisk)
                .mapToInt(m -> {
                    final Map<String, String> codes = getCodes(m.getNavn());
                    final Set<String> missing = m.getVerdier().keySet().stream().filter(it -> !codes.containsKey(it)).collect(Collectors.toSet());
                    if (!missing.isEmpty()) {
                        log.warn("Missing definitions for {}: {}", m.getNavn(), missing);
                    }
                    return missing.size();
                })
                .sum();
        if (codingSystemDefaults.isFatal() && errors > 0) {
            throw new IllegalArgumentException("Missing definitions for " + errors + " mandatory Noark codes.  See log for details.");
        }
    }

    private Map<String, String> getCodes(String property) {
        try {
            log.trace("Try get property {}", property);
            final Map<String, String> result = (Map<String, String>) PropertyUtils.getProperty(codingSystemDefaults, property);
            if (result == null) {
                return Collections.emptyMap();
            }
            return result;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Collections.emptyMap();
        }
    }

    public void mapCodingSystemLinks(FintLinks resource) {
        resource
                .getLinks()
                .entrySet()
                .stream()
                .peek(s -> log.debug("Checking {} ... : {}", s.getKey(), noarkMetadataService.getMetadata().get(s.getKey())))
                .filter(it -> noarkMetadataService.getMetadata().containsKey(it.getKey()))
                .forEach(this::updateEntry);
    }

    private void updateEntry(Map.Entry<String, List<Link>> linkEntry) {
        updateLinks(getCodes(linkEntry.getKey()), linkEntry.getValue());
    }

    private void updateLinks(Map<String, String> mapping, List<Link> links) {
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            String key = StringUtils.substringAfterLast(link.getHref(), "/");

            if (mapping.containsKey(key)) {
                final String replacement = mapping.get(key);
                final Link newLink = Link.with(StringUtils.substringBeforeLast(link.getHref(), "/") + "/" + replacement);
                log.debug("Replacing {} with {}", link, newLink);
                links.set(i, newLink);
            }
        }
    }

}
