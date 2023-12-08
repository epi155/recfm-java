package io.github.epi155.recfm.java.rule;


import io.github.epi155.recfm.api.AlignMode;
import io.github.epi155.recfm.api.NormalizeAbcMode;
import io.github.epi155.recfm.api.OverflowAction;
import io.github.epi155.recfm.api.UnderflowAction;

/**
 * Actions on the value of a field
 */
public class Action {
    public static final int F_NRM_NOX = 0x00;
    public static final int F_NRM_RT0 = 0x01;
    public static final int F_NRM_RT1 = 0x02;
    public static final int F_NRM_LT0 = 0x03;
    public static final int F_NRM_LT1 = 0x04;
    public static final int F_NRM_MSK = 0x07;

    public static final int F_OVF_ERR = 0;
    public static final int F_OVF_TRR = 0x01 << 3;
    public static final int F_OVF_TRL = 0x02 << 3;
    public static final int F_OVF_MSK = 0x03 << 3;

    public static final int F_UNF_ERR = 0;
    public static final int F_UNF_PAR = 0x01 << 5;
    public static final int F_UNF_PAL = 0x02 << 5;
    public static final int F_UNF_MSK = 0x03 << 5;
    private Action() {}

    public static int flagSetter(OverflowAction ovfl, UnderflowAction unfl, AlignMode align) {
        int flag = 0;
        if (align == AlignMode.LFT) {
            if (ovfl == OverflowAction.Trunc) {
                flag |= Action.F_OVF_TRR;
            } else /*ovfl == OverflowAction.Error*/ {
                flag |= Action.F_OVF_ERR;
            }
            if (unfl == UnderflowAction.Pad) {
                flag |= Action.F_UNF_PAR;
            } else /*unfl == UnderflowAction.Error*/ {
                flag |= Action.F_UNF_ERR;
            }
        } else /* align == AlignMode.RGT */ {
            if (ovfl == OverflowAction.Trunc) {
                flag |= Action.F_OVF_TRL;
            } else /*ovfl == OverflowAction.Error*/ {
                flag |= Action.F_OVF_ERR;
            }
            if (unfl == UnderflowAction.Pad) {
                flag |= Action.F_UNF_PAL;
            } else /*unfl == UnderflowAction.Error*/ {
                flag |= Action.F_UNF_ERR;
            }
        }
        return flag;
    }
    public static int flagGetter(NormalizeAbcMode norm, AlignMode align) {
        if (align == AlignMode.LFT) {
            switch (norm) {
                case None: return Action.F_NRM_NOX;
                case Trim: return Action.F_NRM_RT0;
                case Trim1: return Action.F_NRM_RT1;
            }
        } else /* align == AlignMode.RGT */ {
            switch (norm) {
                case None: return Action.F_NRM_NOX;
                case Trim: return Action.F_NRM_LT0;
                case Trim1: return Action.F_NRM_LT1;
            }
        }
        return Action.F_NRM_NOX;
    }

}
