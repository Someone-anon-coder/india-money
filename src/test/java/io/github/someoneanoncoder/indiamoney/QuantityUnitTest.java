/*
 * File:      QuantityUnitTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Unit tests for Quantity and Unit.
 * Owns:      Scale enforcement and same-unit arithmetic cases.
 * Uses:      Quantity, Unit
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-02
 */
package io.github.someoneanoncoder.indiamoney;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiamoney.testing.UnitTest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class QuantityUnitTest {

  @Test
  @DisplayName("amount is normalised to the unit scale so equal quantities are equal")
  void shouldBeEqual_whenWrittenWithDifferentScales() {
    assertThat(Quantity.of(new BigDecimal("2.5"), Unit.KG))
        .isEqualTo(Quantity.of(new BigDecimal("2.500"), Unit.KG));
    assertThat(Quantity.of(3, Unit.PCS).amount().scale()).isZero();
  }

  @Test
  @DisplayName("too many decimals are rejected unless a rounding mode is given")
  void shouldRejectOrRound_whenAmountExceedsUnitScale() {
    assertThatThrownBy(() -> Quantity.of(new BigDecimal("1.5"), Unit.PCS))
        .isInstanceOf(ArithmeticException.class);
    assertThat(Quantity.of(new BigDecimal("1.5"), Unit.PCS, RoundingMode.HALF_UP).amount())
        .isEqualByComparingTo("2");
  }

  @Test
  @DisplayName("add and subtract require the same unit")
  void shouldAddSameUnitOnly_whenCombiningQuantities() {
    Quantity a = Quantity.of(new BigDecimal("1.250"), Unit.KG);
    Quantity b = Quantity.of(new BigDecimal("0.750"), Unit.KG);
    assertThat(a.plus(b).amount()).isEqualByComparingTo("2.000");
    assertThat(a.minus(b).amount()).isEqualByComparingTo("0.500");
    assertThatThrownBy(() -> a.plus(Quantity.of(1, Unit.PCS)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> a.compareTo(Quantity.of(1, Unit.PCS)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(a).isGreaterThan(b);
  }

  @Test
  @DisplayName("scaling rounds back to the unit scale")
  void shouldRoundToUnitScale_whenScaling() {
    Quantity q = Quantity.of(5, Unit.PCS);
    assertThat(q.times(new BigDecimal("0.5"), RoundingMode.HALF_UP).amount())
        .isEqualByComparingTo("3");
    assertThat(q.times(new BigDecimal("0.5"), RoundingMode.HALF_EVEN).amount())
        .isEqualByComparingTo("2");
  }

  @Test
  @DisplayName("zero and negative helpers, text")
  void shouldReportZeroAndSign_whenInspected() {
    assertThat(Quantity.of(0, Unit.PCS).isZero()).isTrue();
    assertThat(Quantity.of(-1, Unit.PCS).isNegative()).isTrue();
    assertThat(Quantity.of(new BigDecimal("2.5"), Unit.KG)).hasToString("2.500 kg");
  }

  @Test
  @DisplayName("units validate code and scale")
  void shouldRejectBadUnit_whenCodeBlankOrScaleOutOfRange() {
    assertThatThrownBy(() -> Unit.of(" ", 0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Unit.of("x", -1)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Unit.of("x", 10)).isInstanceOf(IllegalArgumentException.class);
    assertThat(Unit.of("box", 0).code()).isEqualTo("box");
  }
}
