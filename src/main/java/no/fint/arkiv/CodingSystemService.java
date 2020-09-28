package no.fint.arkiv;

import org.springframework.stereotype.Service;

@Service
public class CodingSystemService {
    private final CodingSystemDefaults codingSystemDefaults;
    private final NoarkMetadataService noarkMetadataService;

    public CodingSystemService(CodingSystemDefaults codingSystemDefaults, NoarkMetadataService noarkMetadataService) {
        this.codingSystemDefaults = codingSystemDefaults;
        this.noarkMetadataService = noarkMetadataService;
    }

}
