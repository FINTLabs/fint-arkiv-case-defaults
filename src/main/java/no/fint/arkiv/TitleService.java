package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TitleService {

    private final LinkResolver linkResolver;

    public TitleService(LinkResolver linkResolver) {
        this.linkResolver = linkResolver;
    }

    public <T extends SaksmappeResource> String getCaseTitle(Title title, T saksmappe) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(linkResolver, saksmappe)).replace(title.getCases());
        log.debug("{} - Case title: '{}'", resourceName(saksmappe), result);
        return result;
    }

    public <T extends SaksmappeResource> String getRecordTitlePrefix(Title title, T saksmappe) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(linkResolver, saksmappe)).replace(title.getRecords());
        log.debug("{} - Record title: '{}'", resourceName(saksmappe), result);
        return result == null ? "" : result + " ";
    }

    public <T extends SaksmappeResource> String getDocumentTitlePrefix(Title title, T saksmappe) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(linkResolver, saksmappe)).replace(title.getDocuments());
        log.debug("{} - Document title: '{}'", resourceName(saksmappe), result);
        return result == null ? "" : result + " ";
    }

    public boolean parseCaseTitle(Title title, SaksmappeResource saksmappe, String input) {
        if (title == null || StringUtils.isBlank(title.getCases())) {
            log.debug("No case title format defined");
            return false;
        }
        return parseTitle(saksmappe, input, title.getCases());
    }

    private boolean parseTitle(Object object, String title, String format) {
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
                    log.debug("Setting property {} to {}", nameList.get(i - 1), titleMatcher.group(i));
                    BeanUtils.setProperty(object, nameList.get(i - 1), titleMatcher.group(i));
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                    log.debug("Unable to set property {}", nameList.get(i - 1));
                }
            }
            return true;
        }
        return false;
    }

    public static String resourceName(Object object) {
        return StringUtils.removeEnd(StringUtils.lowerCase(object.getClass().getSimpleName()), "resource");
    }
}
