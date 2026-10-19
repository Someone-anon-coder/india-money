/*
 * File:      RoundOffMode.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   The bill round-off policies a shop can choose.
 * Owns:      Round-off step sizes in paise.
 * Uses:      Nothing.
 * Used by:   RoundOff.
 * Not here:  The rounding calculation (RoundOff).
 * Reuse:     Add modes only through a minor version bump.
 * Plan:      L1-05
 */
package io.github.someoneanoncoder.indiamoney;

/** How a bill total is rounded. Halves round away from zero. */
public enum RoundOffMode {
  /** No rounding; the adjustment is always zero. */
  NONE(1),
  /** Round to the nearest whole rupee. */
  NEAREST_RUPEE(100),
  /** Round to the nearest 50 paise. */
  NEAREST_50_PAISE(50);

  private final long stepPaise;

  RoundOffMode(long stepPaise) {
    this.stepPaise = stepPaise;
  }

  /**
   * Returns the step the total is rounded to.
   *
   * @return step size in paise
   */
  public long stepPaise() {
    return stepPaise;
  }
}
