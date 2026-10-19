/*
 * File:      MoneyPropertyTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Property tests for Money, Quantity, conversion and formatting.
 * Owns:      Algebraic invariants required by L1-06.
 * Uses:      Money, UnitConversion, IndianNumberFormat
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-06
 */
package io.github.someoneanoncoder.indiamoney;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiamoney.format.IndianNumberFormat;
import io.github.someoneanoncoder.indiamoney.testing.PropertyTest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.Size;

@PropertyTest
class MoneyPropertyTest {

  private static final long LIMIT = 1_000_000_000_000_000L;

  @Provide
  Arbitrary<Money> money() {
    return Arbitraries.longs().between(-LIMIT, LIMIT).map(Money::ofPaise);
  }

  @Property
  void shouldBeAssociative_whenAdding(
      @ForAll("money") Money a, @ForAll("money") Money b, @ForAll("money") Money c) {
    assertThat(a.plus(b).plus(c)).isEqualTo(a.plus(b.plus(c)));
    assertThat(a.plus(b)).isEqualTo(b.plus(a));
    assertThat(a.plus(a.negate())).isEqualTo(Money.ZERO);
    assertThat(a.minus(b).plus(b)).isEqualTo(a);
  }

  @Property
  void shouldSumToTotal_whenSplittingEqually(
      @ForAll("money") Money total, @ForAll @IntRange(min = 1, max = 60) int parts) {
    List<Money> split = total.allocate(parts);
    assertThat(split).hasSize(parts);
    assertThat(split.stream().mapToLong(Money::paise).sum()).isEqualTo(total.paise());
    long min = split.stream().mapToLong(Money::paise).min().orElseThrow();
    long max = split.stream().mapToLong(Money::paise).max().orElseThrow();
    assertThat(max - min).isLessThanOrEqualTo(1);
  }

  @Property
  void shouldSumToTotal_whenSplittingByRatios(
      @ForAll("money") Money total,
      @ForAll @Size(min = 1, max = 8) List<@LongRange(min = 0, max = 1000) Long> ratios) {
    long[] weights = ratios.stream().mapToLong(Long::longValue).toArray();
    if (ratios.stream().mapToLong(Long::longValue).sum() == 0) {
      weights[0] = 1;
    }
    List<Money> split = total.allocate(weights);
    assertThat(split.stream().mapToLong(Money::paise).sum()).isEqualTo(total.paise());
    for (int i = 0; i < weights.length; i++) {
      if (weights[i] == 0) {
        assertThat(split.get(i)).isEqualTo(Money.ZERO);
      }
      assertThat(split.get(i).signum()).isIn(0, total.signum());
    }
  }

  @Property
  void shouldRoundTripFormatAndParse_whenAnyAmount(@ForAll("money") Money amount) {
    assertThat(IndianNumberFormat.parse(IndianNumberFormat.format(amount))).isEqualTo(amount);
    assertThat(IndianNumberFormat.parse(IndianNumberFormat.formatPlain(amount))).isEqualTo(amount);
  }

  @Property
  void shouldRoundTripRupeeDecimal_whenConvertingToBigDecimal(@ForAll("money") Money amount) {
    assertThat(Money.ofRupees(amount.rupees(), RoundingMode.UNNECESSARY)).isEqualTo(amount);
  }

  @Property
  void shouldRoundTripConversion_whenResultRepresentable(
      @ForAll @LongRange(min = 0, max = 1_000_000) long grams,
      @ForAll @LongRange(min = 1, max = 5000) long numerator,
      @ForAll @LongRange(min = 1, max = 5000) long denominator) {
    UnitConversion c = new UnitConversion(Unit.GRAM, Unit.of("x", 9), numerator, denominator);
    Quantity start = Quantity.of(grams, Unit.GRAM);
    try {
      Quantity there = c.convert(start);
      assertThat(c.inverse().convert(there)).isEqualTo(start);
    } catch (ArithmeticException lossy) {
      // Not representable at 9 decimals: rejecting is the contract. Rounded mode must still
      // succeed.
      assertThat(c.convert(start, RoundingMode.HALF_UP).unit().scale()).isEqualTo(9);
    }
  }

  @Property
  void shouldReconcileAdjustment_whenRounding(
      @ForAll("money") Money total, @ForAll RoundOffMode mode) {
    RoundOff r = RoundOff.of(total, mode);
    assertThat(r.original().plus(r.adjustment())).isEqualTo(r.rounded());
    assertThat(Math.abs(r.adjustment().paise())).isLessThanOrEqualTo(mode.stepPaise() / 2);
    assertThat(r.rounded().paise() % mode.stepPaise()).isZero();
  }

  @Property
  void shouldKeepPercentWithinHalfPaisa_whenRoundingHalfUp(
      @ForAll @LongRange(min = 0, max = 10_000_000_000L) long paise,
      @ForAll @IntRange(min = 0, max = 100) int percent) {
    Money result = Money.ofPaise(paise).percent(BigDecimal.valueOf(percent), RoundingMode.HALF_UP);
    BigDecimal exact = BigDecimal.valueOf(paise * percent, 2);
    assertThat(BigDecimal.valueOf(result.paise()).subtract(exact).abs())
        .isLessThanOrEqualTo(new BigDecimal("0.5"));
  }
}
