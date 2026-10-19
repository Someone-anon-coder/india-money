/*
 * File:      Unit.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   A unit of measure with the number of decimal places quantities in it may carry.
 * Owns:      Unit code, per-unit scale, a few common constants.
 * Uses:      Nothing.
 * Used by:   Quantity, UnitConversion.
 * Not here:  Conversion factors (UnitConversion) and arithmetic (Quantity).
 * Reuse:     Create your own with Unit.of; the constants are conveniences, not a registry.
 * Plan:      L1-02
 */
package io.github.someoneanoncoder.indiamoney;

import java.util.Objects;

/**
 * A unit of measure such as pieces, kilograms or metres.
 *
 * @param code short code shown to users, for example {@code "kg"}; non-blank
 * @param scale maximum decimal places a quantity in this unit may carry (0 for whole pieces, 3 for
 *     kilograms); between 0 and 9
 */
public record Unit(String code, int scale) {

  /** Whole pieces, scale 0. */
  public static final Unit PCS = new Unit("pcs", 0);

  /** Kilograms, scale 3 (gram precision). */
  public static final Unit KG = new Unit("kg", 3);

  /** Grams, scale 0. */
  public static final Unit GRAM = new Unit("g", 0);

  /** Litres, scale 3 (millilitre precision). */
  public static final Unit LITRE = new Unit("l", 3);

  /** Millilitres, scale 0. */
  public static final Unit MILLILITRE = new Unit("ml", 0);

  /** Metres, scale 3 (millimetre precision). */
  public static final Unit METRE = new Unit("m", 3);

  /** Feet, scale 2. */
  public static final Unit FOOT = new Unit("ft", 2);

  /**
   * Validates the components.
   *
   * @throws IllegalArgumentException if the code is blank or the scale is outside 0 to 9
   */
  public Unit {
    Objects.requireNonNull(code, "code");
    if (code.isBlank()) {
      throw new IllegalArgumentException("unit code must not be blank");
    }
    if (scale < 0 || scale > 9) {
      throw new IllegalArgumentException("unit scale must be 0 to 9: " + scale);
    }
  }

  /**
   * Creates a unit.
   *
   * @param code short code, non-blank
   * @param scale decimal places allowed, 0 to 9
   * @return the unit
   */
  public static Unit of(String code, int scale) {
    return new Unit(code, scale);
  }
}
