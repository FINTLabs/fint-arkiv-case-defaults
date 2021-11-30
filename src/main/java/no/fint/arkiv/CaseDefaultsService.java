package no.fint.arkiv;

import lombok.extern.slf4j.Slf4j;
import no.fint.model.arkiv.kodeverk.*;
import no.fint.model.arkiv.noark.AdministrativEnhet;
import no.fint.model.arkiv.noark.Arkivdel;
import no.fint.model.arkiv.noark.Arkivressurs;
import no.fint.model.arkiv.noark.Klassifikasjonssystem;
import no.fint.model.resource.Link;
import no.fint.model.resource.arkiv.noark.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public abstract class CaseDefaultsService {

    @Autowired
    protected CodingSystemService codingSystemService;

    @Autowired
    protected SubstitutorService substitutorService;

    @Autowired
    protected CodingSystemDefaults codingSystemDefaults;


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
        if (isNotBlank(properties.getSaksansvarlig()) && isEmpty(resource.getSaksansvarlig())) {
            resource.addSaksansvarlig(Link.with(
                    Arkivressurs.class,
                    "systemid",
                    properties.getSaksansvarlig()
            ));
        }
        if (!isEmpty(properties.getKlassifikasjon()) && isEmpty(resource.getKlasse())) {
            final StringSubstitutor substitutor = substitutorService.getSubstitutorForResource(resource);
            resource.setKlasse(
                    properties.getKlassifikasjon().entrySet().stream()
                            .map(e -> {
                                final String klassifikasjon = e.getValue().getOrdning();
                                final int rekkefolge = e.getKey();
                                final String klasse = substitutor.replace(e.getValue().getVerdi());
                                final String tittel = substitutor.replace(e.getValue().getTittel());
                                KlasseResource result = new KlasseResource();
                                result.setRekkefolge(rekkefolge);
                                result.setKlasseId(klasse);
                                result.setTittel(isNotBlank(tittel)
                                        ? tittel
                                        : klasse);
                                result.addKlassifikasjonssystem(Link.with(Klassifikasjonssystem.class, "systemid", klassifikasjon));
                                return result;
                            })
                            .collect(Collectors.toList()));
        }

        if (contains(properties.getSkjermingskontekst(), CaseProperties.Skjermingskontekst.SAK)
                && isNoneBlank(properties.getTilgangsrestriksjon(), properties.getSkjermingshjemmel())
                && resource.getSkjerming() == null) {
            SkjermingResource skjerming = new SkjermingResource();
            skjerming.addTilgangsrestriksjon(Link.with(Tilgangsrestriksjon.class, "systemid", properties.getTilgangsrestriksjon()));
            skjerming.addSkjermingshjemmel(Link.with(Skjermingshjemmel.class, "systemid", properties.getSkjermingshjemmel()));
            resource.setSkjerming(skjerming);
        }

        defaultDate(resource::getOpprettetDato, resource::setOpprettetDato);
        defaultDate(resource::getSaksdato, resource::setSaksdato);

        applyDefaultsForUpdate(properties, resource);
    }

    protected void defaultDate(Supplier<Date> getter, Consumer<Date> setter) {
        if (getter.get() == null) {
            setter.accept(new Date());
        }
    }

    public void applyDefaultsForUpdate(CaseProperties properties, SaksmappeResource resource) {
        if (!isEmpty(resource.getPart())) {
            resource.getPart().forEach(codingSystemService::mapCodingSystemLinks);
        }
        if (properties == null) {
            return;
        }
        if (isEmpty(resource.getJournalpost())) {
            return;
        }
        resource.getJournalpost().forEach(journalpost -> applyDefaultsForJournalpost(properties, journalpost));
        codingSystemService.mapCodingSystemLinks(resource);
    }

    protected void applyDefaultsForJournalpost(CaseProperties properties, JournalpostResource journalpost) {
        defaultDate(journalpost::getOpprettetDato, journalpost::setOpprettetDato);
        defaultDate(journalpost::getDokumentetsDato, journalpost::setDokumentetsDato);
        defaultDate(journalpost::getJournalDato, journalpost::setJournalDato);
        defaultDate(journalpost::getMottattDato, journalpost::setMottattDato);
        defaultDate(journalpost::getSendtDato, journalpost::setSendtDato);

        codingSystemService.mapCodingSystemLinks(journalpost);
        journalpost.getKorrespondansepart().forEach(korrespondanse -> {
            if (isNotBlank(properties.getKorrespondansepartType()) && isEmpty(korrespondanse.getKorrespondanseparttype())) {
                korrespondanse.addKorrespondanseparttype(Link.with(
                        KorrespondansepartType.class,
                        "systemid",
                        properties.getKorrespondansepartType()));
            }
            codingSystemService.mapCodingSystemLinks(korrespondanse);
        });
        journalpost.getDokumentbeskrivelse().forEach(dokumentbeskrivelse -> applyDefaultsForDokument(properties, dokumentbeskrivelse));
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

        // The new wine, work in progress ...
        log.debug("The New 🍷 - Work In Progress");
        log.debug("--->\nproperties.getJournalpost(): {}--->\njournalpost.getJournalstatus(): {}--->\njournalpost.getJournalposttype: {}",
                properties.getJournalpost(), journalpost.getJournalstatus(), journalpost.getJournalposttype());

        if(!isEmpty(properties.getJournalpost()) && isEmpty(journalpost.getJournalstatus())) {

            if (journalpost.getJournalposttype().size() != 1) {
                log.warn("There might be several ({}) journalposttype's in this journalpost.. We'll use the first one.",
                        journalpost.getJournalposttype().size());
            }

            String systemid = StringUtils.substringAfterLast(journalpost.getJournalposttype().get(0).getHref(), "/");

            properties.getJournalpost().entrySet().stream().map(e-> {
                final String journalposttype = e.getKey().name();
                final String journalstatus = e.getValue().getStatus();
                log.debug("The elements (not Sikri Elements Cloud): {} (journalposttype), {} (journalstatus)",
                        journalposttype, journalstatus);

                String foo = codingSystemDefaults.getJournalposttype().get(journalposttype);
                log.debug("Configured (system defaults) journalposttype ({}): {}", journalposttype, foo);

                if (StringUtils.isNotBlank(foo) && foo.equalsIgnoreCase(systemid)) {
                    journalpost.addJournalposttype(Link.with(JournalpostType.class, "systemid", foo));
                    log.debug("journalpost.addJournalposttype: {}", foo);

                    journalpost.addJournalstatus(Link.with(JournalStatus.class, "systemid", journalstatus));
                    log.debug("journalpost.addJournalstatus: {}", journalstatus);
                }

                return journalpost;
            }).collect(Collectors.toList());
        }

        if (isNotBlank(properties.getJournalenhet()) && isEmpty(journalpost.getJournalenhet())) {
            journalpost.addJournalenhet(Link.with(
                    AdministrativEnhet.class,
                    "systemid",
                    properties.getJournalenhet()
            ));
        }

        applyDefaultsForRegistrering(properties, journalpost);
        codingSystemService.mapCodingSystemLinks(journalpost);
    }

    protected void applyDefaultsForRegistrering(CaseProperties properties, RegistreringResource registrering) {
        if (isNotBlank(properties.getAdministrativEnhet()) && isEmpty(registrering.getAdministrativEnhet())) {
            registrering.addAdministrativEnhet(Link.with(
                    AdministrativEnhet.class,
                    "systemid",
                    properties.getAdministrativEnhet()
            ));
        }
        if (isNotBlank(properties.getArkivdel()) && isEmpty(registrering.getArkivdel())) {
            registrering.addArkivdel(Link.with(
                    Arkivdel.class,
                    "systemid",
                    properties.getArkivdel()
            ));
        }
        if (isNotBlank(properties.getAdministrativEnhet()) && isEmpty(registrering.getAdministrativEnhet())) {
            registrering.addAdministrativEnhet(Link.with(
                    AdministrativEnhet.class,
                    "systemid",
                    properties.getAdministrativEnhet()
            ));
        }
        if (isNotBlank(properties.getSaksbehandler()) && isEmpty(registrering.getSaksbehandler())) {
            registrering.addSaksbehandler(Link.with(
                    Arkivressurs.class,
                    "systemid",
                    properties.getSaksbehandler()
            ));
        }

        if (contains(properties.getSkjermingskontekst(), CaseProperties.Skjermingskontekst.JOURNALPOST)
                && isNoneBlank(properties.getTilgangsrestriksjon(), properties.getSkjermingshjemmel())
                && registrering.getSkjerming() == null) {
            SkjermingResource skjerming = new SkjermingResource();
            skjerming.addTilgangsrestriksjon(Link.with(Tilgangsrestriksjon.class, "systemid", properties.getTilgangsrestriksjon()));
            skjerming.addSkjermingshjemmel(Link.with(Skjermingshjemmel.class, "systemid", properties.getSkjermingshjemmel()));
            registrering.setSkjerming(skjerming);
        }

    }

    protected void applyDefaultsForDokument(CaseProperties properties, DokumentbeskrivelseResource
            dokumentbeskrivelse) {
        defaultDate(dokumentbeskrivelse::getOpprettetDato, dokumentbeskrivelse::setOpprettetDato);
        defaultDate(dokumentbeskrivelse::getTilknyttetDato, dokumentbeskrivelse::setTilknyttetDato);

        codingSystemService.mapCodingSystemLinks(dokumentbeskrivelse);
        if (dokumentbeskrivelse.getDokumentobjekt() != null) {
            dokumentbeskrivelse.getDokumentobjekt().forEach(dobj -> applyDefaultsForDokumentobjekt(properties, dobj));
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

        if (contains(properties.getSkjermingskontekst(), CaseProperties.Skjermingskontekst.DOKUMENT)
                && isNoneBlank(properties.getTilgangsrestriksjon(), properties.getSkjermingshjemmel())
                && dokumentbeskrivelse.getSkjerming() == null) {
            SkjermingResource skjerming = new SkjermingResource();
            skjerming.addTilgangsrestriksjon(Link.with(Tilgangsrestriksjon.class, "systemid", properties.getTilgangsrestriksjon()));
            skjerming.addSkjermingshjemmel(Link.with(Skjermingshjemmel.class, "systemid", properties.getSkjermingshjemmel()));
            dokumentbeskrivelse.setSkjerming(skjerming);
        }

        codingSystemService.mapCodingSystemLinks(dokumentbeskrivelse);
    }

    protected void applyDefaultsForDokumentobjekt(CaseProperties properties, DokumentobjektResource dokumentobjekt) {
        if (isNotBlank(dokumentobjekt.getFormat()) && isEmpty(dokumentobjekt.getFilformat())) {
            dokumentobjekt.addFilformat(Link.with(
                    Format.class,
                    "systemid",
                    dokumentobjekt.getFormat()
            ));
        }
        if (isNotBlank(properties.getFormat()) && isEmpty(dokumentobjekt.getFilformat())) {
            dokumentobjekt.addFilformat(Link.with(
                    Format.class,
                    "systemid",
                    properties.getFormat()
            ));
        }
        if (isNotBlank(properties.getVariantFormat()) && isEmpty(dokumentobjekt.getVariantFormat())) {
            dokumentobjekt.addVariantFormat(Link.with(
                    Variantformat.class,
                    "systemid",
                    properties.getVariantFormat()
            ));
        }
        codingSystemService.mapCodingSystemLinks(dokumentobjekt);
    }

    protected static <T> boolean contains(T[] array, T value) {
        return Objects.nonNull(array) && Arrays.asList(array).contains(value);
    }

    protected static boolean isEmpty(List<?> list) {
        return Objects.isNull(list) || list.isEmpty();
    }

    protected static <T extends Map> boolean isEmpty(T collection) {
        return Objects.isNull(collection) || collection.isEmpty();
    }

}
