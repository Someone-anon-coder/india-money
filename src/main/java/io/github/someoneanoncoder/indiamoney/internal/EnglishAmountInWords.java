/*
 * File:      EnglishAmountInWords.java
 * Module:    india-money
 * Layer:     internal
 * Purpose:   English spelling of rupee amounts in the Indian number system.
 * Owns:      Number-to-words algorithm and currency wording.
 * Uses:      Money.
 * Used by:   AmountInWords.english().
 * Not here:  Other languages.
 * Reuse:     Not exported; reach it through AmountInWords.english().
 * Plan:      L1-04
 */
package io.github.someoneanoncoder.indiamoney.internal;

import io.github.someoneanoncoder.indiamoney.AmountInWords;
import io.github.someoneanoncoder.indiamoney.Money;

/** English implementation of {@link AmountInWords}. */
public final class EnglishAmountInWords implements AmountInWords {

  /** Shared stateless instance. */
  public static final EnglishAmountInWords INSTANCE = new EnglishAmountInWords();

  private static final String[] ONES = {
    "Zero",
    "One",
    "Two",
    "Three",
    "Four",
    "Five",
    "Six",
    "Seven",
    "Eight",
    "Nine",
    "Ten",
    "Eleven",
    "Twelve",
    "Thirteen",
    "Fourteen",
    "Fifteen",
    "Sixteen",
    "Seventeen",
    "Eighteen",
    "Nineteen"
  };
  private static final String[] TENS = {
    "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
  };
  private static final long CRORE = 10_000_000L;
  private static final long LAKH = 100_000L;

  private EnglishAmountInWords() {}

  @Override
  public String words(Money amount) {
    long paise = amount.paise();
    long rupees = Math.abs(paise / 100);
    long fraction = Math.abs(paise % 100);
    if (rupees == 0 && fraction == 0) {
      return "Zero Rupees Only";
    }
    StringBuilder text = new StringBuilder(paise < 0 ? "Minus " : "");
    if (rupees > 0) {
      text.append(number(rupees)).append(rupees == 1 ? " Rupee" : " Rupees");
    }
    if (fraction > 0) {
      if (rupees > 0) {
        text.append(" and ");
      }
      text.append(number(fraction)).append(fraction == 1 ? " Paisa" : " Paise");
    }
    return text.append(" Only").toString();
  }

  private static String number(long n) {
    if (n >= CRORE) {
      return number(n / CRORE) + " Crore" + remainder(n % CRORE);
    }
    if (n >= LAKH) {
      return below100(n / LAKH) + " Lakh" + remainder(n % LAKH);
    }
    if (n >= 1000) {
      return below100(n / 1000) + " Thousand" + remainder(n % 1000);
    }
    return below1000(n);
  }

  private static String remainder(long rest) {
    return rest == 0 ? "" : " " + number(rest);
  }

  private static String below1000(long n) {
    if (n >= 100) {
      long rest = n % 100;
      return ONES[(int) (n / 100)] + " Hundred" + (rest == 0 ? "" : " " + below100(rest));
    }
    return below100(n);
  }

  private static String below100(long n) {
    if (n < 20) {
      return ONES[(int) n];
    }
    long unit = n % 10;
    return TENS[(int) (n / 10)] + (unit == 0 ? "" : "-" + ONES[(int) unit]);
  }
}
