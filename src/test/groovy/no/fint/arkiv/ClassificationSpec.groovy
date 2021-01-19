package no.fint.arkiv

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource
import spock.lang.Specification

class ClassificationSpec extends Specification {

    CaseDefaultsService service

    def setup() {
        service = new CaseDefaultsService() {}
        service.substitutorService = new SubstitutorService(Mock(LinkResolver))
    }

    def 'Able to create classifications with subsitutions'() {
        given:
        def r = new TilskuddFredaBygningPrivatEieResource(
                bygningsnavn: 'Kråkeslottet',
                soknadsnummer: new Identifikator(identifikatorverdi: '12345'),
                kulturminneId: '443322-1',
                matrikkelnummer: new MatrikkelnummerResource(
                        gardsnummer: '112',
                        bruksnummer: '34'
                )
        )
        def p = new CaseProperties(
                klassifikasjon: [
                        'EMNE': new CaseProperties.Klasse(
                                rekkefolge: 1,
                                verdi: ['223', 'C50', 'C53']
                        ),
                        'GBNR': new CaseProperties.Klasse(
                                rekkefolge: 2,
                                verdi: ['${matrikkelnummer.gardsnummer}/${matrikkelnummer.bruksnummer}']
                        )
                ]
        )

        when:
        service.applyDefaultsForCreation(p, r)
        print(r)

        then:
        !r.klasse.isEmpty()
        r.klasse.size() == 4
        r.klasse.findAll {it.rekkefolge == 2 }.every {it.tittel == '112/34' }
        r.klasse.findAll {it.rekkefolge == 1 }.collect().size() == 3
    }
}
