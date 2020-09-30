package no.fint.arkiv;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.text.lookup.StringLookup;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanPropertyLookup<T> implements StringLookup {

    private final List<T> beans;

    public BeanPropertyLookup(T... beans) {
        this.beans = Arrays.stream(beans).collect(Collectors.toList());
    }

    @Override
    public String lookup(String key) {
        try {
            for (T bean : beans) {
                try {
                    return String.valueOf(PropertyUtils.getProperty(bean, key));
                } catch (NoSuchMethodException ignore) {
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
