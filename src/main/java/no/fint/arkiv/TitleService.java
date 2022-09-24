package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TitleService {

    private final SubstitutorService substitutorService;

    public TitleService(SubstitutorService substitutorService) {
        this.substitutorService = substitutorService;
    }

    public <T extends SaksmappeResource> String getCaseTitle(CaseProperties.Title title, T saksmappe) {
        if (title == null || StringUtils.isBlank(title.getCases())) {
            return saksmappe.getTittel();
        }
        String result = evaluateExpression(saksmappe, title.getCases());
        log.debug("Title: '{}'", result);
        return result;
    }

    public <T extends SaksmappeResource> String getRecordTitlePrefix(CaseProperties.Title title, T saksmappe) {
        if (title == null || StringUtils.isBlank(title.getRecords())) {
            return "";
        }
        String result = evaluateExpression(saksmappe, title.getRecords());
        log.debug("{} - Record title: '{}'", resourceName(saksmappe), result);
        return prefixFormat(result);
    }

    public <T extends SaksmappeResource> String getDocumentTitlePrefix(CaseProperties.Title title, T saksmappe) {
        if (title == null || StringUtils.isBlank(title.getDocuments())) {
            return "";
        }
        String result = evaluateExpression(saksmappe, title.getDocuments());
        log.debug("{} - Document title: '{}'", resourceName(saksmappe), result);
        return prefixFormat(result);
    }

    public boolean parseCaseTitle(CaseProperties.Title title, SaksmappeResource saksmappe, String input) {
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

        String pattern = RegExUtils.replaceAll(format, names, "(.*)");
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

    private <T extends SaksmappeResource> String evaluateExpression(T saksmappe, String format) {
        if (StringUtils.contains(format, "#{")) {
            return new SpelExpressionParser().parseExpression(format, ParserContext.TEMPLATE_EXPRESSION).getValue(saksmappe, String.class);
        }
        return substitutorService.getSubstitutorForResource(saksmappe).replace(format);
    }

    private String prefixFormat(String result) {
        if (result == null) {
            return "";
        }
        if (result.endsWith(" ")) {
            return result;
        }
        return result + " ";
    }

}
