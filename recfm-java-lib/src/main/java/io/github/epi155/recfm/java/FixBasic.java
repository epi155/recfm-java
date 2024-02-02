package io.github.epi155.recfm.java;

/**
 * Generic method of any fixed record structure
 */
public interface FixBasic extends Validable {
    /**
     * Serialize record
     * @return  serialized string
     */
    String encode();

//    /**
//     * Record length
//     * @return record length
//     */
//    int length();
}
