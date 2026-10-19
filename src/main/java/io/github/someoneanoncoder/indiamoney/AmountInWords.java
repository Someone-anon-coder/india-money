/*
 * File:      AmountInWords.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   Contract for spelling out a rupee amount, with the English implementation.
 * Owns:      The words(Money) contract and the english() factory.
 * Uses:      Money, internal.EnglishAmountInWords.
 * Used by:   Cheque, invoice and receipt rendering in consumers.
 * Not here:  Hindi or Marathi wording (implement this interface).
 * Reuse:     Implement for other languages; do not change the English output within 0.x without a changelog entry.
 * Plan:      L1-04
 */
package io.github.someoneanoncoder.indiamoney;

import io.github.someoneanoncoder.indiamoney.internal.EnglishAmountInWords;

/** Spells an amount in words in one language. */
public interface AmountInWords {

  /**
   * Spells out an amount.
   *
   * @param amount the amount, may be zero or negative
   * @return the amount in words, for example {@code "One Lakh Twenty-Five Thousand Rupees and Fifty
   *     Paise Only"}
   */
  String words(Money amount);

  /**
   * Returns the English implementation using the Indian system (thousand, lakh, crore).
   *
   * <p>Rules: no "and" inside the rupee figure; "and" only joins rupees and paise; amounts of one
   * crore and above are spelt as "<i>n</i> Crore" with <i>n</i> itself spelt out (so 100 crore is
   * "One Hundred Crore"); zero rupees with paise reads as paise alone ("Fifty Paise Only"); zero is
   * "Zero Rupees Only"; negatives are prefixed "Minus"; singular is "Rupee" and "Paisa".
   *
   * @return a thread-safe English implementation
   */
  static AmountInWords english() {
    return EnglishAmountInWords.INSTANCE;
  }
}
