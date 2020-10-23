package no.fint.arkiv

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFartoyResource
import spock.lang.Specification

class TitleServiceSpec extends Specification {
    TitleService titleService

    void setup() {
        titleService = new TitleService(new CustomFormats(title: [
                'tilskuddfartoy': '${kallesignal} - ${fartoyNavn} - Tilskudd - ${kulturminneId} - ${soknadsnummer.identifikatorverdi}'
        ]))
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
        def t = titleService.getTitle(r)
        println(t)

        then:
        t == 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
    }

    def "Set values from title"() {
        given:
        def t = 'XXYYZ - Hestmann - Tilskudd - 22334455-1 - 12345'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleService.parseTitle(r, t)
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
        titleService.parseTitle(r, t)
        println(r)

        then:
        noExceptionThrown()
    }

    def 'Valid title format'() {
        given:
        def t = 'LDQT - Gamle Lofotferga - Tilskudd - 139136-1 - 14812'
        def r = new TilskuddFartoyResource(soknadsnummer: new Identifikator())

        when:
        titleService.parseTitle(r, t)
        println(r)

        then:
        r.fartoyNavn == 'Gamle Lofotferga'
        r.kallesignal == 'LDQT'
        r.soknadsnummer.identifikatorverdi == '14812'
    }

    def "No format defined returns null unless fatal"() {
        given:
        def service = new TitleService(new CustomFormats(
                fatal: false,
                title: [:]
        ))

        when:
        def title = service.getTitle(new TilskuddFartoyResource())

        then:
        noExceptionThrown()
        title == null
    }

    def 'No format defined throws exception if fatal'() {
        given:
        def service = new TitleService(new CustomFormats(
                fatal: true,
                title: [:]
        ))

        when:
        service.getTitle(new TilskuddFartoyResource())

        then:
        thrown(IllegalArgumentException)
    }
}
