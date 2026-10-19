# Changelog

All notable changes are listed here. Format: [Keep a Changelog](https://keepachangelog.com/); versioning: [SemVer](https://semver.org/) (breaking changes only in minor bumps before 1.0.0).

## [Unreleased]

## [0.1.1] - 2026-10-19

### Fixed
- JitPack build: correct SDKMAN identifier for Temurin 25 (v0.1.0 could not be built by JitPack; its GitHub release jars are unaffected).

## [0.1.0] - 2026-10-19

### Added
- `Money` (paise, exact arithmetic, explicit rounding, lossless allocation) [L1-01].
- `Unit`, `Quantity`, `UnitConversion` (rational factor, lossy conversions rejected) [L1-02].
- `IndianNumberFormat` (lakh/crore grouping, compact form, lenient parse) [L1-03].
- `AmountInWords` with English implementation, checked against 200 hand-written values [L1-04].
- `RoundOff` and `RoundOffMode` [L1-05].
- Property tests [L1-06].
