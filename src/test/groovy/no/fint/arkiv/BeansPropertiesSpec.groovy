package no.fint.arkiv

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource
import org.apache.commons.text.StringSubstitutor
import spock.lang.Specification

import java.time.LocalDate

class BeansPropertiesSpec extends Specification {

    def "Try fetching some properties"() {
        given:
        def mapper = new ObjectMapper()
        def object = mapper.readValue(getClass().getResourceAsStream('/tilskuddfartoy.json'), TilskuddFartoyResource)
        def subst = new StringSubstitutor(new BeanPropertyLookup(object))

        when:
        def fartoyNavn = subst.replace('${fartoyNavn}')

        then:
        fartoyNavn == 'Ternen'

        when:
        def soknadsnummer = subst.replace('${soknadsnummer.identifikatorverdi}')

        then:
        soknadsnummer == '35'

        when:
        def tittel = subst.replace('${journalpost[0].dokumentbeskrivelse[0].tittel}')

        then:
        tittel == 'Tomt arkivskap'
    }

    def 'Able to resolve dates'() {
        given:
        def subst = new StringSubstitutor(new BeanPropertyLookup(new Object()))

        when:
        def title = subst.replace('${date:yyyy}')

        then:
        title == LocalDate.now().year.toString()
    }
}
