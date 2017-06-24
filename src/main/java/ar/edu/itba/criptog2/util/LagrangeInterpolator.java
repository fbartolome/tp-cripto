package ar.edu.itba.criptog2.util;

import java.math.BigInteger;

/**
 * Created by juanlipuma on Jun/24/17.
 */
public class LagrangeInterpolator {

    public Polynomial interpolate(int[] xs, int[] ys, int modulo) {
        if(xs == null || ys == null) {
            throw new IllegalArgumentException("xs and ys must not be null");
        }
        if(xs.length != ys.length) {
            throw new IllegalArgumentException("Xs length and Ys length must be equal");
        }
        if(xs.length < 2) {
            throw new IllegalArgumentException("Need at least 2 points to interpolate");
        }
        System.out.println("Interpolating polynomial of degree " + (ys.length-1));

        Polynomial nonModuloResult = new Polynomial(0, 0);
        for (int i = 0; i < xs.length; i++) {
//            Polynomial numerator = new Polynomial(ys[i], 0); //ys[i] * x^0
            Polynomial numerator = new Polynomial(1, 0); //1 * x^0
            int denominator = 1;
            for (int j = 0; j < xs.length; j++) {
                if(j != i) {
                    denominator *= (xs[i] - xs[j]);
                    Polynomial x = new Polynomial(1, 1).minus(new Polynomial(xs[j], 0)); // (1 * x^1 - xs[i] * x^0)
                    numerator = numerator.times(x);
                }
            }
            int coefficient = ys[i] * modInverse(denominator, modulo);
            Polynomial term = numerator.times(new Polynomial(coefficient, 0));
            nonModuloResult = nonModuloResult.plus(term);
        }

        // Apply modulo at the end
        Polynomial result = new Polynomial(0, 0);
        for (int degree = nonModuloResult.getDegree(); degree >= 0 ; degree--) {
            result = result.plus(new Polynomial(nonModuloResult.getCoefficients()[degree] % modulo, degree));
        }
        return result;
    }

    private int modInverse(Integer x, Integer mod) {
        return new Integer((new BigInteger(x.toString()).modInverse(new BigInteger(mod.toString()))).toString());
    }
}
