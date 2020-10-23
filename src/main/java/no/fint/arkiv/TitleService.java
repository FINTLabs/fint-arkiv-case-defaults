package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TitleService {

    private final LinkResolver resolver;
    private final Map<String,String> titles;
    private final boolean fatal;

    public TitleService(LinkResolver resolver, CustomFormats formats) {
        this.resolver = resolver;
        this.titles = formats.getTitle();
        fatal = formats.isFatal();
    }

    public <T> String getTitle(T object) {
        String type = resourceName(object);
        if (fatal && !titles.containsKey(type)) {
            throw new IllegalArgumentException("No format defined for " + type);
        }
        String title = new StringSubstitutor(new BeanPropertyLookup<>(resolver, object)).replace(titles.get(type));
        log.debug("Title: '{}'", title);
        return title;
    }

    public static String resourceName(Object object) {
        return StringUtils.removeEnd(StringUtils.lowerCase(object.getClass().getSimpleName()), "resource");
    }

    public void parseTitle(Object object, String title) {
        if (titles == null){
            log.debug("No formats defined!");
            return;
        }
        String format = titles.get(resourceName(object));
        if (StringUtils.isBlank(format)) {
            log.debug("No format defined for {}", resourceName(object));
            return;
        }
        Pattern names = Pattern.compile("\\$\\{([^}]+)}");
        Matcher nameMatcher = names.matcher(format);
        List<String> nameList = new ArrayList<>();
        while (nameMatcher.find()) {
            nameList.add(nameMatcher.group(1));
        }
        log.debug("nameList = {}", nameList);

        String pattern = RegExUtils.replaceAll(format, names, "(.+)");
        Pattern titlePattern = Pattern.compile("^" + pattern + "$");
        Matcher titleMatcher = titlePattern.matcher(title);
        if (titleMatcher.matches()) {
            for (int i = 1; i <= titleMatcher.groupCount(); ++i) {
                try {
                    log.debug("Setting property {} to {}", nameList.get(i-1), titleMatcher.group(i));
                    BeanUtils.setProperty(object, nameList.get(i-1), titleMatcher.group(i));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
