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
        noarkMetadataService.metadata['M082'].navn == "journalposttype"
        noarkMetadataService.metadata['M082'].obligatorisk
        noarkMetadataService.metadata['M082'].verdier['I'] == 'Inngående dokument'
    }
}
