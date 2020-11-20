# FINT Arkiv Case Defaults

Library for declaring defaults for cases, used by various Arkiv adapters.

Properties declared under `fint.case.defaults.<casetype>` will define default values
applied by the adapter if these values have not been set by the client.

## Case Types

- `tilskuddfartoy`
- `tilskuddfredabygningprivateie`
- `personalmappe`

### Properties for each case type

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
- `tilgangsrestriksjon`
- `skjermingshjemmel`

### How to implement in adapter

1. Add `compile('no.fint:fint-arkiv-case-defaults:+')` to `build.gradle`
2. Extend `CaseDefaultsService` with adapter-specific features
3. Invoke the extended service in the handlers

## TitleService

This service is used to set the format for cases based on case properties.  
It is bidirectional, meaning case properties can also be parsed and applied from the title.

This is controlled by the property `fint.case.formats.title.<casetype>`.  Properties in the `${name}` 
format will be evaluated and parsed. 

### Example
`Løyve drosje - ${tittel} - ${organisasjonsnummer}`

gives

`Løyve drosje - Centro Taxi AS - 222333444`

## AdditionalFieldService

This service is used to apply custom attributes (additional fields) in the system specific objects
to / from case attributes.

This is controlled by properties of the format `fint.case.formats.field.<casetype>.<customField>` where
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
