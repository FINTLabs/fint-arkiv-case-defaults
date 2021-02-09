# FINT Arkiv Case Defaults

Library for declaring defaults for cases, used by various Arkiv adapters.

Properties declared under `fint.case.defaults.<casetype>` will define default values
applied by the adapter if these values have not been set by the client.

## Case Types

- `tilskuddfartoy`
- `tilskuddfredabygningprivateie`
- `personalmappe`
- `soknaddrosjeloyve`

### Properties for each case type

- `title` (see below)
- `field` (see below)
- `administrativEnhet`
- `journalenhet`
- `arkivdel`
- `saksansvarlig`
- `noekkelord`
- `klassifikasjon`
- `klasse`
- `saksstatus`
- `korrespondansepartType`
- `journalpostType`
- `journalstatus`
- `saksbehandler`
- `dokumentstatus`
- `dokumentType`
- `tilknyttetRegistreringSom`
- `skjermingskontekst`
- `tilgangsrestriksjon`
- `skjermingshjemmel`
- `saksmappeType`

### How to implement in adapter

1. Add `compile('no.fint:fint-arkiv-case-defaults:+')` to `build.gradle`
2. Extend `CaseDefaultsService` with adapter-specific features
3. Add a `@Service` which implements `LinkResolver`
4. Invoke the extended service in the handlers

## TitleService

This service formats titles for cases for different case types based on case properties, as well as title prefixes
for records and documents.  
It is bidirectional, meaning case properties can be parsed and applied from the title.

This is controlled by the following properties:
- `fint.case.defaults.<casetype>.title.cases`
- `fint.case.defaults.<casetype>.title.records`
- `fint.case.defaults.<casetype>.title.documents`
 
Properties in the `${name}` format will be evaluated and parsed.

### How to implement in adapter

1. Add `compile('no.fint:fint-arkiv-case-defaults:+')` to `build.gradle`
2. `@Autowired TitleService` in case mapping code.
3. Writing: 
   - Invoke `getCaseTitle()` on `TitleMapper` to create case title.
   - Invoke `getRecordTitlePrefix()` and `getDocumentTitlePrefix()` to obtain prefixes for records and documents. 
4. Reading: Invoke `parseCaseTitle()` on `TitleMapper` to apply case properties from the case title.
   - This method returns `true` if the case title matched the expected pattern.

### Example
`Løyve drosje - ${tittel} - ${organisasjonsnummer}`

gives

`Løyve drosje - Centro Taxi AS - 222333444`

### Formatting titles using Spring Expression Language (SpEL)

As an alternative to the `${name}` format, titles can also be formatted using `#{expression}` statements.
See https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions-language-ref for
a language reference.

## AdditionalFieldService

Use this service to apply custom attributes (additional fields) in the system specific objects
to / from case attributes.

This is controlled by properties of the format `fint.case.defaults.<casetype>.field.<customField>` where
the property value is in `${name}` format.

This is also bidirectional, meaning custom fields are parsed and applied to case attributes.

# Metadata Coding System Mapping

This library also provides mapping functionality for metadata that is specified by Noark.  Refer to Arkivverket's 
metadata catalog at https://arkivverket.metakat.no for definition of codes. 

Configure using properties within `fint.case.coding.<metadata>.<code>`, where the property value is the code or ID used
by the implementing system to represent this coding value.

The metadata attributes supported are:

- `saksstatus`
- `journalstatus`
- `dokumentstatus`
- `journalposttype`
- `dokumenttype`
- `klassifikasjonstype` (*)
- `korrespondanseparttype`
- `tilknyttetRegistreringSom`
- `partRolle`
- `tilgangsrestriksjon`
- `skjermingshjemmel`
- `skjermingMetadata` (*)
- `skjermingDokument` (*)
- `gradering`
- `variantformat`

(Note that the ones marked (*) are not yet supported)

The defined codes can be found in [noark-metadata.json](src/main/resources/noark-metadata.json).

Missing mappings will be logged on application startup.  If `fint.case.coding.fatal` is `true`, a fatal exception will 
be thrown if any mappings are missing.

To perform metadata mapping, invoke `CodingSystemService.mapCodingSystemLinks(FintLinks resource)` on the resources
before submitting to the implementing system.
*NOTE:* This is done automatically by `CaseDefaultsService`.

Furthermore, to expose the standard coding system values, use the functions in `NoarkMetadataService`.
