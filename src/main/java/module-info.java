/*
 * File:      module-info.java
 * Module:    india-money
 * Layer:     api
 * Purpose:   JPMS descriptor exporting only the public API packages.
 * Owns:      The list of exported packages.
 * Uses:      The JDK (java.base) only.
 * Used by:   Every consumer on the module path.
 * Not here:  Implementation classes (the internal package is not exported).
 * Reuse:     Keep exports minimal.
 * Plan:      L1
 */

/** Exact INR money, quantity and unit conversion, Indian number formatting and amount in words. */
module io.github.someoneanoncoder.indiamoney {
  exports io.github.someoneanoncoder.indiamoney;
  exports io.github.someoneanoncoder.indiamoney.format;
}
