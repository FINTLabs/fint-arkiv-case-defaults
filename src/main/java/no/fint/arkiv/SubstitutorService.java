package no.fint.arkiv;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

@Service
public class SubstitutorService {
    private final LinkResolver linkResolver;

    public SubstitutorService(LinkResolver linkResolver) {
        this.linkResolver = linkResolver;
    }

    public <T> StringSubstitutor getSubstitutorForResource(T resource) {
        return new StringSubstitutor(new BeanPropertyLookup<>(linkResolver, resource));
    }
}
