/*
 * File:      EnglishAmountInWordsUnitTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Unit tests for the English amount in words.
 * Owns:      Wording rules that the golden file does not isolate.
 * Uses:      AmountInWords
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-04
 */
package io.github.someoneanoncoder.indiamoney.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiamoney.AmountInWords;
import io.github.someoneanoncoder.indiamoney.Money;
import io.github.someoneanoncoder.indiamoney.testing.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class EnglishAmountInWordsUnitTest {

  private final AmountInWords words = AmountInWords.english();

  @Test
  @DisplayName("the plan example reads exactly")
  void shouldMatchPlanExample_whenOneLakhTwentyFiveThousandFifty() {
    assertThat(words.words(Money.ofPaise(12500050)))
        .isEqualTo("One Lakh Twenty-Five Thousand Rupees and Fifty Paise Only");
  }

  @Test
  @DisplayName("the shared instance is returned")
  void shouldReturnSharedInstance_whenAskedForEnglish() {
    assertThat(AmountInWords.english()).isSameAs(EnglishAmountInWords.INSTANCE);
  }

  @Test
  @DisplayName("extreme values do not fail")
  void shouldSpell_whenAtLongLimits() {
    assertThat(words.words(Money.ofPaise(Long.MAX_VALUE))).endsWith("Rupees and Seven Paise Only");
    assertThat(words.words(Money.ofPaise(Long.MIN_VALUE))).startsWith("Minus ");
  }
}
