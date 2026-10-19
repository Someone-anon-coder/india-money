/*
 * File:      IndianNumberFormat.java
 * Module:    india-money
 * Layer:     format
 * Purpose:   Formats and parses rupee amounts with lakh/crore digit grouping.
 * Owns:      Grouping (1,25,000.00), the rupee symbol, compact forms (1.25 L, 2.3 Cr), lenient parsing.
 * Uses:      Money.
 * Used by:   Screens, receipts and exports in consumers.
 * Not here:  Locale-specific digits or non-INR currencies; amount in words (AmountInWords).
 * Reuse:     Stateless static helpers; locale independent.
 * Plan:      L1-03
 */
package io.github.someoneanoncoder.indiamoney.format;

import io.github.someoneanoncoder.indiamoney.Money;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Indian-style formatting of {@link Money}: last three digits, then groups of two. */
public final class IndianNumberFormat {

  private static final String SYMBOL = "₹";
  private static final BigDecimal LAKH_PAISE = BigDecimal.valueOf(10_000_000L);
  private static final BigDecimal CRORE_PAISE = BigDecimal.valueOf(1_000_000_000L);
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final int MAX_PARSE_LENGTH = 40;
  private static final Pattern PLAIN_AMOUNT = Pattern.compile("[-+]?\\d+(\\.\\d{1,2})?");

  private IndianNumberFormat() {}

  /**
   * Formats with the rupee sign and two decimals, for example {@code ₹1,25,000.00}.
   *
   * @param amount the amount
   * @return the formatted text; negatives read {@code -₹1,25,000.00}
   */
  public static String format(Money amount) {
    String sign = amount.isNegative() ? "-" : "";
    return sign + SYMBOL + digits(amount);
  }

  /**
   * Formats without the rupee sign, for example {@code 1,25,000.00}.
   *
   * @param amount the amount
   * @return the grouped number with two decimals; negatives are prefixed with {@code -}
   */
  public static String formatPlain(Money amount) {
    return (amount.isNegative() ? "-" : "") + digits(amount);
  }

  /**
   * Formats in lakh or crore, rounding half up to two decimals and dropping trailing zeros.
   *
   * <p>From one lakh up the result reads {@code 1.25 L} or {@code 2.3 Cr}; below one lakh the
   * amount is shown in rupees, grouped, with paise only when non-zero ({@code 99,999} or {@code
   * 999.50}). No rupee sign is added.
   *
   * @param amount the amount
   * @return the compact text
   */
  public static String formatCompact(Money amount) {
    Money magnitude = amount.abs();
    String sign = amount.isNegative() ? "-" : "";
    BigDecimal paise = BigDecimal.valueOf(magnitude.paise());
    if (paise.compareTo(CRORE_PAISE) >= 0) {
      return sign + scaled(paise, CRORE_PAISE, " Cr");
    }
    if (paise.compareTo(LAKH_PAISE) >= 0) {
      BigDecimal lakhs = paise.divide(LAKH_PAISE, 2, RoundingMode.HALF_UP);
      // 99.996 L rounds to 100.00 L, which must read as 1 Cr.
      boolean carries = lakhs.compareTo(HUNDRED) >= 0;
      return sign + (carries ? scaled(paise, CRORE_PAISE, " Cr") : scaled(paise, LAKH_PAISE, " L"));
    }
    boolean wholeRupees = magnitude.paise() % 100 == 0;
    String text = digits(magnitude);
    return sign + (wholeRupees ? text.substring(0, text.length() - 3) : text);
  }

  /**
   * Parses rupee text leniently. Accepts an optional sign, a leading {@code ₹}, {@code Rs}, {@code
   * Rs.} or {@code INR}, any commas or spaces, and up to two decimals. Comma positions are not
   * checked, so Western grouping such as {@code 125,000.00} also parses.
   *
   * @param text the text to read; at most 40 characters
   * @return the amount
   * @throws IllegalArgumentException if the text is not a rupee amount, has more than two decimals,
   *     or is too large
   */
  public static Money parse(String text) {
    Objects.requireNonNull(text, "text");
    if (text.length() > MAX_PARSE_LENGTH) {
      throw new IllegalArgumentException("amount text too long");
    }
    String cleaned = text.replaceAll("[\\s,]", "");
    cleaned = cleaned.replaceFirst("(?i)^([-+]?)(₹|rs\\.?|inr)", "$1");
    if (!PLAIN_AMOUNT.matcher(cleaned).matches()) {
      throw new IllegalArgumentException("not a rupee amount: " + text);
    }
    try {
      return Money.ofRupees(new BigDecimal(cleaned), RoundingMode.UNNECESSARY);
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException("amount out of range: " + text, e);
    }
  }

  /**
   * Groups the digits of a non-negative integer Indian style.
   *
   * @param value non-negative integer
   * @return digits with commas, for example {@code 12500000} gives {@code 1,25,00,000}
   * @throws IllegalArgumentException if {@code value} is negative
   */
  public static String group(BigInteger value) {
    if (value.signum() < 0) {
      throw new IllegalArgumentException("value must not be negative");
    }
    String digits = value.toString();
    if (digits.length() <= 3) {
      return digits;
    }
    StringBuilder out = new StringBuilder();
    String head = digits.substring(0, digits.length() - 3);
    int firstGroup = head.length() % 2 == 0 ? 2 : 1;
    out.append(head, 0, firstGroup);
    for (int i = firstGroup; i < head.length(); i += 2) {
      out.append(',').append(head, i, i + 2);
    }
    return out.append(',').append(digits.substring(digits.length() - 3)).toString();
  }

  private static String digits(Money amount) {
    BigInteger paise = BigInteger.valueOf(amount.paise()).abs();
    BigInteger[] parts = paise.divideAndRemainder(BigInteger.valueOf(100));
    return group(parts[0]) + String.format(Locale.ROOT, ".%02d", parts[1].intValue());
  }

  private static String scaled(BigDecimal paise, BigDecimal divisor, String suffix) {
    BigDecimal value = paise.divide(divisor, 2, RoundingMode.HALF_UP).stripTrailingZeros();
    if (value.scale() < 0) {
      value = value.setScale(0);
    }
    String whole = group(value.toBigInteger());
    String fraction =
        value.scale() > 0
            ? value.toPlainString().substring(value.toPlainString().indexOf('.'))
            : "";
    return whole + fraction + suffix;
  }
}
