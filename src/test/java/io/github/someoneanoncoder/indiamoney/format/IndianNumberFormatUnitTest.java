/*
 * File:      IndianNumberFormatUnitTest.java
 * Module:    india-money
 * Layer:     test
 * Purpose:   Unit tests for IndianNumberFormat.
 * Owns:      Grouping, compact and parse cases.
 * Uses:      IndianNumberFormat
 * Used by:   Gradle test tasks.
 * Not here:  Production code.
 * Reuse:     Test only.
 * Plan:      L1-03
 */
package io.github.someoneanoncoder.indiamoney.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.someoneanoncoder.indiamoney.Money;
import io.github.someoneanoncoder.indiamoney.testing.UnitTest;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class IndianNumberFormatUnitTest {

  private static Money rupees(String text) {
    return IndianNumberFormat.parse(text);
  }

  @Test
  @DisplayName("groups as last three digits then pairs")
  void shouldGroupLakhCrore_whenFormatting() {
    assertThat(IndianNumberFormat.format(Money.ZERO)).isEqualTo("₹0.00");
    assertThat(IndianNumberFormat.format(Money.ofPaise(5))).isEqualTo("₹0.05");
    assertThat(IndianNumberFormat.format(Money.ofRupees(999))).isEqualTo("₹999.00");
    assertThat(IndianNumberFormat.format(Money.ofRupees(1000))).isEqualTo("₹1,000.00");
    assertThat(IndianNumberFormat.format(Money.ofRupees(125000))).isEqualTo("₹1,25,000.00");
    assertThat(IndianNumberFormat.format(Money.ofPaise(1234567890123L)))
        .isEqualTo("₹12,34,56,78,901.23");
    assertThat(IndianNumberFormat.format(Money.ofPaise(-12500050))).isEqualTo("-₹1,25,000.50");
    assertThat(IndianNumberFormat.formatPlain(Money.ofRupees(125000))).isEqualTo("1,25,000.00");
    assertThat(IndianNumberFormat.formatPlain(Money.ofPaise(-100))).isEqualTo("-1.00");
  }

  @Test
  @DisplayName("formats the extreme long values")
  void shouldFormat_whenAtLongLimits() {
    assertThat(IndianNumberFormat.formatPlain(Money.ofPaise(Long.MIN_VALUE)))
        .isEqualTo("-92,23,37,20,36,85,47,758.08");
  }

  @Test
  @DisplayName("compact form uses L and Cr")
  void shouldUseLakhAndCrore_whenCompact() {
    assertThat(IndianNumberFormat.formatCompact(Money.ofRupees(125000))).isEqualTo("1.25 L");
    assertThat(IndianNumberFormat.formatCompact(Money.ofRupees(230_00_000))).isEqualTo("2.3 Cr");
    assertThat(IndianNumberFormat.formatCompact(Money.ofRupees(100000))).isEqualTo("1 L");
    assertThat(IndianNumberFormat.formatCompact(Money.ofRupees(1_00_00_000))).isEqualTo("1 Cr");
    assertThat(IndianNumberFormat.formatCompact(Money.ofRupees(1234_00_00_000L)))
        .isEqualTo("1,234 Cr");
    assertThat(IndianNumberFormat.formatCompact(Money.ofRupees(-250000))).isEqualTo("-2.5 L");
  }

  @Test
  @DisplayName("compact rounds half up and carries into crore")
  void shouldCarryIntoCrore_whenLakhRoundsToHundred() {
    assertThat(IndianNumberFormat.formatCompact(rupees("99,99,999.00"))).isEqualTo("1 Cr");
    assertThat(IndianNumberFormat.formatCompact(rupees("1,23,456"))).isEqualTo("1.23 L");
    assertThat(IndianNumberFormat.formatCompact(rupees("1,25,500"))).isEqualTo("1.26 L");
  }

  @Test
  @DisplayName("below a lakh compact shows grouped rupees, paise only when present")
  void shouldShowRupees_whenBelowLakh() {
    assertThat(IndianNumberFormat.formatCompact(Money.ofRupees(99999))).isEqualTo("99,999");
    assertThat(IndianNumberFormat.formatCompact(Money.ofPaise(99950))).isEqualTo("999.50");
    assertThat(IndianNumberFormat.formatCompact(Money.ZERO)).isEqualTo("0");
  }

  @Test
  @DisplayName("parse accepts symbols, commas and prefixes")
  void shouldParseVariants_whenTextLooksLikeRupees() {
    assertThat(rupees("₹1,25,000.00")).isEqualTo(Money.ofRupees(125000));
    assertThat(rupees("125,000.5")).isEqualTo(Money.ofPaise(12500050));
    assertThat(rupees("Rs. 1,250")).isEqualTo(Money.ofRupees(1250));
    assertThat(rupees("rs1250")).isEqualTo(Money.ofRupees(1250));
    assertThat(rupees("INR 99.99")).isEqualTo(Money.ofPaise(9999));
    assertThat(rupees("-₹5")).isEqualTo(Money.ofRupees(-5));
    assertThat(rupees("₹-5.25")).isEqualTo(Money.ofPaise(-525));
    assertThat(rupees("+7")).isEqualTo(Money.ofRupees(7));
  }

  @Test
  @DisplayName("parse rejects junk, extra decimals, overflow and long text")
  void shouldRejectBadText_whenParsing() {
    for (String bad :
        new String[] {"", " ", "abc", "1.234", "1e5", "1..2", ".5", "5.", "--5", "₹"}) {
      assertThatThrownBy(() -> IndianNumberFormat.parse(bad))
          .as("input '%s'", bad)
          .isInstanceOf(IllegalArgumentException.class);
    }
    assertThatThrownBy(() -> IndianNumberFormat.parse("99999999999999999999"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> IndianNumberFormat.parse("1".repeat(41)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("group rejects negatives and handles small values")
  void shouldGroup_whenGivenIntegers() {
    assertThat(IndianNumberFormat.group(BigInteger.ZERO)).isEqualTo("0");
    assertThat(IndianNumberFormat.group(BigInteger.valueOf(1000))).isEqualTo("1,000");
    assertThat(IndianNumberFormat.group(BigInteger.valueOf(12345))).isEqualTo("12,345");
    assertThat(IndianNumberFormat.group(BigInteger.valueOf(123456))).isEqualTo("1,23,456");
    assertThatThrownBy(() -> IndianNumberFormat.group(BigInteger.valueOf(-1)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
