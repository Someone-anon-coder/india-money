/*
 * File:      UnitConversionUnitTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Unit tests for UnitConversion.
 * Owns:      Exact, lossy, rounded and inverse conversions.
 * Uses:      UnitConversion
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
class UnitConversionUnitTest {

  private static final Unit BOX = Unit.of("box", 0);
  private static final Unit DOZEN_BOX = Unit.of("dbox", 1);

  @Test
  @DisplayName("box to pieces converts exactly")
  void shouldConvertExactly_whenFactorIsWhole() {
    UnitConversion boxToPcs = new UnitConversion(BOX, Unit.PCS, 12, 1);
    assertThat(boxToPcs.convert(Quantity.of(3, BOX))).isEqualTo(Quantity.of(36, Unit.PCS));
  }

  @Test
  @DisplayName("pieces to box is rejected when lossy and allowed with a rounding mode")
  void shouldRejectLossyConversion_whenNoRoundingModeGiven() {
    UnitConversion pcsToBox = new UnitConversion(Unit.PCS, BOX, 1, 12);
    Quantity thirteen = Quantity.of(13, Unit.PCS);
    assertThatThrownBy(() -> pcsToBox.convert(thirteen)).isInstanceOf(ArithmeticException.class);
    assertThat(pcsToBox.convert(thirteen, RoundingMode.DOWN)).isEqualTo(Quantity.of(1, BOX));
    assertThat(pcsToBox.convert(thirteen, RoundingMode.UP)).isEqualTo(Quantity.of(2, BOX));
    assertThat(pcsToBox.convert(Quantity.of(24, Unit.PCS))).isEqualTo(Quantity.of(2, BOX));
  }

  @Test
  @DisplayName("fractional factor, for example 2 kg per 3 metres")
  void shouldUseRationalFactor_whenNotWhole() {
    UnitConversion metreToKg = new UnitConversion(Unit.METRE, Unit.KG, 2, 3);
    Quantity kg = metreToKg.convert(Quantity.of(new BigDecimal("3.000"), Unit.METRE));
    assertThat(kg.amount()).isEqualByComparingTo("2.000");
    assertThatThrownBy(() -> metreToKg.convert(Quantity.of(new BigDecimal("1.000"), Unit.METRE)))
        .isInstanceOf(ArithmeticException.class);
  }

  @Test
  @DisplayName("factor is reduced to lowest terms")
  void shouldReduceFactor_whenCommonDivisorExists() {
    UnitConversion c = new UnitConversion(Unit.KG, Unit.GRAM, 2000, 2);
    assertThat(c.numerator()).isEqualTo(1000);
    assertThat(c.denominator()).isEqualTo(1);
    assertThat(new UnitConversion(Unit.KG, Unit.GRAM, 6, 4).denominator()).isEqualTo(2);
  }

  @Test
  @DisplayName("inverse converts back exactly")
  void shouldRoundTrip_whenInverseApplied() {
    UnitConversion kgToGram = new UnitConversion(Unit.KG, Unit.GRAM, 1000, 1);
    Quantity kg = Quantity.of(new BigDecimal("1.234"), Unit.KG);
    Quantity grams = kgToGram.convert(kg);
    assertThat(grams).isEqualTo(Quantity.of(1234, Unit.GRAM));
    assertThat(kgToGram.inverse().convert(grams)).isEqualTo(kg);
  }

  @Test
  @DisplayName("bad factor or wrong source unit is rejected")
  void shouldReject_whenFactorNonPositiveOrUnitWrong() {
    assertThatThrownBy(() -> new UnitConversion(BOX, Unit.PCS, 0, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new UnitConversion(BOX, Unit.PCS, 1, -1))
        .isInstanceOf(IllegalArgumentException.class);
    UnitConversion c = new UnitConversion(BOX, Unit.PCS, 12, 1);
    assertThatThrownBy(() -> c.convert(Quantity.of(1, Unit.PCS)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(DOZEN_BOX.scale()).isEqualTo(1);
  }
}
