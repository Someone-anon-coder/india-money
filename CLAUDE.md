# CLAUDE.md — india-money

Public MIT library (derived from the ShopSuite monorepo CLAUDE.md §12 template). Scope: `Money`, `Quantity`, `Unit`, `UnitConversion`, `IndianNumberFormat`, `AmountInWords`, `RoundOff`. Anything else (pricing, discounts, tax) is out of scope and stays in the private product.

## Rules

1. Zero runtime dependencies beyond the JDK. Test/build tools must be permissively licensed.
2. Money and quantity use `long`/`BigDecimal`/`BigInteger` only; no `double`/`float` anywhere (ArchUnit enforced).
3. Every public type/method has full Javadoc; every source file has the header (checkstyle enforced); every folder has `FOLDER.md`.
4. Files at most 400 lines, methods at most 40.
5. Value types use natural names (`plus`, `times`, `allocate`). The subject-stem rule of the monorepo (§5.3) applies to layered code, not to this public API.
6. Tests: `<Class><Category>Test`; categories unit, property, golden, arch, security. Line coverage at least 90%, PIT at least 70%.
7. Golden amount-in-words values are written by hand and never generated from the implementation.
8. Never delete branches. Ask the owner before pushing, opening PRs or publishing releases.
9. Releases only from tags `v*` via CI. Breaking changes only in a minor bump before 1.0.0, listed in CHANGELOG.

## Tracking

Plan IDs L1-01..L1-06 live in the ShopSuite repo `docs/PLAN.md`. Status of this library is tracked in that repo's CLAUDE.md §10.
