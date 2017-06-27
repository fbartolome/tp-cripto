package ar.edu.itba.criptog2.util;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by juanlipuma on Jun/24/17.
 */
public class LagrangeInterpolator {

    public Polynomial interpolate(List<Point> points, int modulo) {
        if (points == null) {
            throw new IllegalArgumentException("xs and ys must not be null");
        }
        int size = points.size();
        if (size < 2) {
            throw new IllegalArgumentException("Need at least 2 points to interpolate");
        }

        Polynomial nonModuloResult = new Polynomial(0, 0);
        for (int i = 0; i < size; i++) {
//            Polynomial numerator = new Polynomial(ys[i], 0); //ys[i] * x^0
            Polynomial numerator = new Polynomial(1, 0); //1 * x^0
            int denominator = 1;
            for (int j = 0; j < size; j++) {
                if(j != i) {
                    denominator *= (points.get(i).getX() - points.get(j).getX());
                    Polynomial x = new Polynomial(1, 1).minus(new Polynomial(points.get(j).getX(), 0)); // (1 * x^1 - xs[i] * x^0)
                    numerator = numerator.times(x);
                }
            }
            int coefficient = points.get(i).getY() * modInverse(denominator, modulo);
            Polynomial term = numerator.times(new Polynomial(coefficient, 0));
            nonModuloResult = nonModuloResult.plus(term);
        }

        // Apply modulo at the end
        Polynomial result = new Polynomial(0, 0);
        for (int degree = nonModuloResult.getDegree(); degree >= 0 ; degree--) {
            BigInteger moduloExponent = nonModuloResult.getCoefficients()[degree].remainder(BigInteger.valueOf(modulo));
            if (moduloExponent.compareTo(BigInteger.ZERO) < 0) {
                moduloExponent = moduloExponent.add(BigInteger.valueOf(modulo));
            }
            result = result.plus(new Polynomial(moduloExponent, degree));
        }
        return result;
    }

    private int modInverse(Integer x, Integer mod) {
        return new Integer((new BigInteger(x.toString()).modInverse(new BigInteger(mod.toString()))).toString());
    }
}
