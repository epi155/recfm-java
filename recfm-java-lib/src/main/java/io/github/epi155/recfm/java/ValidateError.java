package io.github.epi155.recfm.java;

/**
 * Type of errors generated during validation
 */
public enum ValidateError {
    /**
     * Char is not a number
     */
    NotNumber,
    /**
     * Char is not an Ascii char
     */
    NotAscii,
    /**
     * Char is not a latin1 / ISO-8859-1 char
     */
    NotLatin,
    /**
     * Char is not a valid java UTF-16 char
     */
    NotValid,
    /**
     * Field value is not in domain
     */
    NotDomain,
    /**
     * Char is not blank
     */
    NotBlank,
    /**
     * Field constant different from expected
     */
    NotEqual,
    /**
     * Field does not match regex pattern
     */
    NotMatch,
    /**
     * Field DigitOrBlank begins with neither a digit nor a space
     */
    NotDigitBlank,
}
