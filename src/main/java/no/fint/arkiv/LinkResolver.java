package no.fint.arkiv;

import no.fint.model.resource.Link;

public interface LinkResolver {
    Object resolve(Link link);
}

