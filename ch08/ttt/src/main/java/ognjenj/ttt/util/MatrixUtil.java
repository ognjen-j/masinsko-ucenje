package ognjenj.ttt.util;

/**
 * @author ognjen
 */
public class MatrixUtil {

    public static double[] sub(double[] a, double[] b) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            res[i] = a[i] - b[i];
        }
        return res;
    }

    public static double mse(double[] a, double[] b) {
        double[] sub = MatrixUtil.sub(a, b);
        double mse = 0.0;
        for (int i = 0; i < sub.length; i++) {
            mse += Math.pow(sub[i], 2);
        }
        return mse / sub.length;
    }

    public static double mulVec(double[] a, double[] b) {
        double mul = 0;
        for (int i = 0; i < a.length; i++) {
            mul += a[i] * b[i];
        }
        return mul;
    }

    public static double mulVec(int[] a, double[] b) {
        double mul = 0;
        for (int i = 0; i < a.length; i++) {
            mul += a[i] * b[i];
        }
        return mul;
    }

    public static double[] intArrayToDoubleArray(int[] input) {
        double[] result = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = input[i];
        }
        return result;
    }
}
