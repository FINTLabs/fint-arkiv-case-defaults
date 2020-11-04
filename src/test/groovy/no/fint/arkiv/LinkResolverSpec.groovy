package no.fint.arkiv

import no.fint.model.resource.Link
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource
import no.fint.model.resource.felles.kodeverk.FylkeResource
import no.fint.model.resource.felles.kodeverk.KommuneResource
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource
import spock.lang.Specification

class LinkResolverSpec extends Specification {

    def 'Test default resolution of List<Link> attributes'() {
        given:
        def resolver = Mock(LinkResolver)
        def titleService = new TitleService(resolver)
        def title = new Title(cases: '${bygningsnavn} – ${matrikkelnummer.gardsnummer}/${matrikkelnummer.bruksnummer} – Tilskudd – ${link:matrikkelnummer.kommunenummer#navn}, ${link:matrikkelnummer.kommunenummer#link:fylke#navn} – ${kulturminneId}')
        def r = new TilskuddFredaBygningPrivatEieResource(
                matrikkelnummer: new MatrikkelnummerResource(
                        gardsnummer: '1234',
                        bruksnummer: '56'
                ),
                saksaar: '2020',
                kulturminneId: '223344-5',
                bygningsnavn: 'Villa Panderosa'
        )
        r.matrikkelnummer.addKommunenummer(Link.with('https://api.felleskomponent.no/felles/kodeverk/kommune/systemid/3005'))

        when:
        def result = titleService.getCaseTitle(title, r)

        then:
        result == 'Villa Panderosa – 1234/56 – Tilskudd – Drammen, Viken – 223344-5'
        noExceptionThrown()
        2 * resolver.resolve({ it.href.contains('/kommune/') }) >> new KommuneResource(
                kode: '3005',
                navn: 'Drammen',
                links: [
                        'fylke': [
                                Link.with('https://api.felleskomponent.no/felles/kodeverk/fylke/systemid/30')
                        ]
                ]
        )
        1 * resolver.resolve({ it.href.contains('/fylke/') }) >> new FylkeResource(
                kode: '30',
                navn: 'Viken'
        )
    }
}
