package no.fint.arkiv;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Getter
@ToString
public class CodeValue {
    private final String code, value;

    public CodeValue(String input) {
        if (!StringUtils.contains(input, '='))
            throw new IllegalArgumentException(input);

        code = StringUtils.trim(StringUtils.substringBefore(input, "="));
        value = StringUtils.trim(StringUtils.substringAfter(input, "="));
    }
}
