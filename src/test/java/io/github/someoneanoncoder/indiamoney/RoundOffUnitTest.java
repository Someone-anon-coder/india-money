/*
 * File:      RoundOffUnitTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Unit tests for RoundOff.
 * Owns:      Rupee and 50-paise rounding with signed adjustment.
 * Uses:      RoundOff, RoundOffMode
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-05
 */
package io.github.someoneanoncoder.indiamoney;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiamoney.testing.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class RoundOffUnitTest {

  private static void check(long paise, RoundOffMode mode, long rounded, long adjustment) {
    RoundOff result = RoundOff.of(Money.ofPaise(paise), mode);
    assertThat(result.original().paise()).isEqualTo(paise);
    assertThat(result.rounded().paise()).as("rounded %d %s", paise, mode).isEqualTo(rounded);
    assertThat(result.adjustment().paise())
        .as("adjustment %d %s", paise, mode)
        .isEqualTo(adjustment);
  }

  @Test
  @DisplayName("nearest rupee rounds halves away from zero")
  void shouldRoundToRupee_whenNearestRupee() {
    check(1049, RoundOffMode.NEAREST_RUPEE, 1000, -49);
    check(1050, RoundOffMode.NEAREST_RUPEE, 1100, 50);
    check(1099, RoundOffMode.NEAREST_RUPEE, 1100, 1);
    check(1000, RoundOffMode.NEAREST_RUPEE, 1000, 0);
    check(0, RoundOffMode.NEAREST_RUPEE, 0, 0);
    check(-1050, RoundOffMode.NEAREST_RUPEE, -1100, -50);
    check(-1049, RoundOffMode.NEAREST_RUPEE, -1000, 49);
  }

  @Test
  @DisplayName("nearest 50 paise")
  void shouldRoundToFiftyPaise_whenNearest50() {
    check(1024, RoundOffMode.NEAREST_50_PAISE, 1000, -24);
    check(1025, RoundOffMode.NEAREST_50_PAISE, 1050, 25);
    check(1074, RoundOffMode.NEAREST_50_PAISE, 1050, -24);
    check(1075, RoundOffMode.NEAREST_50_PAISE, 1100, 25);
    check(-1025, RoundOffMode.NEAREST_50_PAISE, -1050, -25);
  }

  @Test
  @DisplayName("none leaves the total untouched")
  void shouldNotChangeTotal_whenModeNone() {
    check(1049, RoundOffMode.NONE, 1049, 0);
    assertThat(RoundOffMode.NEAREST_RUPEE.stepPaise()).isEqualTo(100);
  }

  @Test
  @DisplayName("record rejects an adjustment that does not reconcile")
  void shouldReject_whenAdjustmentDoesNotReconcile() {
    assertThatThrownBy(() -> new RoundOff(Money.ofPaise(10), Money.ofPaise(20), Money.ofPaise(5)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
