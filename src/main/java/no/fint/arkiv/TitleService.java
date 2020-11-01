package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class TitleService {

    private final Map<String,Title> titles;
    private final LinkResolver linkResolver;
    private final boolean fatal;

    public TitleService(CustomFormats formats, LinkResolver linkResolver) {
        this.titles = formats.getTitle();
        this.linkResolver = linkResolver;
        this.fatal = formats.isFatal();
    }

    public <T> TitleMapper getTitleMapper(T object) {
        return new TitleMapper(titles.getOrDefault(resourceName(object), new Title()), linkResolver, fatal);
    }

    public static String resourceName(Object object) {
        return StringUtils.removeEnd(StringUtils.lowerCase(object.getClass().getSimpleName()), "resource");
    }

}
