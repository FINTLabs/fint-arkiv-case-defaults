package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class TitleService {

    private final Map<String,Title> titles;

    public TitleService(CustomFormats formats) {
        this.titles = formats.getTitle();
    }

    public <T> TitleMapper getTitleMapper(T object) {
        return new TitleMapper(titles.getOrDefault(resourceName(object), new Title()));
    }

    public static String resourceName(Object object) {
        return StringUtils.removeEnd(StringUtils.lowerCase(object.getClass().getSimpleName()), "resource");
    }

}
