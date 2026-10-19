/*
 * File:      Money.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   Immutable Indian rupee amount held as a whole number of paise.
 * Owns:      Exact add/subtract, rounding-explicit multiplication, percentages, and lossless allocation.
 * Uses:      java.math only.
 * Used by:   Quantity-based pricing code, IndianNumberFormat, AmountInWords, RoundOff.
 * Not here:  Pricing, discount or tax rules; formatting (IndianNumberFormat); words (AmountInWords).
 * Reuse:     Value type; never subclass. All arithmetic is exact and throws on overflow.
 * Plan:      L1-01
 */
package io.github.someoneanoncoder.indiamoney;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An amount of Indian rupees stored as a signed {@code long} count of paise (1 rupee = 100 paise).
 *
 * <p>No floating point is ever involved. Addition, subtraction and negation throw {@link
 * ArithmeticException} on overflow instead of wrapping. Every operation that can lose precision
 * (multiplying by a fraction, taking a percentage) requires an explicit {@link RoundingMode}.
 *
 * @param paise the amount in paise; negative values are credits or refunds
 */
public record Money(long paise) implements Comparable<Money> {

  /** Zero rupees. */
  public static final Money ZERO = new Money(0);

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  /**
   * Creates an amount from a paise count.
   *
   * @param paise amount in paise
   * @return the amount
   */
  public static Money ofPaise(long paise) {
    return new Money(paise);
  }

  /**
   * Creates an amount from whole rupees.
   *
   * @param rupees whole rupees
   * @return the amount
   * @throws ArithmeticException if the paise count does not fit in a {@code long}
   */
  public static Money ofRupees(long rupees) {
    return new Money(Math.multiplyExact(rupees, 100L));
  }

  /**
   * Creates an amount from a decimal rupee value, rounding to whole paise.
   *
   * @param rupees decimal rupees, for example {@code 12.345}
   * @param mode how to round when {@code rupees} has more than two decimal places
   * @return the amount
   * @throws ArithmeticException if {@code mode} is {@link RoundingMode#UNNECESSARY} and rounding is
   *     needed, or the result does not fit in a {@code long}
   */
  public static Money ofRupees(BigDecimal rupees, RoundingMode mode) {
    Objects.requireNonNull(rupees, "rupees");
    Objects.requireNonNull(mode, "mode");
    return new Money(rupees.multiply(HUNDRED).setScale(0, mode).longValueExact());
  }

  /**
   * Adds another amount.
   *
   * @param other amount to add
   * @return the sum
   * @throws ArithmeticException on overflow
   */
  public Money plus(Money other) {
    return new Money(Math.addExact(paise, other.paise));
  }

  /**
   * Subtracts another amount.
   *
   * @param other amount to subtract
   * @return the difference
   * @throws ArithmeticException on overflow
   */
  public Money minus(Money other) {
    return new Money(Math.subtractExact(paise, other.paise));
  }

  /**
   * Returns the amount with its sign flipped.
   *
   * @return the negated amount
   * @throws ArithmeticException if the amount is {@code Long.MIN_VALUE} paise
   */
  public Money negate() {
    return new Money(Math.negateExact(paise));
  }

  /**
   * Returns the absolute value.
   *
   * @return this amount if not negative, otherwise its negation
   * @throws ArithmeticException if the amount is {@code Long.MIN_VALUE} paise
   */
  public Money abs() {
    return paise < 0 ? negate() : this;
  }

  /**
   * Multiplies by a whole number, for example a count of identical items.
   *
   * @param count the multiplier
   * @return the exact product
   * @throws ArithmeticException on overflow
   */
  public Money times(long count) {
    return new Money(Math.multiplyExact(paise, count));
  }

  /**
   * Multiplies by a decimal factor such as a rate times a weight, rounding to whole paise.
   *
   * @param factor the multiplier
   * @param mode how to round the product to whole paise
   * @return the product
   * @throws ArithmeticException if {@code mode} is {@link RoundingMode#UNNECESSARY} and rounding is
   *     needed, or the result does not fit in a {@code long}
   */
  public Money times(BigDecimal factor, RoundingMode mode) {
    Objects.requireNonNull(factor, "factor");
    Objects.requireNonNull(mode, "mode");
    return new Money(BigDecimal.valueOf(paise).multiply(factor).setScale(0, mode).longValueExact());
  }

  /**
   * Multiplies this per-unit rate by a quantity.
   *
   * @param quantity the quantity bought or sold
   * @param mode how to round the product to whole paise
   * @return rate times quantity
   * @throws ArithmeticException see {@link #times(BigDecimal, RoundingMode)}
   */
  public Money times(Quantity quantity, RoundingMode mode) {
    return times(quantity.amount(), mode);
  }

  /**
   * Takes a percentage of this amount.
   *
   * @param percent the percentage, for example {@code 18} or {@code 2.5}
   * @param mode how to round to whole paise
   * @return {@code this * percent / 100}
   * @throws ArithmeticException see {@link #times(BigDecimal, RoundingMode)}
   */
  public Money percent(BigDecimal percent, RoundingMode mode) {
    Objects.requireNonNull(percent, "percent");
    Objects.requireNonNull(mode, "mode");
    BigDecimal exact = BigDecimal.valueOf(paise).multiply(percent).divide(HUNDRED);
    return new Money(exact.setScale(0, mode).longValueExact());
  }

  /**
   * Splits this amount into equal parts without losing a paisa. The first {@code paise % parts}
   * parts (by magnitude) carry one extra paisa.
   *
   * @param parts number of parts, at least 1
   * @return {@code parts} amounts that sum exactly to this amount
   * @throws IllegalArgumentException if {@code parts} is less than 1
   */
  public List<Money> allocate(int parts) {
    if (parts < 1) {
      throw new IllegalArgumentException("parts must be at least 1: " + parts);
    }
    long[] ratios = new long[parts];
    java.util.Arrays.fill(ratios, 1L);
    return allocate(ratios);
  }

  /**
   * Splits this amount proportionally to the given ratios using the largest-remainder method, so
   * the parts always sum exactly to this amount. Ties go to the earlier ratio.
   *
   * @param ratios non-negative weights with a positive sum, for example GST rate shares
   * @return one amount per ratio, summing exactly to this amount; a zero ratio receives zero
   * @throws IllegalArgumentException if {@code ratios} is empty, has a negative entry, or sums to 0
   */
  public List<Money> allocate(long... ratios) {
    if (ratios.length == 0) {
      throw new IllegalArgumentException("ratios must not be empty");
    }
    BigInteger total = BigInteger.ZERO;
    for (long ratio : ratios) {
      if (ratio < 0) {
        throw new IllegalArgumentException("ratio must not be negative: " + ratio);
      }
      total = total.add(BigInteger.valueOf(ratio));
    }
    if (total.signum() == 0) {
      throw new IllegalArgumentException("ratios must have a positive sum");
    }
    return distribute(ratios, total);
  }

  private List<Money> distribute(long[] ratios, BigInteger total) {
    BigInteger magnitude = BigInteger.valueOf(paise).abs();
    long[] share = new long[ratios.length];
    long[] remainder = new long[ratios.length];
    long assigned = 0;
    for (int i = 0; i < ratios.length; i++) {
      BigInteger[] qr = magnitude.multiply(BigInteger.valueOf(ratios[i])).divideAndRemainder(total);
      share[i] = qr[0].longValueExact();
      remainder[i] = qr[1].longValue();
      assigned += share[i];
    }
    BigInteger left = magnitude.subtract(BigInteger.valueOf(assigned));
    giveLeftover(share, remainder, left.intValueExact());
    List<Money> parts = new ArrayList<>(ratios.length);
    for (long s : share) {
      parts.add(new Money(paise < 0 ? -s : s));
    }
    return List.copyOf(parts);
  }

  private static void giveLeftover(long[] share, long[] remainder, int left) {
    for (int round = 0; round < left; round++) {
      int best = -1;
      for (int i = 0; i < remainder.length; i++) {
        if (remainder[i] >= 0 && (best < 0 || remainder[i] > remainder[best])) {
          best = i;
        }
      }
      share[best]++;
      remainder[best] = -1;
    }
  }

  /**
   * Tells whether this amount is zero.
   *
   * @return {@code true} if zero
   */
  public boolean isZero() {
    return paise == 0;
  }

  /**
   * Tells whether this amount is below zero.
   *
   * @return {@code true} if negative
   */
  public boolean isNegative() {
    return paise < 0;
  }

  /**
   * Returns the sign of this amount.
   *
   * @return -1, 0 or 1
   */
  public int signum() {
    return Long.signum(paise);
  }

  /**
   * Returns the amount as decimal rupees with exactly two decimal places.
   *
   * @return the rupee value, for example {@code 1250.50}
   */
  public BigDecimal rupees() {
    return BigDecimal.valueOf(paise, 2);
  }

  @Override
  public int compareTo(Money other) {
    return Long.compare(paise, other.paise);
  }

  /**
   * Returns a locale-independent text such as {@code INR 1250.50}. Use {@code IndianNumberFormat}
   * for display.
   *
   * @return the text form
   */
  @Override
  public String toString() {
    return "INR " + rupees().toPlainString();
  }
}
