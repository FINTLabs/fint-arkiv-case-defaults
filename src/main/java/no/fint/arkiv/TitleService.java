package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource;
import no.fint.model.resource.arkiv.noark.RegistreringResource;
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

    public <T extends SaksmappeResource> String getRecordTitle(Title title, T saksmappe, RegistreringResource registrering) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(linkResolver, registrering, saksmappe)).replace(title.getRecords());
        log.debug("{} - Record title: '{}'", resourceName(saksmappe), result);
        return result;
    }

    public <T extends SaksmappeResource> String getDocumentTitle(Title title, T saksmappe, RegistreringResource registering, DokumentbeskrivelseResource dokumentbeskrivelse) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(linkResolver, dokumentbeskrivelse, registering, saksmappe)).replace(title.getDocuments());
        log.debug("{} - Document title: '{}'", resourceName(saksmappe), result);
        return result;
    }

    public boolean parseCaseTitle(Title title, SaksmappeResource saksmappe, String input) {
        if (StringUtils.isBlank(title.getCases())) {
            log.debug("No case title format defined");
            return !title.isFatal();
        }
        return parseTitle(saksmappe, input, title.getCases());
    }

    public boolean parseRecordTitle(Title title, RegistreringResource registering, String input) {
        if (StringUtils.isBlank(title.getRecords())) {
            log.debug("No record title format defined");
            return !title.isFatal();
        }
        return parseTitle(registering, input, title.getRecords());
    }

    public boolean parseDocumentTitle(Title title, DokumentbeskrivelseResource dokumentbeskrivelse, String input) {
        if (StringUtils.isBlank(title.getDocuments())) {
            log.debug("No document title format defined");
            return !title.isFatal();
        }
        return parseTitle(dokumentbeskrivelse, input, title.getDocuments());
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
