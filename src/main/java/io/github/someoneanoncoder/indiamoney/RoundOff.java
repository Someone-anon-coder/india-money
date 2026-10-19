/*
 * File:      RoundOff.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   Rounds a bill total and reports the round-off as a separate amount.
 * Owns:      Nearest-rupee and nearest-50-paise rounding with the signed adjustment.
 * Uses:      Money, RoundOffMode.
 * Used by:   Invoice total calculation in consumers.
 * Not here:  Whether round-off is legally shown on a given document type.
 * Reuse:     Value type; never subclass.
 * Plan:      L1-05
 */
package io.github.someoneanoncoder.indiamoney;

import java.util.Objects;

/**
 * The result of rounding a total: the original, the rounded amount, and the adjustment to print as
 * its own line ({@code rounded = original + adjustment}).
 *
 * <p>Halves round away from zero, so 10.50 becomes 11.00 and -10.50 becomes -11.00 when rounding to
 * the nearest rupee, and 10.25 becomes 10.50 when rounding to 50 paise.
 *
 * @param original the unrounded total
 * @param rounded the total after rounding
 * @param adjustment {@code rounded - original}; negative when the total went down
 */
public record RoundOff(Money original, Money rounded, Money adjustment) {

  /**
   * Validates the components.
   *
   * @throws IllegalArgumentException if the adjustment does not reconcile the amounts
   */
  public RoundOff {
    Objects.requireNonNull(original, "original");
    Objects.requireNonNull(rounded, "rounded");
    Objects.requireNonNull(adjustment, "adjustment");
    if (!original.plus(adjustment).equals(rounded)) {
      throw new IllegalArgumentException("rounded must equal original plus adjustment");
    }
  }

  /**
   * Rounds a total.
   *
   * @param total the unrounded total
   * @param mode the rounding policy
   * @return the rounded total and the adjustment
   * @throws ArithmeticException if the rounded value overflows
   */
  public static RoundOff of(Money total, RoundOffMode mode) {
    Objects.requireNonNull(total, "total");
    Objects.requireNonNull(mode, "mode");
    long step = mode.stepPaise();
    long magnitude = Math.abs(total.paise());
    long steps = magnitude / step;
    if ((magnitude % step) * 2 >= step) {
      steps++;
    }
    long roundedMagnitude = Math.multiplyExact(steps, step);
    Money rounded = Money.ofPaise(total.isNegative() ? -roundedMagnitude : roundedMagnitude);
    return new RoundOff(total, rounded, rounded.minus(total));
  }
}
