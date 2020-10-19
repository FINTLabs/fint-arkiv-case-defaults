package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.arkiv.kodeverk.*;
import no.fint.model.arkiv.noark.AdministrativEnhet;
import no.fint.model.arkiv.noark.Arkivdel;
import no.fint.model.arkiv.noark.Klassifikasjonssystem;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.KlasseResource;
import no.fint.model.resource.arkiv.noark.SaksmappeResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public abstract class CaseDefaultsService {

    @Autowired
    protected CodingSystemService codingSystemService;

    public void applyDefaultsForCreation(CaseProperties properties, SaksmappeResource resource) {
        if (properties == null) {
            return;
        }
        if (isNotBlank(properties.getSaksstatus()) && isEmpty(resource.getSaksstatus())) {
            resource.addSaksstatus(Link.with(
                    Saksstatus.class,
                    "systemid",
                    properties.getSaksstatus()
            ));
        }
        if (isNotBlank(properties.getArkivdel()) && isEmpty(resource.getArkivdel())) {
            resource.addArkivdel(Link.with(
                    Arkivdel.class,
                    "systemid",
                    properties.getArkivdel()
            ));
        }
        if (isNotBlank(properties.getJournalenhet()) && isEmpty(resource.getJournalenhet())) {
            resource.addJournalenhet(Link.with(
                    AdministrativEnhet.class,
                    "systemid",
                    properties.getJournalenhet()
            ));
        }
        if (isNotBlank(properties.getAdministrativEnhet()) && isEmpty(resource.getAdministrativEnhet())) {
            resource.addAdministrativEnhet(Link.with(
                    AdministrativEnhet.class,
                    "systemid",
                    properties.getAdministrativEnhet()
            ));
        }
        if (!isEmpty(properties.getKlassifikasjon()) && !isEmpty(properties.getKlasse())
                && isEmpty(resource.getKlasse())) {
            resource.setKlasse(
                    IntStream.range(0, properties.getKlasse().length)
                            .mapToObj(i -> {
                                String klassifikasjon = properties.getKlassifikasjon()[Math.min(properties.getKlassifikasjon().length - 1, i)];
                                String klasse = properties.getKlasse()[i];
                                KlasseResource result = new KlasseResource();
                                result.setRekkefolge(i + 1);
                                result.setKlasseId(klasse);
                                result.addKlassifikasjonssystem(Link.with(Klassifikasjonssystem.class, "systemid", klassifikasjon));
                                return result;
                            }).collect(Collectors.toList()));
        }
        applyDefaultsForUpdate(properties, resource);
    }

    public void applyDefaultsForUpdate(CaseProperties properties, SaksmappeResource resource) {
        codingSystemService.mapCodingSystemLinks(resource);
        if (!isEmpty(resource.getPart())) {
            resource.getPart().forEach(codingSystemService::mapCodingSystemLinks);
        }
        if (properties == null) {
            return;
        }
        if (isEmpty(resource.getJournalpost())) {
            return;
        }
        resource.getJournalpost().forEach(journalpost -> {
            codingSystemService.mapCodingSystemLinks(journalpost);
            journalpost.getKorrespondansepart().forEach(korrespondanse -> {
                codingSystemService.mapCodingSystemLinks(korrespondanse);
                if (isNotBlank(properties.getKorrespondansepartType()) && isEmpty(korrespondanse.getKorrespondanseparttype())) {
                    korrespondanse.addKorrespondanseparttype(Link.with(
                            KorrespondansepartType.class,
                            "systemid",
                            properties.getKorrespondansepartType()));
                }
            });
            journalpost.getDokumentbeskrivelse().forEach(dokumentbeskrivelse -> {
                codingSystemService.mapCodingSystemLinks(dokumentbeskrivelse);
                if (dokumentbeskrivelse.getDokumentobjekt() != null) {
                    dokumentbeskrivelse.getDokumentobjekt().forEach(codingSystemService::mapCodingSystemLinks);
                }
                if (isNotBlank(properties.getDokumentstatus()) && isEmpty(dokumentbeskrivelse.getDokumentstatus())) {
                    dokumentbeskrivelse.addDokumentstatus(Link.with(
                            DokumentStatus.class,
                            "systemid",
                            properties.getDokumentstatus()
                    ));
                }
                if (isNotBlank(properties.getDokumentType()) && isEmpty(dokumentbeskrivelse.getDokumentType())) {
                    dokumentbeskrivelse.addDokumentType(Link.with(
                            DokumentType.class,
                            "systemid",
                            properties.getDokumentType()
                    ));
                }
                if (isNotBlank(properties.getTilknyttetRegistreringSom()) && isEmpty(dokumentbeskrivelse.getTilknyttetRegistreringSom())) {
                    dokumentbeskrivelse.addTilknyttetRegistreringSom(Link.with(
                            TilknyttetRegistreringSom.class,
                            "systemid",
                            properties.getTilknyttetRegistreringSom()
                    ));
                }
            });
            if (isNotBlank(properties.getJournalpostType()) && isEmpty(journalpost.getJournalposttype())) {
                journalpost.addJournalposttype(Link.with(
                        JournalpostType.class,
                        "systemid",
                        properties.getJournalpostType()));
            }
            if (isNotBlank(properties.getJournalstatus()) && isEmpty(journalpost.getJournalstatus())) {
                journalpost.addJournalstatus(Link.with(
                        JournalStatus.class,
                        "systemid",
                        properties.getJournalstatus()));
            }
            if (isNotBlank(properties.getJournalenhet()) && isEmpty(journalpost.getJournalenhet())) {
                journalpost.addJournalenhet(Link.with(
                        AdministrativEnhet.class,
                        "systemid",
                        properties.getJournalenhet()
                ));
            }
            if (isNotBlank(properties.getAdministrativEnhet()) && isEmpty(journalpost.getAdministrativEnhet())) {
                journalpost.addAdministrativEnhet(Link.with(
                        AdministrativEnhet.class,
                        "systemid",
                        properties.getAdministrativEnhet()
                ));
            }
            if (isNotBlank(properties.getArkivdel()) && isEmpty(journalpost.getArkivdel())) {
                journalpost.addArkivdel(Link.with(
                        Arkivdel.class,
                        "systemid",
                        properties.getArkivdel()
                ));
            }
        });
    }

    private static boolean isEmpty(List<?> list) {
        return Objects.isNull(list) || list.isEmpty();
    }

    private static <T> boolean isEmpty(T[] array) {
        return Objects.isNull(array) || array.length == 0;
    }

}
