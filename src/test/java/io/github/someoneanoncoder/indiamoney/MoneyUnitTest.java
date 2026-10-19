/*
 * File:      MoneyUnitTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Unit tests for Money.
 * Owns:      Arithmetic, rounding, overflow and allocation cases.
 * Uses:      Money
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-01
 */
package io.github.someoneanoncoder.indiamoney;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiamoney.testing.UnitTest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class MoneyUnitTest {

  @Test
  @DisplayName("adds and subtracts exactly in paise")
  void shouldAddAndSubtractExactly_whenAmountsHaveFractions() {
    Money a = Money.ofPaise(1999);
    Money b = Money.ofPaise(1);
    assertThat(a.plus(b)).isEqualTo(Money.ofRupees(20));
    assertThat(a.minus(b).paise()).isEqualTo(1998);
    assertThat(b.minus(a).isNegative()).isTrue();
  }

  @Test
  @DisplayName("overflow throws instead of wrapping")
  void shouldThrow_whenArithmeticOverflows() {
    Money max = Money.ofPaise(Long.MAX_VALUE);
    assertThatThrownBy(() -> max.plus(Money.ofPaise(1))).isInstanceOf(ArithmeticException.class);
    assertThatThrownBy(() -> Money.ofPaise(Long.MIN_VALUE).negate())
        .isInstanceOf(ArithmeticException.class);
    assertThatThrownBy(() -> Money.ofRupees(Long.MAX_VALUE))
        .isInstanceOf(ArithmeticException.class);
    assertThatThrownBy(() -> max.times(2)).isInstanceOf(ArithmeticException.class);
  }

  @Test
  @DisplayName("negate, abs, sign helpers")
  void shouldReportSign_whenNegativeZeroPositive() {
    assertThat(Money.ofPaise(-5).abs()).isEqualTo(Money.ofPaise(5));
    assertThat(Money.ofPaise(5).abs()).isEqualTo(Money.ofPaise(5));
    assertThat(Money.ofPaise(5).negate().signum()).isEqualTo(-1);
    assertThat(Money.ZERO.isZero()).isTrue();
    assertThat(Money.ZERO.signum()).isZero();
  }

  @Test
  @DisplayName("multiplying a rate by a quantity rounds with the given mode")
  void shouldRoundPerMode_whenMultiplyingByFraction() {
    Money rate = Money.ofPaise(1005);
    BigDecimal half = new BigDecimal("0.5");
    assertThat(rate.times(half, RoundingMode.HALF_UP).paise()).isEqualTo(503);
    assertThat(rate.times(half, RoundingMode.HALF_DOWN).paise()).isEqualTo(502);
    assertThat(rate.times(half, RoundingMode.HALF_EVEN).paise()).isEqualTo(502);
    assertThat(rate.times(half, RoundingMode.CEILING).paise()).isEqualTo(503);
    assertThat(rate.times(half, RoundingMode.FLOOR).paise()).isEqualTo(502);
    assertThatThrownBy(() -> rate.times(half, RoundingMode.UNNECESSARY))
        .isInstanceOf(ArithmeticException.class);
    assertThat(rate.times(new BigDecimal("2"), RoundingMode.UNNECESSARY).paise()).isEqualTo(2010);
  }

  @Test
  @DisplayName("rate times quantity uses the quantity amount")
  void shouldMultiplyByQuantity_whenRateIsPerUnit() {
    Money perKg = Money.ofRupees(85);
    Quantity weight = Quantity.of(new BigDecimal("2.500"), Unit.KG);
    assertThat(perKg.times(weight, RoundingMode.HALF_UP)).isEqualTo(Money.ofPaise(21250));
  }

  @Test
  @DisplayName("percentage of an amount")
  void shouldTakePercentage_whenRateGiven() {
    Money base = Money.ofRupees(1000);
    assertThat(base.percent(new BigDecimal("18"), RoundingMode.HALF_UP))
        .isEqualTo(Money.ofRupees(180));
    assertThat(Money.ofPaise(1999).percent(new BigDecimal("2.5"), RoundingMode.HALF_UP).paise())
        .isEqualTo(50);
    assertThat(Money.ofPaise(1999).percent(new BigDecimal("2.5"), RoundingMode.DOWN).paise())
        .isEqualTo(49);
  }

  @Test
  @DisplayName("decimal rupees convert to paise with explicit rounding")
  void shouldRoundDecimalRupees_whenMoreThanTwoPlaces() {
    assertThat(Money.ofRupees(new BigDecimal("12.345"), RoundingMode.HALF_UP).paise())
        .isEqualTo(1235);
    assertThat(Money.ofRupees(new BigDecimal("12.3"), RoundingMode.UNNECESSARY).paise())
        .isEqualTo(1230);
    assertThatThrownBy(() -> Money.ofRupees(new BigDecimal("12.345"), RoundingMode.UNNECESSARY))
        .isInstanceOf(ArithmeticException.class);
  }

  @Test
  @DisplayName("equal split hands leftover paise to the first parts")
  void shouldGiveLeftoverToFirstParts_whenSplittingEqually() {
    assertThat(Money.ofPaise(100).allocate(3))
        .containsExactly(Money.ofPaise(34), Money.ofPaise(33), Money.ofPaise(33));
    assertThat(Money.ofPaise(-100).allocate(3))
        .containsExactly(Money.ofPaise(-34), Money.ofPaise(-33), Money.ofPaise(-33));
    assertThat(Money.ofPaise(2).allocate(5)).hasSize(5);
  }

  @Test
  @DisplayName("proportional split uses largest remainder")
  void shouldSplitProportionally_whenRatiosGiven() {
    List<Money> parts = Money.ofPaise(5).allocate(3, 7);
    assertThat(parts).containsExactly(Money.ofPaise(2), Money.ofPaise(3));
    assertThat(Money.ofPaise(1000).allocate(0, 1, 0))
        .containsExactly(Money.ZERO, Money.ofPaise(1000), Money.ZERO);
    assertThat(Money.ofPaise(1180).allocate(9, 9))
        .containsExactly(Money.ofPaise(590), Money.ofPaise(590));
  }

  @Test
  @DisplayName("allocation handles amounts near the long limit")
  void shouldNotOverflow_whenAllocatingHugeAmount() {
    Money huge = Money.ofPaise(Long.MAX_VALUE);
    List<Money> parts = huge.allocate(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
    assertThat(parts.stream().mapToLong(Money::paise).sum()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  @DisplayName("invalid allocation arguments are rejected")
  void shouldRejectBadArguments_whenAllocating() {
    Money m = Money.ofPaise(10);
    assertThatThrownBy(() -> m.allocate(0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> m.allocate(new long[0])).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> m.allocate(1, -1)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> m.allocate(0, 0)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("text and ordering")
  void shouldFormatAndCompare_whenUsedAsValue() {
    assertThat(Money.ofPaise(125050)).hasToString("INR 1250.50");
    assertThat(Money.ofPaise(-5)).hasToString("INR -0.05");
    assertThat(Money.ofPaise(1)).isLessThan(Money.ofPaise(2));
    assertThat(Money.ofPaise(125050).rupees()).isEqualByComparingTo("1250.50");
  }
}
