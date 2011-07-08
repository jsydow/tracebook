package de.fu.tracebook.util;

import org.mapsforge.android.maps.GeoPoint;

import Jama.Matrix;
import android.graphics.Point;

/**
 * This is an implementation of the Kalman filter for points given by a GPS
 * receiver. It should correct to some degree the error of the location given by
 * the GPS receiver. By this a smoother GPS track is achieved.
 */
public class KalmanFilter {

    /**
     * This work is licensed under a Creative Commons Attribution 3.0 License.
     * 
     * @author Ahmed Abdelkader, comments by Jan Sydow
     */
    private class KalmanFilterAlgorithm {
        protected Matrix F, B, U, Q;
        protected Matrix H, R;
        protected Matrix P, P0;
        protected Matrix X, X0;

        KalmanFilterAlgorithm() {
            // do nothing
        }

        /**
         * Computes the "a posteriori"-estimate using the measurement vector z.
         * 
         * @param Z
         *            The meausure value.
         */
        void correct(Matrix Z) {

            Matrix S = H.times(P0).times(H.transpose()).plus(R);

            // The Kalman gain. It tries to minimise the error
            // covariance. The Kalman gain is a weight which
            // choses which value is more trustworthy, the estimate
            // or the measured value.
            Matrix K = P0.times(H.transpose()).times(S.inverse());

            // z - H*x0 is the residual. It show the difference
            // between the estimate and the actual measurement.
            // using K as a weight, a new weighted estimated state
            // is computed.
            X = X0.plus(K.times(Z.minus(H.times(X0))));

            // The new error covariance.
            Matrix I = Matrix.identity(P0.getRowDimension(),
                    P0.getColumnDimension());
            P = (I.minus(K.times(H))).times(P0);
        }

        /**
         * Getter method for the vector x.
         * 
         * @return X.
         */
        Matrix getX() {
            return X;
        }

        /**
         * Computes the "a priori"-estimates.
         */
        void predict() {
            X0 = F.times(X).plus(B.times(U));
            P0 = F.times(P).times(F.transpose()).plus(Q);
        }

        /**
         * Setter method for the matrix B.
         * 
         * @param b
         *            The new B.
         */
        void setB(Matrix b) {
            B = b;
        }

        /**
         * Setter method for the matrix F.
         * 
         * @param f
         *            The new F.
         */
        void setF(Matrix f) {
            F = f;
        }

        /**
         * Setter method for the matrix H.
         * 
         * @param h
         *            The new H.
         */
        void setH(Matrix h) {
            H = h;
        }

        /**
         * Setter method for the matrix P.
         * 
         * @param p
         *            The new P.
         */
        void setP(Matrix p) {
            P = p;
        }

        /**
         * Setter method for the vector Q.
         * 
         * @param q
         *            The new Q.
         */
        void setQ(Matrix q) {
            Q = q;
        }

        /**
         * Setter method for the vector R.
         * 
         * @param r
         *            The new R.
         */
        void setR(Matrix r) {
            R = r;
        }

        /**
         * Setter method for the vector u.
         * 
         * @param u
         *            The new u.
         */
        void setU(Matrix u) {
            U = u;
        }

        /**
         * Setter method for the vector x.
         * 
         * @param x
         *            The new x.
         */
        void setX(Matrix x) {
            X = x;
        }
    }

    private KalmanFilterAlgorithm kf = new KalmanFilterAlgorithm();

    private double lastX;

    private double lastY;

    /**
     * Initialises the Kalman filter with first measurements and the matrices
     * the filter works on.
     * 
     * @param x
     *            The first measured x value.
     * @param y
     *            The first measured y value.
     */
    KalmanFilter(double x, double y) {
        buildKf(x, y);
        lastX = x;
        lastY = y;
    }

    /**
     * Applies the Kalman filter to the given point.
     * 
     * @param point
     *            The point as measured.
     * @return The point as corrected by the Kalman Filter.
     */
    public GeoPoint filter(GeoPoint point) {
        double x = point.getLongitude();
        double y = point.getLatitude();
        kf.predict();
        kf.correct(new Matrix(new double[][] { { x, y, x - lastX, y - lastY } })
                .transpose());
        Matrix est = kf.getX();
        lastX = x;
        lastY = y;
        return new GeoPoint((int) est.get(0, 0), (int) est.get(1, 0));
    }

    /**
     * Applies the Kalman filter to the given point.
     * 
     * @param point
     *            The point as measured.
     * @return The point as corrected by the Kalman Filter.
     */
    public Point filter(Point point) {
        double x = point.x;
        double y = point.y;
        kf.predict();
        kf.correct(new Matrix(new double[][] { { x, y, x - lastX, y - lastY } })
                .transpose());
        Matrix est = kf.getX();
        lastX = x;
        lastY = y;
        return new Point((int) est.get(0, 0), (int) est.get(1, 0));
    }

    /**
     * Set up the matrices of the Kalman filter.
     */
    private void buildKf(double x, double y) {

        final double L = 100.0; // initial value for covariance
        final double processNoise = 1.0; // noise of the filter
        final double measurementNoise = 1.0; // noise of the measurement.

        // The state vector. It is the current estimate of the real position
        // made by the Kalman filter. This is the initial value.
        kf.setX(new Matrix(new double[][] { { x, y, 0, 0 } }).transpose());

        // The error covariance matrix. Will be updated in every step.
        // This is the initial value.
        // The correctness of the initial value has an impact on
        // how fast the filter converges.
        kf.setP(Matrix.identity(4, 4).times(L));

        // B and u are a linear factor that is added every time step to x.
        // They are a control factor, with u being the control vector and
        // B being an input matrix.
        // May both be zero.
        // Set to 0 here, because there is no known control.
        kf.setB(new Matrix(4, 4));
        kf.setU(new Matrix(4, 1));

        // State transition matrix. Defines how x changes in each time step
        // Default is the identity.
        kf.setF(new Matrix(new double[][] { { 1, 0, 1, 0 }, { 0, 1, 0, 1 },
                { 0, 0, 1, 0 }, { 0, 0, 0, 1 } }));

        // Process noise matrix. Influences the covariance matrix P. Default is
        // zero. TODO evaluate process noise.
        kf.setQ(Matrix.identity(4, 4).times(processNoise));

        // Observation matrix. Default is the identity.
        kf.setH(Matrix.identity(4, 4));

        // Measurement noise vector.
        kf.setR(Matrix.identity(4, 4).times(measurementNoise));
    }

}
