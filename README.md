# FINT Arkiv Case Defaults

Library for declaring defaults for cases, used by various Arkiv adapters.

Properties declared under `fint.case.defaults.<casetype>` will define default values
applied by the adapter if these values have not been set by the client.

## Case Types

- `sak`
- `tilskuddfartoy`
- `tilskuddfredabygningprivateie`
- `personalmappe`
- `soknaddrosjeloyve`
- `dispensasjonautomatiskfredakulturminne`

### Properties for each case type

- `title` (see _TitleService_ below)
- `field` (see _AdditionalFieldService_ below)
- `administrativEnhet`
- `journalenhet`
- `arkivdel`
- `saksansvarlig`
- `noekkelord`
- `klassifikasjon` (see _Classifications_ below)
- `saksstatus`
- `korrespondansepartType`
- `journalpostType`
- `journalstatus` (or `journalpost`(*))
- `saksbehandler`
- `dokumentstatus`
- `dokumentType`
- `format`
- `variantFormat`
- `tilknyttetRegistreringSom`
- `skjermingskontekst`
- `tilgangsrestriksjon`
- `skjermingshjemmel`
- `saksmappeType`

(*) From version 4.1.0 it's supported to declare different journalstatuses for different journalposttypes.

Examples:
- `fint.case.defaults.tilskuddfredabygningprivateie.journalpost.I.status`
- `fint.case.defaults.tilskuddfredabygningprivateie.journalpost.U.status`

(If you're happy with one status to rule them all, just keep on using `journalstatus` e.g.
`fint.case.defaults.soknaddrosjeloyve.journalstatus`. If `journalstatus` is declared, the `journalpost` will be ignored.)

### How to implement in adapter

1. Add `compile('no.fint:fint-arkiv-case-defaults:+')` to `build.gradle`
2. Extend `CaseDefaultsService` with adapter-specific features
3. Add a `@Service` which implements `LinkResolver`
4. Invoke the extended service in the handlers

## Property expansion using Apache Commons Beanutils

In the sections below, the pattern `${name}` indicates that the strings support parameter substitution with the help
of `org.apache.commons.beanutils.PropertyUtils.getProperty()`.  Unfortunately, the project's Javadoc does not provide
concrete examples of usage, but have a look at https://www.baeldung.com/apache-commons-beanutils for some examples.

### Resolving links to other resources

The property lookup also supports following links to other resources, so information can be retrieved from the target
of these links.

Projects using this library must have a `@Service` which implements `LinkResolver` for this to work.

The syntax for this linked property is as follows:

- `${link$name.of.link.property#name.of.property.in.target}`

This can be done in a nested manner, where the right-hand side of `#` contains another `link$xxx#yyy` expression.

Examples:
- On `Saksmappe`, `${link$arkivdel#tittel}` would resolve to the `tittel` attribute of the `Arkivdel` resource linked.
- On `Saksmappe`, `${link$saksansvarlig#link$tilgang#link$rolle#navn}` would resolve to the role name of the responsible
  person, following the links from `Saksmappe` via `Arkivressurs` and `Tilgang` to `Rolle`.

*NOTE:* In earlier versions (until 3.1.0) we used colon as delimiter (i.e. `link:`) so beware of that when upgrading!

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

Combining `${name}` and `#{expression}` statements in the same string results in undefined behavior.

## AdditionalFieldService

Use this service to apply custom attributes (additional fields) in the system specific objects
to / from case attributes.

This is controlled by properties of the format `fint.case.defaults.<casetype>.field.<customField>` where
the property value is in `${name}` format.  

This is also bidirectional, meaning custom fields are parsed and applied to case attributes.

## Classifications

Classifications are controlled by the following sets of properties:

- `fint.case.defaults.<casetype>.klassifikasjon.<KEY>.ordning`
- `fint.case.defaults.<casetype>.klassifikasjon.<KEY>.verdi`
- `fint.case.defaults.<casetype>.klassifikasjon.<KEY>.tittel`

Where `<KEY>` determines sorting order - `1` for primary classification, `2` for secondary classification, etc.
`ordning` is the ID of the `Klassifikasjonssystem` the classifications is within. 
`verdi` is the classification value, and `tittel` the classification title (or description).  These support the same
`${name}` interpolation values as titles and additional fields referred above.

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
- `filformat`
- `variantFormat`

(Note that the ones marked (*) are not yet implemented)

The defined codes can be found in [noark-metadata.json](src/main/resources/noark-metadata.json).

Missing mappings will be logged on application startup.  If `fint.case.coding.fatal` is `true`, a fatal exception will 
be thrown if any mappings are missing.

To perform metadata mapping, invoke `CodingSystemService.mapCodingSystemLinks(FintLinks resource)` on the resources
before submitting to the implementing system.
*NOTE:* This is done automatically by `CaseDefaultsService`.

Furthermore, to expose the standard coding system values, use the functions in `NoarkMetadataService`.
