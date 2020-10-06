package no.fint.arkiv;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.lookup.StringLookup;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BeanPropertyLookup<T> implements StringLookup {

    private final T bean;

    public BeanPropertyLookup(T bean) {
        this.bean = bean;
    }

    @Override
    public String lookup(String key) {
        try {
            if (StringUtils.startsWith(key, "date:")) {
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern(StringUtils.substringAfter(key, "date:")));
            }
            return String.valueOf(PropertyUtils.getProperty(bean, key));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
