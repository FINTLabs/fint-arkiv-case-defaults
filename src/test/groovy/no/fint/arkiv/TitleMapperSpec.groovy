package no.fint.arkiv

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource
import no.fint.model.resource.arkiv.noark.JournalpostResource
import spock.lang.Specification

class TitleMapperSpec extends Specification {
    TitleMapper titleMapper

    void setup() {
        titleMapper = new TitleMapper('tilskuddfrip', new Title(
                cases: '${kallesignal} - ${fartoyNavn} - Tilskudd - ${kulturminneId} - ${soknadsnummer.identifikatorverdi}',
                records: '${kallesignal} - ${fartoyNavn}: ${tittel}',
                documents: '${saksaar}/${sakssekvensnummer}-${journalPostnummer} -- ${tittel}'
        ), Mock(LinkResolver), false)
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
        def t = titleMapper.getCaseTitle(r)
        println(t)

        then:
        t == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
    }

    def "Set values from title"() {
        given:
        def t = 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleMapper.parseCaseTitle(r, t)
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
        titleMapper.parseCaseTitle(r, t)
        println(r)

        then:
        noExceptionThrown()
    }

    def 'Valid title format'() {
        given:
        def t = 'LDQT - Gamle Lofotferga - Tilskudd - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleMapper.parseCaseTitle(r, t)
        println(r)

        then:
        r.fartoyNavn == 'Gamle Lofotferga'
        r.kallesignal == 'LDQT'
        r.soknadsnummer.identifikatorverdi == '14812'
    }

    def 'Produce and parse case titles with a format containing {title}'() {
        given:
        titleMapper = new TitleMapper('tilskuddfrip', new Title(
                'cases': '${kallesignal} - ${fartoyNavn} - Tilskudd - ${kulturminneId} - ${soknadsnummer.identifikatorverdi}: ${tittel}'
        ), Mock(LinkResolver), false)
        def r = new TilskuddFartoyResource(
                soknadsnummer: new Identifikator(identifikatorverdi: '12345'),
                fartoyNavn: 'Hestmann',
                kallesignal: 'XXYYZ',
                kulturminneId: '22334455-1',
                tittel: 'Hei og hallo'
        )

        when:
        def t = titleMapper.getCaseTitle(r)

        then:
        t == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345: Hei og hallo'

        when:
        r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())
        titleMapper.parseCaseTitle(r, t)

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
        def sak = titleMapper.getCaseTitle(r)
        def journalpost = r.journalpost.collect { titleMapper.getRecordTitle(r, it) }
        def dokument = r.journalpost.collect {
            j ->
                j.dokumentbeskrivelse.collect {
                    titleMapper.getDocumentTitle(r, j, it)
                }
        }.flatten()

        then:
        sak == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
        journalpost.every { it == 'XXYYZ - Hestmann: Vedtak om tilskudd' }
        dokument.every { it == '2020/12-14 -- Vedtaksbrev' }
    }

    def "Parsing when no format defined returns true unless fatal"() {
        given:
        def service = new TitleMapper('tilskuddfrip', new Title(), Mock(LinkResolver), false)

        when:
        def result = service.parseCaseTitle(new TilskuddFartoyResource(), 'Hello there')

        then:
        result
    }

    def 'Parsing when no format defined returns false if fatal'() {
        given:
        def service = new TitleMapper('tilskuddfrip', new Title(), Mock(LinkResolver), true)

        when:
        def result = service.parseCaseTitle(new TilskuddFartoyResource(), 'Hello there')

        then:
        !result
    }

    def 'Return false if title does not match pattern'() {
        given:
        def t = 'Tilskudd fartøy: LDQT - Gamle Lofotferga - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        def result = titleMapper.parseCaseTitle(r, t)

        then:
        !result
    }

    def 'Return true if title matches pattern'() {
        given:
        def t = 'LDQT - Gamle Lofotferga - Tilskudd - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        def result = titleMapper.parseCaseTitle(r, t)

        then:
        result
    }
}
