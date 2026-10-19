/*
 * File:      IndianNumberFormatSecurityTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Security tests for parsing untrusted text.
 * Owns:      Hostile input produces IllegalArgumentException only, quickly.
 * Uses:      IndianNumberFormat.parse
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-03
 */
package io.github.someoneanoncoder.indiamoney.format;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiamoney.testing.SecurityTest;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@SecurityTest
class IndianNumberFormatSecurityTest {

  @Test
  @DisplayName("hostile strings raise IllegalArgumentException and nothing else")
  void shouldOnlyThrowIllegalArgument_whenInputIsHostile() {
    String[] hostile = {
      "\u0000",
      "1\n2",
      "9".repeat(39),
      "-".repeat(39),
      "NaN",
      "Infinity",
      "0x10",
      "١٢",
      "1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9",
      "%s%n",
      "${jndi:ldap://x}",
      "₹₹₹",
      "Rs.Rs.5",
      "1_000"
    };
    for (String text : hostile) {
      assertThatCode(
              () -> {
                try {
                  IndianNumberFormat.parse(text);
                } catch (IllegalArgumentException expected) {
                  // rejecting is fine
                }
              })
          .as("input %s", text)
          .doesNotThrowAnyException();
    }
  }

  @Test
  @Timeout(2)
  @DisplayName("long inputs are refused before any heavy work")
  void shouldRefuseQuickly_whenInputIsHuge() {
    String huge = "9".repeat(5_000_000);
    assertThatThrownBy(() -> IndianNumberFormat.parse(huge))
        .isInstanceOf(IllegalArgumentException.class);
    org.junit.jupiter.api.Assertions.assertTimeout(
        Duration.ofSeconds(1),
        () -> {
          try {
            IndianNumberFormat.parse("1" + ",".repeat(30) + "1");
          } catch (IllegalArgumentException ignored) {
            // fine
          }
        });
  }
}
