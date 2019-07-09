

Floating point division - using Erik's inversion method.

The divisor is inverted and the result is multiplied by the dividend.

Internal format is not IEEE 754 single precision float. (Mantissas are the same but exponents are different. No bias in Erik's format)

Coefficients are calculated by master students in their thesis (Chen & Shi).

Most significant 6 bits of the mantissa are used for addressing the coefficients.

Conversion of coefficients into binary (coeffcients are smaller than 1):
decimal 0.25 = binary 0.010
decimal 0.625 = binary 0.101 (integer part and fraction part are converted seperately)

