package no.fint.arkiv

import no.fint.model.administrasjon.arkiv.DokumentStatus
import no.fint.model.administrasjon.arkiv.JournalStatus
import no.fint.model.administrasjon.arkiv.Saksstatus
import no.fint.model.administrasjon.arkiv.TilknyttetRegistreringSom
import no.fint.model.resource.Link
import no.fint.model.resource.administrasjon.arkiv.DokumentbeskrivelseResource
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource
import no.fint.model.resource.administrasjon.arkiv.SakResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.util.stream.Collectors

@SpringBootTest(classes = [CodingSystemDefaults, CodingSystemService, NoarkMetadataService])
@ActiveProfiles(['spock'])
class CodingSystemSpec extends Specification {

    @Autowired
    CodingSystemDefaults codingSystemDefaults

    @Autowired
    CodingSystemService codingSystemService

    @Autowired
    NoarkMetadataService noarkMetadataService

    def "Able to supply stream of JournalpostType"() {
        when:
        def result = noarkMetadataService.journalpostType.collect(Collectors.toList())

        then:
        result.any { it.navn == 'Inngående dokument' }
    }

    def "Able to supply stream of TilknyttetRegistreringSom"() {
        when:
        def result = noarkMetadataService.tilknyttetRegistreringSom.collect(Collectors.toList())

        then:
        result.any { it.navn == 'Hoveddokument' }
    }

    def 'Able to supply stream of KorrespondansepartType'() {
        when:
        def result = noarkMetadataService.korrespondansepartType.collect(Collectors.toList())

        then:
        result.any { it.navn == 'Avsender' }
    }

    def "Able to get definition of metadata M082"() {
        expect:
        noarkMetadataService.metadata['journalposttype'].kode == 'M082'
        noarkMetadataService.metadata['journalposttype'].obligatorisk
        noarkMetadataService.metadata['journalposttype'].verdier['I'] == 'Inngående dokument'
    }

    def 'Able to update Journalpost code value links'() {
        given:
        def journalpost = new JournalpostResource()

        when:
        journalpost.addJournalstatus(Link.with(JournalStatus, 'systemid', 'F'))
        codingSystemService.mapCodingSystemLinks(journalpost)

        then:
        journalpost.getJournalstatus().any {it.href.endsWith('/42')}
    }

    def 'Able to update Saksmappe code value links'() {
        given:
        def saksmappe = new SakResource()

        when:
        saksmappe.addSaksstatus(Link.with(Saksstatus, 'systemid', 'B'))
        codingSystemService.mapCodingSystemLinks(saksmappe)

        then:
        saksmappe.getSaksstatus().any {it.href.endsWith('/997')}
    }

    def 'Able to update Dokumentbeskrivelse code value links'() {
        given:
        def dokument = new DokumentbeskrivelseResource()

        when:
        dokument.addDokumentstatus(Link.with(DokumentStatus, 'systemid', 'B'))
        dokument.addTilknyttetRegistreringSom(Link.with(TilknyttetRegistreringSom, 'systemid', 'H'))
        codingSystemService.mapCodingSystemLinks(dokument)

        then:
        dokument.getDokumentstatus().any {it.href.endsWith('/12')}
        dokument.getTilknyttetRegistreringSom().any {it.href.endsWith('/1')}
    }

}
