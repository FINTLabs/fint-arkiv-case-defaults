package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.novari.fint.model.resource.Link;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.lookup.StringLookup;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class BeanPropertyLookup<T> implements StringLookup {

    private final LinkResolver resolver;
    private final T bean;

    public BeanPropertyLookup(LinkResolver resolver, T bean) {
        this.resolver = resolver;
        this.bean = bean;
    }

    @Override
    public String lookup(String key) {
        try {
            return getProperty(bean, key);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
            throw new RuntimeException(e);
        }
    }

    private String getProperty(Object target, String key) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        log.trace("Lookup {} on {}", key, target);
        if (StringUtils.startsWith(key, "link$")) {
            String linkProperty = StringUtils.substringBetween(key, "link$", "#");
            String targetProperty = StringUtils.substringAfter(key, "#");
            final Object property = PropertyUtils.getProperty(target, linkProperty);
            if (property == null) {
                return "";
            } else if (property instanceof List) {
                List<?> list = (List<?>) property;
                if (list.isEmpty()) {
                    return "";
                } else if (list.get(0) instanceof Link) {
                    return getProperty(resolver.resolve((Link) ((List<?>) property).get(0)), targetProperty);
                } else {
                    throw new IllegalArgumentException(linkProperty + " does not resolve to a Link");
                }
            } else if (property instanceof Link) {
                return getProperty(resolver.resolve((Link) property), targetProperty);
            } else {
                throw new IllegalArgumentException(linkProperty + " does not resolve to a Link");
            }
        }
        return Objects.toString(PropertyUtils.getProperty(target, key), "");
    }

}
