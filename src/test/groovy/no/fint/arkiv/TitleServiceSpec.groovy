package no.fint.arkiv

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource
import no.fint.model.resource.arkiv.noark.JournalpostResource
import spock.lang.Specification

class TitleServiceSpec extends Specification {
    TitleService titleService
    CaseProperties.Title title

    void setup() {
        def substitutorService = new SubstitutorService(Mock(LinkResolver))
        titleService = new TitleService(substitutorService)
        title = new CaseProperties.Title(
                cases: '${kallesignal} - ${fartoyNavn} - Tilskudd - ${kulturminneId} - ${soknadsnummer.identifikatorverdi}',
                records: '${kallesignal} - ${fartoyNavn}:',
                documents: '${saksaar}/${sakssekvensnummer} --')
    }

    def "Format title from object"() {
        given:
        def r = new TilskuddFartoyResource(
                soknadsnummer: new Identifikator(identifikatorverdi: '12345'),
                fartoyNavn: 'Hestmann',
                kallesignal: 'XXYYZ',
                kulturminneId: '22334455-1'
        )

        when:
        def t = titleService.getCaseTitle(title, r)
        println(t)

        then:
        t == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
    }

    def "Set values from title"() {
        given:
        def t = 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleService.parseCaseTitle(title, r, t)
        println(r)

        then:
        r.fartoyNavn == 'Hestmann'
        r.soknadsnummer.identifikatorverdi == '12345'
    }

    def "Invalid title format"() {
        given:
        def t = 'Tilskudd - LDQT - Gamle Lofotferga - 139136-1 - 14812 - 14812 - TEST integrasjon FINT fartøy'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleService.parseCaseTitle(title, r, t)
        println(r)

        then:
        noExceptionThrown()
    }

    def 'Valid title format'() {
        given:
        def t = 'LDQT - Gamle Lofotferga - Tilskudd - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        def result = titleService.parseCaseTitle(title, r, t)
        println(r)

        then:
        result
        r.fartoyNavn == 'Gamle Lofotferga'
        r.kallesignal == 'LDQT'
        r.soknadsnummer.identifikatorverdi == '14812'
    }

    def 'Produce and parse case titles with a format containing {title}'() {
        given:
        def myTitle = new CaseProperties.Title(
                'cases': '${kallesignal} - ${fartoyNavn} - Tilskudd - ${kulturminneId} - ${soknadsnummer.identifikatorverdi}: ${tittel}')
        def r = new TilskuddFartoyResource(
                soknadsnummer: new Identifikator(identifikatorverdi: '12345'),
                fartoyNavn: 'Hestmann',
                kallesignal: 'XXYYZ',
                kulturminneId: '22334455-1',
                tittel: 'Hei og hallo'
        )

        when:
        def t = titleService.getCaseTitle(myTitle, r)

        then:
        t == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345: Hei og hallo'

        when:
        r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())
        titleService.parseCaseTitle(myTitle, r, t)

        then:
        r.kallesignal == 'XXYYZ'
        r.fartoyNavn == 'Hestmann'
        r.soknadsnummer.identifikatorverdi == '12345'
        r.kulturminneId == '22334455-1'
        r.tittel == 'Hei og hallo'

    }

    def 'Format All Titles'() {
        given:
        def r = new TilskuddFartoyResource(
                soknadsnummer: new Identifikator(identifikatorverdi: '12345'),
                saksaar: '2020',
                sakssekvensnummer: '12',
                fartoyNavn: 'Hestmann',
                kallesignal: 'XXYYZ',
                kulturminneId: '22334455-1',
                tittel: 'Hei og hallo',
                journalpost: [
                        new JournalpostResource(
                                tittel: 'Vedtak om tilskudd',
                                journalPostnummer: 14,
                                dokumentbeskrivelse: [
                                        new DokumentbeskrivelseResource(
                                                tittel: 'Vedtaksbrev'
                                        )
                                ]
                        )
                ]
        )

        when:
        def sak = titleService.getCaseTitle(title, r)
        def journalpost = r.journalpost.collect { titleService.getRecordTitlePrefix(title, r) + it.tittel }
        def dokument = r.journalpost.collect {
            j ->
                j.dokumentbeskrivelse.collect {
                    titleService.getDocumentTitlePrefix(title, r) + it.tittel
                }
        }.flatten()

        then:
        sak == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
        journalpost.every { it == 'XXYYZ - Hestmann: Vedtak om tilskudd' }
        dokument.every { it == '2020/12 -- Vedtaksbrev' }
    }

    def 'Return false if title does not match pattern'() {
        given:
        def t = 'Tilskudd fartøy: LDQT - Gamle Lofotferga - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        def result = titleService.parseCaseTitle(title, r, t)

        then:
        !result
    }

    def 'Return true if title matches pattern'() {
        given:
        def t = 'LDQT - Gamle Lofotferga - Tilskudd - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        def result = titleService.parseCaseTitle(title, r, t)

        then:
        result
    }

    def 'Expression language test'() {
        given:
        def title = new CaseProperties.Title(
                cases: '${kallesignal} - ${fartoyNavn} - Tilskudd - ${kulturminneId} - ${soknadsnummer.identifikatorverdi}',
                records: '#{tittel.replaceFirst("Tilskudd Kapittel.*","")}',
                documents: '${saksaar}/${sakssekvensnummer} --')

        def r = new TilskuddFartoyResource(
                soknadsnummer: new Identifikator(identifikatorverdi: '12345'),
                fartoyNavn: 'Hestmann',
                kallesignal: 'XXYYZ',
                kulturminneId: '22334455-1',
                tittel: 'Karmøy – 167/1630 – Kråkeslottet – 86010-1 - Tilskudd Kapittel 1429 post 71 og dispensasjoner'
        )

        when:
        def result = titleService.getRecordTitlePrefix(title, r)
        print('"' + result + '"')

        then:
        noExceptionThrown()
        result =~ /^Karmøy/
        result =~ /86010-1 - /
    }
}
