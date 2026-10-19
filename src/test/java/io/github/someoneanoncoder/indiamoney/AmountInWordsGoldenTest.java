/*
 * File:      AmountInWordsGoldenTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Golden test for English amount in words.
 * Owns:      200 hand-checked values from golden/EnglishAmountInWords/amounts.txt.
 * Uses:      AmountInWords, Money
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-04
 */
package io.github.someoneanoncoder.indiamoney;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.someoneanoncoder.indiamoney.testing.GoldenTest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@GoldenTest
class AmountInWordsGoldenTest {

  private static final String FILE =
      "/io/github/someoneanoncoder/indiamoney/golden/EnglishAmountInWords/amounts.txt";

  private record Case(String rupees, String expected) {}

  private static List<Case> cases() throws IOException {
    try (InputStream in = AmountInWordsGoldenTest.class.getResourceAsStream(FILE)) {
      assertThat(in).as("golden file").isNotNull();
      List<Case> cases = new ArrayList<>();
      for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
        if (!line.isBlank() && !line.startsWith("#")) {
          int bar = line.indexOf('|');
          cases.add(new Case(line.substring(0, bar), line.substring(bar + 1).strip()));
        }
      }
      return cases;
    }
  }

  @Test
  @DisplayName("every hand-checked amount reads as approved")
  void shouldMatchApprovedWords_whenSpellingEachGoldenAmount() throws IOException {
    for (Case c : cases()) {
      Money amount = Money.ofRupees(new BigDecimal(c.rupees()), RoundingMode.UNNECESSARY);
      assertThat(AmountInWords.english().words(amount))
          .as("amount %s", c.rupees())
          .isEqualTo(c.expected());
    }
  }

  @Test
  @DisplayName("the golden set has 200 values including the required edge cases")
  void shouldContainRequiredEdgeCases_whenGoldenFileLoaded() throws IOException {
    List<String> amounts = cases().stream().map(Case::rupees).toList();
    assertThat(amounts).hasSizeGreaterThanOrEqualTo(200).doesNotHaveDuplicates();
    assertThat(amounts).contains("0.00", "0.01", "99999999.99", "999999999.99");
  }
}
