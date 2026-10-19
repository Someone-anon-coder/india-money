/*
 * File:      Quantity.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   An exact amount of goods in a unit, always held at that unit's scale.
 * Owns:      Scale enforcement, same-unit add/subtract, rounding-explicit scaling.
 * Uses:      Unit, java.math.
 * Used by:   Money.times, UnitConversion.
 * Not here:  Converting between units (UnitConversion) or pricing (Money).
 * Reuse:     Value type; never subclass.
 * Plan:      L1-02
 */
package io.github.someoneanoncoder.indiamoney;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An exact quantity such as 2.500 kg or 12 pcs. The amount is always stored at exactly the unit's
 * scale, so equal quantities are {@code equals} regardless of how they were written.
 *
 * @param amount the numeric amount, scale equal to {@code unit.scale()}
 * @param unit the unit of measure
 */
public record Quantity(BigDecimal amount, Unit unit) implements Comparable<Quantity> {

  /**
   * Validates and normalises the amount to the unit's scale.
   *
   * @throws ArithmeticException if the amount has more decimal places than the unit allows
   */
  public Quantity {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(unit, "unit");
    amount = amount.setScale(unit.scale(), RoundingMode.UNNECESSARY);
  }

  /**
   * Creates a quantity that must fit the unit's scale exactly.
   *
   * @param amount the amount
   * @param unit the unit
   * @return the quantity
   * @throws ArithmeticException if {@code amount} has too many decimal places
   */
  public static Quantity of(BigDecimal amount, Unit unit) {
    return new Quantity(amount, unit);
  }

  /**
   * Creates a quantity, rounding to the unit's scale when needed.
   *
   * @param amount the amount
   * @param unit the unit
   * @param mode rounding to apply if {@code amount} has too many decimal places
   * @return the quantity
   */
  public static Quantity of(BigDecimal amount, Unit unit, RoundingMode mode) {
    Objects.requireNonNull(amount, "amount");
    Objects.requireNonNull(unit, "unit");
    return new Quantity(amount.setScale(unit.scale(), mode), unit);
  }

  /**
   * Creates a quantity from a whole number.
   *
   * @param amount the amount
   * @param unit the unit
   * @return the quantity
   */
  public static Quantity of(long amount, Unit unit) {
    return new Quantity(BigDecimal.valueOf(amount), unit);
  }

  /**
   * Adds a quantity of the same unit.
   *
   * @param other quantity to add
   * @return the sum
   * @throws IllegalArgumentException if the units differ
   */
  public Quantity plus(Quantity other) {
    requireSameUnit(other);
    return new Quantity(amount.add(other.amount), unit);
  }

  /**
   * Subtracts a quantity of the same unit.
   *
   * @param other quantity to subtract
   * @return the difference
   * @throws IllegalArgumentException if the units differ
   */
  public Quantity minus(Quantity other) {
    requireSameUnit(other);
    return new Quantity(amount.subtract(other.amount), unit);
  }

  /**
   * Multiplies by a factor, rounding back to the unit's scale.
   *
   * @param factor the multiplier
   * @param mode rounding to apply
   * @return the scaled quantity
   */
  public Quantity times(BigDecimal factor, RoundingMode mode) {
    Objects.requireNonNull(factor, "factor");
    return of(amount.multiply(factor), unit, mode);
  }

  /**
   * Tells whether the amount is zero.
   *
   * @return {@code true} if zero
   */
  public boolean isZero() {
    return amount.signum() == 0;
  }

  /**
   * Tells whether the amount is below zero.
   *
   * @return {@code true} if negative
   */
  public boolean isNegative() {
    return amount.signum() < 0;
  }

  /**
   * Compares by amount.
   *
   * @param other quantity of the same unit
   * @return negative, zero or positive
   * @throws IllegalArgumentException if the units differ
   */
  @Override
  public int compareTo(Quantity other) {
    requireSameUnit(other);
    return amount.compareTo(other.amount);
  }

  private void requireSameUnit(Quantity other) {
    if (!unit.equals(other.unit)) {
      throw new IllegalArgumentException(
          "unit mismatch: " + unit.code() + " vs " + other.unit.code());
    }
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + unit.code();
  }
}
