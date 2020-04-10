package ognjenj.ttt.util;

/**
 * @author ognjen
 */
public class MathUtil {

    /**
     * Calculates the value of the sigmoid at point x.
     *
     * @param x
     * @return
     */
    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * Calculates the value of the sigmoid derivative at point x.
     *
     * @param x
     * @return
     */
    public static double dSigmoid(double x) {
        return -(Math.exp(-x) / Math.pow((1 + Math.exp(-x)), 2.0));
    }

    public static double relu(double x) {
        return Math.max(0, x);
    }
}
