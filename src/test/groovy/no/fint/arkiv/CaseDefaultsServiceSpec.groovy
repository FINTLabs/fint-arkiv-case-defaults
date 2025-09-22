package no.fint.arkiv

import no.novari.fint.model.arkiv.kodeverk.*
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.arkiv.noark.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(classes = [CodingSystemService, CodingSystemDefaults, NoarkMetadataService, CaseDefaults])
@ActiveProfiles(['spock'])
class CaseDefaultsServiceSpec extends Specification {
    @Autowired
    CodingSystemService codingSystemService

    @Autowired
    CaseDefaults caseDefaults

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

        def props = new CaseProperties(
                skjermingskontekst: [CaseProperties.Skjermingskontekst.JOURNALPOST, CaseProperties.Skjermingskontekst.SAK],
                tilgangsrestriksjon: 'ABC',
                skjermingshjemmel: 'DEF'
        )

        when:
        service.applyDefaultsForCreation(props, sak)

        then:
        sak.getSaksstatus().every { it.href.endsWith('/997') }
        sak.skjerming.tilgangsrestriksjon.every { it.href.endsWith('/ABC') }
        sak.skjerming.skjermingshjemmel.every { it.href.endsWith(('/DEF')) }
        sak.part.every { it.getPartRolle().every { it.href.endsWith('/43') } }
        sak.journalpost.every {
            it.skjerming.tilgangsrestriksjon.every { it.href.endsWith('/ABC') }
            it.skjerming.skjermingshjemmel.every { it.href.endsWith(('/DEF')) }
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

    def "Able to decode skjermingskontekst"() {
        when:
        def props = caseDefaults.tilskuddfartoy
        then:
        props.skjermingskontekst.any { it == CaseProperties.Skjermingskontekst.SAK }
        !props.skjermingskontekst.any { it == CaseProperties.Skjermingskontekst.JOURNALPOST }

        when:
        props = caseDefaults.personalmappe
        then:
        props.skjermingskontekst.any { it == CaseProperties.Skjermingskontekst.SAK }
        props.skjermingskontekst.any { it == CaseProperties.Skjermingskontekst.JOURNALPOST }

        when:
        props = caseDefaults.tilskuddfredabygningprivateie
        then:
        !props.skjermingskontekst.any { it == CaseProperties.Skjermingskontekst.SAK }
        props.skjermingskontekst.any { it == CaseProperties.Skjermingskontekst.JOURNALPOST }
    }

    def "Able to support different journalstatus on different journalpost types"() {
        when:
            def properties = caseDefaults.tilskuddfartoy
        then:
            properties.journalpost.any {
                 (it.getKey() <=> CaseProperties.Journalposttype.I)
                 ("S".equalsIgnoreCase(it.getValue().getStatus()))

                 (it.getKey() <=> CaseProperties.Journalposttype.U)
                 ("E".equalsIgnoreCase(it.getValue().getStatus()))

                 (it.getKey() <=> CaseProperties.Journalposttype.X)
                 ("J".equalsIgnoreCase(it.getValue().getStatus()))
            }
    }

    def "Able to support journalenhet on sak"() {
        when:
        def properties = caseDefaults.sak
        then:
        properties.journalenhet.equalsIgnoreCase("PM")
    }
}
