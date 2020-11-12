package no.fint.arkiv;

import no.fint.model.resource.Link;
import org.springframework.stereotype.Component;

@Component
public class TestLinkResolver implements LinkResolver {
    @Override
    public Object resolve(Link link) {
        return "dummy";
    }
}
