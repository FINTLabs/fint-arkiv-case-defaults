# FINT Arkiv Case Defaults

Library for declaring defaults for cases, used by various Arkiv adapters.

Properties declared under `fint.case.defaults.<casetype>` will define default values
applied by the adapter if these values have not been set by the client.

## Case Types

- `tilskuddfartoy`
- `personalmappe`

## Properties for each case type

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

# How to implement in adapter

1. Add `compile('no.fint:fint-arkiv-case-defaults:+')` to `build.gradle`
2. Extend `CaseDefaultsService` with adapter-specific features
3. Invoke the extended service in the handlers

