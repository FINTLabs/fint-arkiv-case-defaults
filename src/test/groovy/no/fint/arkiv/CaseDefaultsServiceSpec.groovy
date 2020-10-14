package no.fint.arkiv

import no.fint.model.arkiv.kodeverk.DokumentStatus
import no.fint.model.arkiv.kodeverk.DokumentType
import no.fint.model.arkiv.kodeverk.JournalStatus
import no.fint.model.arkiv.kodeverk.JournalpostType
import no.fint.model.arkiv.kodeverk.KorrespondansepartType
import no.fint.model.arkiv.kodeverk.PartRolle
import no.fint.model.arkiv.kodeverk.Saksstatus
import no.fint.model.arkiv.kodeverk.TilknyttetRegistreringSom
import no.fint.model.arkiv.kodeverk.Variantformat
import no.fint.model.resource.Link
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource
import no.fint.model.resource.arkiv.noark.DokumentobjektResource
import no.fint.model.resource.arkiv.noark.JournalpostResource
import no.fint.model.resource.arkiv.noark.KorrespondansepartResource
import no.fint.model.resource.arkiv.noark.PartResource
import no.fint.model.resource.arkiv.noark.SakResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(classes = [CodingSystemService, CodingSystemDefaults, NoarkMetadataService])
@ActiveProfiles(['spock'])
class CaseDefaultsServiceSpec extends Specification {
    @Autowired
    CodingSystemService codingSystemService

    def 'Applies both noark defaults and case defaults'() {
        given:
        def service = new CaseDefaultsService() {}
        service.codingSystemService = codingSystemService

        def dokobj = new DokumentobjektResource()
        dokobj.addVariantFormat(Link.with(Variantformat, 'systemid', 'P'))
        def db = new DokumentbeskrivelseResource(
                dokumentobjekt: [dokobj]
        )
        db.addTilknyttetRegistreringSom(Link.with(TilknyttetRegistreringSom, 'systemid', 'H'))
        db.addDokumentstatus(Link.with(DokumentStatus, 'systemid', 'B'))
        db.addDokumentType(Link.with(DokumentType, 'systemid', 'B'))
        def kp = new KorrespondansepartResource()
        kp.addKorrespondanseparttype(Link.with(KorrespondansepartType, 'systemid', 'EA'))
        def jp = new JournalpostResource(
                korrespondansepart: [kp],
                dokumentbeskrivelse: [db]
        )
        jp.addJournalposttype(Link.with(JournalpostType, 'systemid', 'I'))
        jp.addJournalstatus(Link.with(JournalStatus, 'systemid', 'G'))
        def part = new PartResource()
        part.addPartRolle(Link.with(PartRolle, 'systemid', 'KLI'))
        def sak = new SakResource(
                part: [part],
                journalpost: [jp]
        )
        sak.addSaksstatus(Link.with(Saksstatus, 'systemid', 'B'))

        def props = new CaseProperties()

        when:


        service.applyDefaultsForCreation(props, sak)

        then:
        sak.getSaksstatus().every { it.href.endsWith('/997') }
        sak.part.every { it.getPartRolle().every { it.href.endsWith('/43') } }
        sak.journalpost.every {
            it.getJournalstatus().every { it.href.endsWith('/42') } &&
                    it.getJournalposttype().every { it.href.endsWith('/110') } &&
                    it.dokumentbeskrivelse.every {
                        it.getDokumentstatus().every { it.href.endsWith('/12') } &&
                                it.getDokumentType().every { it.href.endsWith('/44') } &&
                                it.getTilknyttetRegistreringSom().every { it.href.endsWith('/1') } &&
                                it.dokumentobjekt.every {
                                    it.getVariantFormat().every { it.href.endsWith('/99') }
                                }
                    }
        }
    }
}
