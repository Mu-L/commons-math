/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math4.legacy.stat.descriptive;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import org.apache.commons.math4.legacy.core.MathArrays;
import org.apache.commons.math4.legacy.exception.MathIllegalArgumentException;
import org.apache.commons.math4.legacy.exception.OutOfRangeException;
import org.apache.commons.math4.legacy.exception.util.LocalizedFormats;
import org.apache.commons.statistics.descriptive.DoubleStatistic;
import org.apache.commons.statistics.descriptive.Quantile;
import org.apache.commons.statistics.descriptive.StatisticResult;
import org.apache.commons.statistics.descriptive.Quantile.EstimationMethod;

/**
 * Utility class delegating computations to Commons Statistics.
 */
final class Statistics {

    /**
     * Represents a function that accepts a range of {@code double[]} values and produces a result.
     *
     * @param <R> the type of the result of the function
     */
    @FunctionalInterface
    private interface RangeFunction<R> {
        /**
         * Applies this function to the given arguments.
         *
         * @param t the function argument
         * @param from Inclusive start of the range.
         * @param to Exclusive end of the range.
         * @return the function result
         */
        R apply(double[] t, int from, int to);
    }

    /**
     * Delegating univariate statistic implementation.
     * This is the base class for descriptive statistics computed using an array range.
     *
     * @param <R> the statistic type
     */
    private static class StatImp<R extends DoubleStatistic> implements UnivariateStatistic {
        /** Statistic function. */
        private final RangeFunction<R> function;

        /**
         * Create an instance.
         *
         * @param function the function
         */
        StatImp(RangeFunction<R> function) {
            this.function = function;
        }

        @Override
        public double evaluate(double[] values) {
            // This method is not used
            throw new IllegalStateException();
        }

        @Override
        public double evaluate(double[] values, int begin, int length) {
            // Support legacy exception behaviour
            MathArrays.verifyValues(values, begin, length);
            return function.apply(values, begin, begin + length).getAsDouble();
        }

        @Override
        public UnivariateStatistic copy() {
            // Return this is safe if the RangeFunction is thread safe
            return this;
        }
    }

    /**
     * Delegating univariate statistic implementation that returns NaN for empty arrays.
     *
     * @param <R> the statistic type
     */
    private static class NaNStatImp<R extends DoubleStatistic> extends StatImp<R> {
        /**
         * Create an instance.
         *
         * @param function the function
         */
        NaNStatImp(RangeFunction<R> function) {
            super(function);
        }

        @Override
        public double evaluate(double[] values, int begin, int length) {
            // Support legacy behaviour of returning NaN for empty data
            final double r = super.evaluate(values, begin, length);
            return length == 0 ? Double.NaN : r;
        }
    }

    /**
     * Mean implementation.
     */
    static final class Mean extends StatImp<org.apache.commons.statistics.descriptive.Mean> {
        /** Default instance. */
        private static final Mean INSTANCE = new Mean();

        /** Create an instance. */
        private Mean() {
            super(org.apache.commons.statistics.descriptive.Mean::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static Mean getInstance() {
            return INSTANCE;
        }
    }

    /**
     * GeometricMean implementation.
     */
    static final class GeometricMean extends StatImp<org.apache.commons.statistics.descriptive.GeometricMean> {
        /** Default instance. */
        private static final GeometricMean INSTANCE = new GeometricMean();

        /** Create an instance. */
        private GeometricMean() {
            super(org.apache.commons.statistics.descriptive.GeometricMean::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static GeometricMean getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Kurtosis implementation.
     */
    static final class Kurtosis extends StatImp<org.apache.commons.statistics.descriptive.Kurtosis> {
        /** Default instance. */
        private static final Kurtosis INSTANCE = new Kurtosis();

        /** Create an instance. */
        private Kurtosis() {
            super(org.apache.commons.statistics.descriptive.Kurtosis::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static Kurtosis getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Max implementation.
     */
    static final class Max extends NaNStatImp<org.apache.commons.statistics.descriptive.Max> {
        /** Default instance. */
        private static final Max INSTANCE = new Max();

        /** Create an instance. */
        private Max() {
            super(org.apache.commons.statistics.descriptive.Max::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static Max getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Min implementation.
     */
    static final class Min extends NaNStatImp<org.apache.commons.statistics.descriptive.Min> {
        /** Default instance. */
        private static final Min INSTANCE = new Min();

        /** Create an instance. */
        private Min() {
            super(org.apache.commons.statistics.descriptive.Min::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static Min getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Skewness implementation.
     */
    static final class Skewness extends StatImp<org.apache.commons.statistics.descriptive.Skewness> {
        /** Default instance. */
        private static final Skewness INSTANCE = new Skewness();

        /** Create an instance. */
        private Skewness() {
            super(org.apache.commons.statistics.descriptive.Skewness::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static Skewness getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Variance implementation.
     */
    static final class Variance extends StatImp<org.apache.commons.statistics.descriptive.Variance> {
        /** Default instance. */
        private static final Variance INSTANCE = new Variance();

        /** Create an instance. */
        private Variance() {
            super(org.apache.commons.statistics.descriptive.Variance::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static Variance getInstance() {
            return INSTANCE;
        }
    }

    /**
     * SumOfSquares implementation.
     */
    static final class SumOfSquares extends NaNStatImp<org.apache.commons.statistics.descriptive.SumOfSquares> {
        /** Default instance. */
        private static final SumOfSquares INSTANCE = new SumOfSquares();

        /** Create an instance. */
        private SumOfSquares() {
            super(org.apache.commons.statistics.descriptive.SumOfSquares::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static SumOfSquares getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Sum implementation.
     */
    static final class Sum extends NaNStatImp<org.apache.commons.statistics.descriptive.Sum> {
        /** Default instance. */
        private static final Sum INSTANCE = new Sum();

        /** Create an instance. */
        private Sum() {
            super(org.apache.commons.statistics.descriptive.Sum::ofRange);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static Sum getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Percentile implementation.
     */
    static final class Percentile implements UnivariateStatistic {
        /** Delegate percentile implementation. */
        private static final Quantile QUANTILE =
            Quantile.withDefaults().with(EstimationMethod.HF6).withCopy(false);

        /** Probability. */
        private double p;

        /**
         * Create an instance.
         *
         * @param p Probability in [0, 1].
         */
        private Percentile(double p) {
            this.p = p;
        }

        /**
         * Create an instance.
         *
         * @param percentile Percentile in [0, 100].
         * @return an instance
         * @throws OutOfRangeException if the percentile is invalid.
         */
        static Percentile create(double percentile) {
            return new Percentile(createProbability(percentile));
        }

        /**
         * Create the probability from the percentile.
         *
         * @param percentile Percentile in [0, 100].
         * @return probability in [0, 1]
         * @throws OutOfRangeException if the percentile is invalid.
         */
        static double createProbability(double percentile) {
            if (percentile <= 100 && percentile >= 0) {
                return percentile / 100;
            }
            // NaN or out of range
            throw new OutOfRangeException(LocalizedFormats.OUT_OF_RANGE, percentile, 0, 100);
        }

        /**
         * Sets the quantile (in percent).
         * Note: This is named using the CM legacy method name
         * setQuantile rather than setPercentile.
         *
         * @param percentile Percentile in [0, 100].
         * @throws OutOfRangeException if the percentile is invalid.
         */
        void setQuantile(double percentile) {
            p = createProbability(percentile);
        }

        @Override
        public double evaluate(double[] values) {
            MathArrays.verifyValues(values, 0, 0);
            return QUANTILE.evaluate(values, p);
        }

        @Override
        public double evaluate(double[] values, int begin, int length) {
            MathArrays.verifyValues(values, begin, length);
            return QUANTILE.evaluateRange(values, begin, begin + length, p);
        }

        @Override
        public UnivariateStatistic copy() {
            return new Percentile(p);
        }
    }

    /**
     * Delegating storeless univariate statistic implementation.
     * This is the base class for descriptive statistics computed using a single double value.
     *
     * @param <R> the statistic type
     */
    private static class StorelessStatImp<R extends DoubleStatistic & StatisticResult> implements StorelessUnivariateStatistic {
        /** Statistic factory function. */
        private final Supplier<R> factory;
        /** Statistic combine function. */
        private final BinaryOperator<R> combine;
        /** Statistic. */
        private R s;

        /**
         * Create an instance.
         *
         * @param factory the factory function
         * @param combine the combine function
         */
        StorelessStatImp(Supplier<R> factory, BinaryOperator<R> combine) {
            this.factory = factory;
            this.combine = combine;
            s = factory.get();
        }

        @Override
        public void increment(double d) {
            s.accept(d);
        }

        @Override
        public void incrementAll(double[] values) throws MathIllegalArgumentException {
            // This method is not used
            throw new IllegalStateException();
        }

        @Override
        public void incrementAll(double[] values, int start, int length) throws MathIllegalArgumentException {
            // This method is not used
            throw new IllegalStateException();
        }

        @Override
        public double getResult() {
            return s.getAsDouble();
        }

        @Override
        public long getN() {
            // This method is not used
            throw new IllegalStateException();
        }

        @Override
        public void clear() {
            s = factory.get();
        }

        @Override
        public StorelessUnivariateStatistic copy() {
            final StorelessStatImp<R> r = new StorelessStatImp<R>(factory, combine);
            r.s = combine.apply(r.s, s);
            return r;
        }

        @Override
        public double evaluate(double[] values) throws MathIllegalArgumentException {
            // This method is not used
            throw new IllegalStateException();
        }

        @Override
        public double evaluate(double[] values, int begin, int length) throws MathIllegalArgumentException {
            // This method is not used
            throw new IllegalStateException();
        }
    }

    /**
     * Storeless Sum implementation.
     */
    static final class StorelessSum extends StorelessStatImp<org.apache.commons.statistics.descriptive.Sum> {
        /** Create an instance. */
        private StorelessSum() {
            super(org.apache.commons.statistics.descriptive.Sum::create, org.apache.commons.statistics.descriptive.Sum::combine);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static StorelessSum create() {
            return new StorelessSum();
        }
    }

    /**
     * Storeless SumOfSquares implementation.
     */
    static final class StorelessSumOfSquares extends StorelessStatImp<org.apache.commons.statistics.descriptive.SumOfSquares> {
        /** Create an instance. */
        private StorelessSumOfSquares() {
            super(org.apache.commons.statistics.descriptive.SumOfSquares::create, org.apache.commons.statistics.descriptive.SumOfSquares::combine);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static StorelessSumOfSquares create() {
            return new StorelessSumOfSquares();
        }
    }

    /**
     * Storeless Min implementation.
     */
    static final class StorelessMin extends StorelessStatImp<org.apache.commons.statistics.descriptive.Min> {
        /** Create an instance. */
        private StorelessMin() {
            super(org.apache.commons.statistics.descriptive.Min::create, org.apache.commons.statistics.descriptive.Min::combine);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static StorelessMin create() {
            return new StorelessMin();
        }
    }

    /**
     * Storeless Max implementation.
     */
    static final class StorelessMax extends StorelessStatImp<org.apache.commons.statistics.descriptive.Max> {
        /** Create an instance. */
        private StorelessMax() {
            super(org.apache.commons.statistics.descriptive.Max::create, org.apache.commons.statistics.descriptive.Max::combine);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static StorelessMax create() {
            return new StorelessMax();
        }
    }

    /**
     * Storeless SumOfLogs implementation.
     */
    static final class StorelessSumOfLogs extends StorelessStatImp<org.apache.commons.statistics.descriptive.SumOfLogs> {
        /** Create an instance. */
        private StorelessSumOfLogs() {
            super(org.apache.commons.statistics.descriptive.SumOfLogs::create, org.apache.commons.statistics.descriptive.SumOfLogs::combine);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static StorelessSumOfLogs create() {
            return new StorelessSumOfLogs();
        }
    }

    /**
     * Storeless GeometricMean implementation.
     */
    static final class StorelessGeometricMean extends StorelessStatImp<org.apache.commons.statistics.descriptive.GeometricMean> {
        /** Create an instance. */
        private StorelessGeometricMean() {
            super(org.apache.commons.statistics.descriptive.GeometricMean::create, org.apache.commons.statistics.descriptive.GeometricMean::combine);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static StorelessGeometricMean create() {
            return new StorelessGeometricMean();
        }
    }

    /**
     * Storeless Mean implementation.
     */
    static final class StorelessMean extends StorelessStatImp<org.apache.commons.statistics.descriptive.Mean> {
        /** Create an instance. */
        private StorelessMean() {
            super(org.apache.commons.statistics.descriptive.Mean::create, org.apache.commons.statistics.descriptive.Mean::combine);
        }

        /**
         * Gets an instance.
         *
         * @return instance
         */
        static StorelessMean create() {
            return new StorelessMean();
        }
    }

    /** No instances. */
    private Statistics() {
        // Do nothing
    }
}
