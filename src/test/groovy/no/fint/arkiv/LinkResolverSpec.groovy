package no.fint.arkiv

import no.fint.model.felles.kodeverk.Fylke
import no.fint.model.resource.Link
import no.fint.model.resource.felles.kodeverk.FylkeResource
import no.fint.model.resource.felles.kodeverk.KommuneResource
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFredaHusPrivatEieResource
import spock.lang.Specification

class LinkResolverSpec extends Specification {

    def 'Test default resolution of List<Link> attributes'() {
        given:
        def resolver = Mock(LinkResolver)
        def titleService = new TitleService(resolver, new CustomFormats(title: [
                'tilskuddfredahusprivateie': '{bygningsnavn} – ${matrikkelnummer.gardsnummer}/${matrikkelnummer.bruksnummer} – {gateadresse} - Tilskudd – ${saksaar} - ${link:matrikkelnummer.kommunenummer#navn}, ${link:matrikkelnummer.kommunenummer#link:fylke#navn} – ${kulturminneId}'
        ]))
        def r = new TilskuddFredaHusPrivatEieResource(
                matrikkelnummer: new MatrikkelnummerResource(
                        gardsnummer: '1234',
                        bruksnummer: '56'
                ),
                saksaar: '2020',
                kulturminneId: '223344-5'
        )
        r.matrikkelnummer.addKommunenummer(Link.with('https://api.felleskomponent.no/felles/kodeverk/kommune/systemid/3005'))

        when:
        def title = titleService.getTitle(r)
        println(title)

        then:
        noExceptionThrown()
        2 * resolver.resolve({it.href.contains('/kommune/')}) >> new KommuneResource(
                kode: '3005',
                navn: 'Drammen',
                links: [
                        'fylke': [
                                Link.with('https://api.felleskomponent.no/felles/kodeverk/fylke/systemid/30')
                        ]
                ]
        )
        1 * resolver.resolve({it.href.contains('/fylke/')}) >> new FylkeResource(
                kode: '30',
                navn: 'Viken'
        )
    }
}
