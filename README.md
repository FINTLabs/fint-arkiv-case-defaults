# FINT Arkiv Case Defaults

Library for declaring defaults for cases, used by various Arkiv adapters.

Properties declared under `fint.case.defaults.<casetype>` will define default values
applied by the adapter if these values have not been set by the client.

## Case Types

- `tilskuddfartoy`
- `tilskuddfredahusprivateie`
- `personalmappe`

### Properties for each case type

- `administrativEnhet`
- `arkivdel`
- `noekkelord`
- `klassifikasjon`
- `klasse`
- `saksstatus`
- `korrespondansepartType`
- `journalpostType`
- `journalstatus`
- `dokumentstatus`
- `dokumentType`
- `tilknyttetRegistreringSom`

### How to implement in adapter

1. Add `compile('no.fint:fint-arkiv-case-defaults:+')` to `build.gradle`
2. Extend `CaseDefaultsService` with adapter-specific features
3. Invoke the extended service in the handlers

## TitleService

This service is used to set the format for cases based on case properties.  
It is bidirectional, meaning case properties can also be parsed and applied from the title.

This is controlled by the property `fint.case.formats.title.<casetype>`.  Properties in the `${name}` 
format will be evaluated and parsed. 

## AdditionalFieldService

This service is used to apply custom attributes (additional fields) in the system specific objects
to / from case attributes.

This is controlled by properties of the format `fint.case.formats.field.<casetype>.<customField>` where
the property value is in `${name}` format.

This is also bidirectional, meaning custom fields are parsed and applied to case attributes.
