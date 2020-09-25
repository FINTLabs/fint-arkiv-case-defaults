package no.fint.arkiv

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

    def "Able to decode coding system values"() {
        expect:
        codingSystemDefaults.journalposttype.any {it.code == 'Inngående dokument' && it.value == '110'}
        codingSystemDefaults.tilknyttetregistreringsom.any { it.code == 'Hoveddokument' && it.value == '1'}
    }

    def "Able to supply stream of JournalpostType"() {
        when:
        def result = codingSystemService.journalpostType.collect(Collectors.toList())

        then:
        result.any {it.kode == 'Inngående dokument'}
    }

    def "Able to supply stream of TilknyttetRegistreringSom"() {
        when:
        def result = codingSystemService.tilknyttetRegistreringSom.collect(Collectors.toList())

        then:
        result.any {it.kode == 'Hoveddokument'}
    }

    def "Able to get definition of metadata M082"() {
        expect:
        noarkMetadataService.metadata.get("M082").navn == "journalposttype"
    }
}
