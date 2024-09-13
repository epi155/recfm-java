# Change Log

## [0.7.2]

### Added
- auto length in parent fields, if omitted
- check field names against reserved names
- copy COBOL at the end of the class

### Change
- `initialize()` from `protected` to `public`
- `initialize()` nested (group level)

### Fixed
- GRP nested in Grp
- Grp nested in Emb
- Emb::interface nested in Emb::class
- Val,Cus,Dom nested in Occ,OCC
- Code too large (initialize, toString)
- automatic offset in fields with occurrences
- ignore override fields in hole checking

## [0.7.1] - 2024-02-02

### Change
- `length()` modifiers to `static`

### Fixed
- update dependency (CVE-2023-6378)
- `Val` fields in override `Grp`


## [0.7.0] - 2023-09-01 - baseline version