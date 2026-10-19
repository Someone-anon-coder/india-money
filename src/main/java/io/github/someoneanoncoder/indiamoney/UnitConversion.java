/*
 * File:      UnitConversion.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   Exact conversion between two units using a rational factor.
 * Owns:      Factor reduction, lossless conversion, rejection of lossy results, inverse.
 * Uses:      Quantity, Unit, java.math.
 * Used by:   Multi-unit stock and billing code.
 * Not here:  Storing conversion tables per item.
 * Reuse:     Value type; never subclass.
 * Plan:      L1-02
 */
package io.github.someoneanoncoder.indiamoney;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Converts quantities from one unit to another: {@code to = from * numerator / denominator}.
 *
 * <p>For example 1 box = 12 pcs is {@code new UnitConversion(box, Unit.PCS, 12, 1)}, and 1 kg =
 * 1000 g is {@code (KG, GRAM, 1000, 1)}. The factor is kept as an exact fraction in lowest terms,
 * never as a decimal, so a conversion and its {@link #inverse()} round-trip exactly whenever the
 * result is representable at the target unit's scale.
 *
 * @param from the source unit
 * @param to the target unit
 * @param numerator positive numerator of the factor
 * @param denominator positive denominator of the factor
 */
public record UnitConversion(Unit from, Unit to, long numerator, long denominator) {

  /**
   * Validates the factor and reduces it to lowest terms.
   *
   * @throws IllegalArgumentException if numerator or denominator is not positive
   */
  public UnitConversion {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    if (numerator <= 0 || denominator <= 0) {
      throw new IllegalArgumentException(
          "factor must be positive: " + numerator + "/" + denominator);
    }
    long divisor = BigInteger.valueOf(numerator).gcd(BigInteger.valueOf(denominator)).longValue();
    numerator /= divisor;
    denominator /= divisor;
  }

  /**
   * Converts exactly.
   *
   * @param quantity amount in the source unit
   * @return the equivalent amount in the target unit
   * @throws ArithmeticException if the result needs more decimal places than the target unit allows
   * @throws IllegalArgumentException if {@code quantity} is not in the source unit
   */
  public Quantity convert(Quantity quantity) {
    return convert(quantity, RoundingMode.UNNECESSARY);
  }

  /**
   * Converts, rounding the result to the target unit's scale.
   *
   * @param quantity amount in the source unit
   * @param mode how to round when the result is not representable
   * @return the equivalent amount in the target unit
   * @throws IllegalArgumentException if {@code quantity} is not in the source unit
   */
  public Quantity convert(Quantity quantity, RoundingMode mode) {
    Objects.requireNonNull(quantity, "quantity");
    Objects.requireNonNull(mode, "mode");
    if (!quantity.unit().equals(from)) {
      throw new IllegalArgumentException(
          "expected " + from.code() + " but got " + quantity.unit().code());
    }
    BigDecimal scaled = quantity.amount().multiply(BigDecimal.valueOf(numerator));
    BigDecimal result = scaled.divide(BigDecimal.valueOf(denominator), to.scale(), mode);
    return new Quantity(result, to);
  }

  /**
   * Returns the conversion in the opposite direction.
   *
   * @return a conversion from the target unit back to the source unit
   */
  public UnitConversion inverse() {
    return new UnitConversion(to, from, denominator, numerator);
  }
}
