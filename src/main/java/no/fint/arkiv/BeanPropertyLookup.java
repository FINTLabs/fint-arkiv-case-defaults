package no.fint.arkiv;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.text.lookup.StringLookup;

import java.lang.reflect.InvocationTargetException;

public class BeanPropertyLookup<T> implements StringLookup {

    private final T bean;

    public BeanPropertyLookup(T bean) {
        this.bean = bean;
    }

    @Override
    public String lookup(String key) {
        try {
            final Object value = PropertyUtils.getProperty(bean, key);
            if (value == null) {
                return "";
            }
            return value.toString();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
