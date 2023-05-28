package io.github.epi155.recfm.type;

/**
 * Audit check availability
 */
public interface CheckAware {
    /**
     * Audit enable check
     *
     * @return enable
     */
    boolean isAudit();
}
