package no.fint.arkiv

import no.fint.model.arkiv.kodeverk.*
import no.fint.model.resource.Link
import no.fint.model.resource.arkiv.kulturminnevern.TilskuddFredaBygningPrivatEieResource
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource
import no.fint.model.resource.arkiv.noark.JournalpostResource
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource
import no.fint.model.resource.arkiv.noark.SakResource
import no.fint.model.resource.felles.kompleksedatatyper.MatrikkelnummerResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(classes = [CodingSystemDefaults, CodingSystemService, NoarkMetadataService, TestCaseDefaultsService, CaseDefaults])
@ActiveProfiles(['spock'])
class CodingSystemSpec extends Specification {

    @Autowired
    CodingSystemDefaults codingSystemDefaults

    @Autowired
    CodingSystemService codingSystemService

    @Autowired
    NoarkMetadataService noarkMetadataService

    @Autowired
    TestCaseDefaultsService caseDefaultsService

    @Autowired
    CaseDefaults caseDefaults

    def 'Verify all metadata streams are supported'() {
        expect:
        noarkMetadataService.saksStatus.anyMatch {it.navn == 'Under behandling'}
        noarkMetadataService.journalStatus.anyMatch {it.navn == 'Godkjent av leder'}
        noarkMetadataService.dokumentStatus.anyMatch {it.navn == 'Dokumentet er ferdigstilt'}
        noarkMetadataService.journalpostType.anyMatch {it.navn == 'Inngående dokument'}
        noarkMetadataService.dokumentType.anyMatch {it.navn == 'Brev'}
        noarkMetadataService.korrespondansepartType.anyMatch {it.navn == 'Intern avsender'}
        noarkMetadataService.tilknyttetRegistreringSom.anyMatch {it.navn == 'Hoveddokument'}
        noarkMetadataService.partRolle.anyMatch {it.navn == 'Klient'}
        noarkMetadataService.variantformat.anyMatch {it.navn == 'Arkivformat'}
        noarkMetadataService.tilgangsrestriksjon.anyMatch { it.navn == 'Unntatt etter offentleglova'}
        noarkMetadataService.skjermingshjemmel.allMatch { it.navn.contains('§')}
    }

    def "Able to get definition of metadata M082"() {
        expect:
        noarkMetadataService.metadata['journalposttype'].kode == 'M082'
        noarkMetadataService.metadata['journalposttype'].obligatorisk
        noarkMetadataService.metadata['journalposttype'].verdier['I'] == 'Inngående dokument'
    }

    def 'Able to update Journalpost code value links'() {
        given:
        def korrespondansepart = new KorrespondansepartResource()
        def journalpost = new JournalpostResource(
                korrespondansepart: [
                        korrespondansepart
                ]
        )

        when:
        korrespondansepart.addKorrespondanseparttype(Link.with(KorrespondansepartType, 'systemid', 'EA'))
        journalpost.addJournalstatus(Link.with(JournalStatus, 'systemid', 'G'))
        codingSystemService.mapCodingSystemLinks(journalpost)
        codingSystemService.mapCodingSystemLinks(korrespondansepart)

        then:
        journalpost.getJournalstatus().every {it.href.endsWith('/42')}
        journalpost.korrespondansepart.every {it.getKorrespondanseparttype().every {it.href.endsWith('/396')}}
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

    def 'Able to apply defaults to case'() {
        given:
        def resource = new TilskuddFredaBygningPrivatEieResource(
                matrikkelnummer: new MatrikkelnummerResource(
                        gardsnummer: '123',
                        bruksnummer: '456'
                )
        )

        when:
        caseDefaultsService.applyDefaultsForCreation(caseDefaults.getTilskuddfredabygningprivateie(), resource)
        println(resource)

        then:
        resource.klasse.any {it.klasseId=='123/456' }
    }

}
