# indiamoney (package root)

**Path:** src/main/java/io/github/someoneanoncoder/indiamoney
**Purpose:** Public API value types.

## Contents
| File | Purpose | Plan |
|---|---|---|
| Money.java | Paise-backed rupee amount | L1-01 |
| Unit.java | Unit of measure with decimal scale | L1-02 |
| Quantity.java | Exact quantity at the unit's scale | L1-02 |
| UnitConversion.java | Rational-factor unit conversion | L1-02 |
| AmountInWords.java | Amount-in-words contract and English factory | L1-04 |
| RoundOffMode.java | Round-off policies | L1-05 |
| RoundOff.java | Rounded total with separate adjustment | L1-05 |
| package-info.java | Package documentation | L1 |

## Sub-folders
- format/ — Indian number formatting and parsing
- internal/ — non-exported implementations

## Rules for adding files here
- Public API nouns only; implementations go in internal/.
