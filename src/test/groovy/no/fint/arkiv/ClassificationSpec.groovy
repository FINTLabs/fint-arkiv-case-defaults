package no.fint.arkiv

import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource
import no.novari.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource
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
                                ordning: 'EMNE',
                                verdi: '223'
                        ),
                        2: new CaseProperties.Klasse(
                                ordning: 'EMNE',
                                verdi: 'C50'
                        ),
                        3: new CaseProperties.Klasse(
                                ordning: 'EMNE',
                                verdi: 'C53'
                        ),
                        4: new CaseProperties.Klasse(
                                ordning: 'GBNR',
                                verdi: '${matrikkelnummer.gardsnummer}/${matrikkelnummer.bruksnummer}',
                                tittel: '${matrikkelnummer.gardsnummer}/${matrikkelnummer.bruksnummer} ${bygningsnavn}'
                        )
                ]
        )

        when:
        service.applyDefaultsForCreation(p, r)
        print(r)

        then:
        !r.klasse.isEmpty()
        r.klasse.size() == 4
        r.klasse.any { it.klasseId == '112/34' && it.tittel == '112/34 Kråkeslottet' }
    }
}
