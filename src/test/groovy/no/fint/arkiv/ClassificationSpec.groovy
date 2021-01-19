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
                        1: new CaseProperties.Klasse(
                                setOrdning: 'EMNE',
                                verdi: '223'
                        ),
                        2: new CaseProperties.Klasse(
                                setOrdning: 'EMNE',
                                verdi: 'C50'
                        ),
                        3: new CaseProperties.Klasse(
                                setOrdning: 'EMNE',
                                verdi: 'C53'
                        ),
                        4: new CaseProperties.Klasse(
                                setOrdning: 'GBNR',
                                verdi: '${matrikkelnummer.gardsnummer}/${matrikkelnummer.bruksnummer}'
                        )
                ]
        )

        when:
        service.applyDefaultsForCreation(p, r)
        print(r)

        then:
        !r.klasse.isEmpty()
        r.klasse.size() == 4
        r.klasse.any { it.tittel == '112/34' }
    }
}
