package no.fint.arkiv;

import no.novari.fint.model.resource.Link;

public interface LinkResolver {
    Object resolve(Link link);
}

