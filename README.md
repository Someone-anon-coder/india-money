# india-money

Exact rupee arithmetic for Java: `Money` in paise, `Quantity` with per-unit scale, exact `UnitConversion`, Indian (lakh/crore) number formatting and parsing, amount in words, and bill round-off. Zero runtime dependencies, Java 25, JPMS module `io.github.someoneanoncoder.indiamoney`.

Status: `0.1.1`. MIT licence.

## Install

JitPack (first release):

```kotlin
repositories { maven("https://jitpack.io") }
dependencies { implementation("com.github.Someone-anon-coder:india-money:v0.1.1") }
```

Maven Central coordinates `io.github.someone-anon-coder:india-money` follow before v1.0.0.

## Examples

### 1. Money never loses a paisa

```java
Money bill = Money.ofRupees(1000);
Money gst = bill.percent(new BigDecimal("18"), RoundingMode.HALF_UP);   // INR 180.00
List<Money> shares = Money.ofPaise(100).allocate(3);                    // 0.34, 0.33, 0.33 (sums to 1.00)
List<Money> byRate = Money.ofPaise(1180).allocate(9, 9);                // 5.90, 5.90 (CGST/SGST halves)
```

### 2. Quantities and units

```java
Unit box = Unit.of("box", 0);
UnitConversion boxToPcs = new UnitConversion(box, Unit.PCS, 12, 1);
Quantity pcs = boxToPcs.convert(Quantity.of(3, box));                   // 36 pcs
new UnitConversion(Unit.PCS, box, 1, 12).convert(Quantity.of(13, Unit.PCS));            // throws: lossy
new UnitConversion(Unit.PCS, box, 1, 12).convert(Quantity.of(13, Unit.PCS), RoundingMode.UP); // 2 box

Money line = Money.ofRupees(85).times(Quantity.of(new BigDecimal("2.5"), Unit.KG), RoundingMode.HALF_UP);
```

### 3. Indian formatting

```java
IndianNumberFormat.format(Money.ofRupees(125000));         // ₹1,25,000.00
IndianNumberFormat.formatCompact(Money.ofRupees(125000));  // 1.25 L
IndianNumberFormat.formatCompact(Money.ofRupees(2_30_00_000)); // 2.3 Cr
IndianNumberFormat.parse("Rs. 1,25,000.50");               // INR 125000.50
```

### 4. Amount in words and round-off

```java
AmountInWords.english().words(Money.ofPaise(12500050));
// One Lakh Twenty-Five Thousand Rupees and Fifty Paise Only

RoundOff r = RoundOff.of(Money.ofPaise(118650), RoundOffMode.NEAREST_RUPEE);
// r.rounded() = INR 1187.00, r.adjustment() = INR 0.50 (print as its own line)
```

## Behaviour you should know

- Money is a `long` of paise (max about ₹92,233,720,368,547,758.07). Add, subtract, negate and multiply throw `ArithmeticException` on overflow.
- Every lossy operation takes an explicit `RoundingMode`; `UNNECESSARY` makes it throw instead.
- Round-off halves go away from zero (₹10.50 becomes ₹11.00; -₹10.50 becomes -₹11.00).
- English words follow the Indian system. Wording choices are documented on `AmountInWords.english()`: no "and" inside the rupee figure, "One Paisa" singular, "Minus" for negatives, "One Hundred Crore" rather than "Arab".
- `IndianNumberFormat.parse` is lenient about comma positions and accepts at most two decimals.

## Limitations

- INR only. No locale-specific digits, no Hindi or Marathi words yet (implement `AmountInWords`).
- No tax, discount or pricing rules; those belong in your application (see the sibling `india-gst`).
- Amount in words are checked against 200 hand-written values in `src/test/resources/.../golden`; they are conventions, not legal text. Check cheque or statutory wording with your accountant.

## Development

```
./gradlew check            # format, checkstyle, tests, coverage gate (90% lines), javadoc
./gradlew testUnit testProperty testGolden testArch testSecurity
./gradlew pitest           # mutation testing, threshold 70%
```

Requires JDK 25. See `CLAUDE.md` for conventions and `CONTRIBUTING.md`.
