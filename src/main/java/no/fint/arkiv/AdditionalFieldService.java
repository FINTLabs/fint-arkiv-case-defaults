package no.fint.arkiv;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@Slf4j
public class AdditionalFieldService {
    private final LinkResolver resolver;

    public AdditionalFieldService(LinkResolver resolver) {
        this.resolver = resolver;
    }

    public <T> Stream<Field> getFieldsForResource(Map<String, String> fields, T resource) {
        if (fields == null) {
            log.warn("No custom fields for {}", resource.getClass());
            return Stream.empty();
        }
        final StringSubstitutor substitutor = new StringSubstitutor(new BeanPropertyLookup<>(resolver, resource));
        return fields.entrySet().stream()
                .map(e -> new Field(e.getKey(),
                        substitutor.replace(e.getValue())));
    }

    public <U> void setFieldsForResource(Map<String, String> fieldMap, U resource, List<Field> fields) {
        if (fieldMap == null) {
            return;
        }

        Pattern names = Pattern.compile("\\$\\{([^}]+)}");

        fields
                .stream()
                .filter(f -> fieldMap.containsKey(f.getName()))
                .filter(f -> StringUtils.isNotBlank(f.getValue()))
                .forEach(f -> {
                    final String format = fieldMap.get(f.getName());
                    log.trace("Parsing field {} -> {} -> {}", f.getName(), f.getValue(), format);
                    List<String> nameList = getNameList(names, format);
                    Matcher fieldMatcher = getFieldMatcher(names, format, f.getValue());
                    if (fieldMatcher.matches()) {
                        for (int i = 1; i <= fieldMatcher.groupCount(); i++) {
                            try {
                                log.debug("Setting attribute {} to {}", nameList.get(i - 1), fieldMatcher.group(i));
                                BeanUtils.setProperty(resource, nameList.get(i - 1), fieldMatcher.group(i));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private Matcher getFieldMatcher(Pattern names, String fieldPattern, String fieldValue) {
        String pattern = RegExUtils.replaceAll(fieldPattern, names, "(.+)");
        Pattern titlePattern = Pattern.compile("^" + pattern + "$");
        return titlePattern.matcher(fieldValue);
    }

    private List<String> getNameList(Pattern names, String value) {
        List<String> nameList = new LinkedList<>();
        Matcher nameMatcher = names.matcher(value);
        while (nameMatcher.find()) {
            nameList.add(nameMatcher.group(1));
        }
        log.trace("nameList = {}", nameList);
        return nameList;
    }

    @Data
    public static class Field {
        private final String name, value;
    }
}
