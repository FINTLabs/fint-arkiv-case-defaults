package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.administrasjon.arkiv.DokumentbeskrivelseResource;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.administrasjon.arkiv.RegistreringResource;
import no.fint.model.resource.administrasjon.arkiv.SaksmappeResource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TitleMapper {
    private final Title title;

    public TitleMapper(@NotNull Title title) {
        this.title = title;
    }

    public String getCaseTitle(SaksmappeResource saksmappe) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(saksmappe)).replace(title.getCases());
        log.debug("Case title: '{}'", result);
        return result;
    }

    public String getRecordTitle(SaksmappeResource saksmappe, RegistreringResource registrering) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(registrering, saksmappe)).replace(title.getRecords());
        log.debug("Record title: '{}'", result);
        return result;
    }

    public String getDocumentTitle(SaksmappeResource saksmappe, RegistreringResource registering, DokumentbeskrivelseResource dokumentbeskrivelse) {
        String result = new StringSubstitutor(new BeanPropertyLookup<>(dokumentbeskrivelse, registering, saksmappe)).replace(title.getDocuments());
        log.debug("Document title: '{}'", result);
        return result;
    }

    public void parseCaseTitle(SaksmappeResource saksmappe, String input) {
        parseTitle(saksmappe, input, title.getCases());
    }

    public void parseRecordTitle(RegistreringResource registering, String input) {
        parseTitle(registering, input, title.getRecords());
    }

    public void parseDocumentTitle(DokumentbeskrivelseResource dokumentbeskrivelse, String input) {
        parseTitle(dokumentbeskrivelse, input, title.getDocuments());
    }

    private void parseTitle(Object object, String title, String format) {
        if (StringUtils.isBlank(format)) {
            log.debug("No format defined for {}", TitleService.resourceName(object));
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
                    log.debug("Setting property {} to {}", nameList.get(i - 1), titleMatcher.group(i));
                    BeanUtils.setProperty(object, nameList.get(i - 1), titleMatcher.group(i));
                } catch (IllegalAccessException | InvocationTargetException ignore) {
                    log.debug("Unable to set property {}", nameList.get(i - 1));
                }
            }
        }
    }

}
