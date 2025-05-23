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

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import org.apache.commons.math4.core.jdkmath.JdkMath;
import org.apache.commons.math4.legacy.exception.MathIllegalStateException;
import org.apache.commons.math4.legacy.exception.NullArgumentException;
import org.apache.commons.math4.legacy.exception.util.LocalizedFormats;
import org.apache.commons.statistics.descriptive.DoubleStatistics;
import org.apache.commons.statistics.descriptive.Statistic;

/**
 * <p>
 * Computes summary statistics for a stream of data values added using the
 * {@link #addValue(double) addValue} method. The data values are not stored in
 * memory, so this class can be used to compute statistics for very large data
 * streams.
 * </p>
 * <p>
 * Default implementations can be configured via setters. For example, the
 * default implementation for the variance can be overridden by calling
 * {@link #setVarianceImpl(StorelessUnivariateStatistic)}. Actual parameters to
 * these methods must implement the {@link StorelessUnivariateStatistic}
 * interface and configuration must be completed before <code>addValue</code>
 * is called. No configuration is necessary to use the default
 * provided implementations.
 * </p>
 * <p>
 * Note: This class is not thread-safe. Use
 * {@link SynchronizedSummaryStatistics} if concurrent access from multiple
 * threads is required.
 * </p>
 */
public class SummaryStatistics implements StatisticalSummary {
    /** count of values that have been added. */
    private long n;

    /** Default statistics. */
    private final EnumSet<Statistic> stats = EnumSet.of(Statistic.SUM,
        Statistic.MIN, Statistic.MAX, Statistic.MEAN,
        Statistic.VARIANCE);

    /** Default statistic implementations. */
    private DoubleStatistics values;

    /** Consumer of values. */
    private DoubleConsumer action;

    /** Overridden sum statistic implementation - can be reset by setter. */
    private StorelessUnivariateStatistic sumImpl;

    /** Overridden minimum statistic implementation - can be reset by setter. */
    private StorelessUnivariateStatistic minImpl;

    /** Overridden maximum statistic implementation - can be reset by setter. */
    private StorelessUnivariateStatistic maxImpl;

    /** Overridden mean statistic implementation - can be reset by setter. */
    private StorelessUnivariateStatistic meanImpl;

    /** Overridden variance statistic implementation - can be reset by setter. */
    private StorelessUnivariateStatistic varianceImpl;

    /**
     * Construct a SummaryStatistics instance.
     */
    public SummaryStatistics() {
        // default implementation
        values = DoubleStatistics.of(stats);
    }

    /**
     * A copy constructor. Creates a deep-copy of the {@code original}.
     *
     * @param original the {@code SummaryStatistics} instance to copy
     * @throws NullArgumentException if original is null
     */
    public SummaryStatistics(SummaryStatistics original) throws NullArgumentException {
        copy(original, this);
    }

    /**
     * Return a {@link StatisticalSummaryValues} instance reporting current
     * statistics.
     * @return Current values of statistics
     */
    public StatisticalSummary getSummary() {
        return new StatisticalSummaryValues(getMean(), getVariance(), getN(),
                getMax(), getMin(), getSum());
    }

    /**
     * Add a value to the data.
     * @param value the value to add
     */
    public void addValue(double value) {
        if (n == 0) {
            createAction();
        }
        action.accept(value);
        n++;
    }

    /**
     * Creates the consumer of double values. This should be called when
     * the implementations have been updated. This should only occur when
     * no values have been added, or the object is being copied from another
     * source.
     */
    private void createAction() {
        DoubleConsumer a = null;
        if (!stats.isEmpty()) {
            values = DoubleStatistics.of(stats);
            a = values::accept;
        }
        a = combine(a, sumImpl);
        a = combine(a, minImpl);
        a = combine(a, maxImpl);
        a = combine(a, meanImpl);
        a = combine(a, varianceImpl);
        action = a;
    }

    /**
     * Combine the two consumers of double values, either may be null.
     *
     * @param a Action.
     * @param s Statistic.
     * @return the consumer
     */
    private DoubleConsumer combine(DoubleConsumer a, StorelessUnivariateStatistic s) {
        if (s == null) {
            return a;
        }
        if (a == null) {
            return s::increment;
        }
        return a.andThen(s::increment);
    }

    /**
     * Returns the number of available values.
     * @return The number of available values
     */
    @Override
    public long getN() {
        return n;
    }

    /**
     * Gets the statistic from the overridden implementation, or from the default implementation.
     *
     * @param imp Statistics implementation.
     * @param s Statistic
     * @return the value
     */
    private double getStatistic(StorelessUnivariateStatistic imp, Statistic s) {
        if (getN() == 0) {
            return Double.NaN;
        }
        return imp != null ? imp.getResult() : values.getAsDouble(s);
    }

    /**
     * Returns the sum of the values that have been added.
     * @return The sum or <code>Double.NaN</code> if no values have been added
     */
    @Override
    public double getSum() {
        return getStatistic(sumImpl, Statistic.SUM);
    }

    /**
     * Returns the mean of the values that have been added.
     * <p>
     * Double.NaN is returned if no values have been added.
     * </p>
     * @return the mean
     */
    @Override
    public double getMean() {
        return getStatistic(meanImpl, Statistic.MEAN);
    }

    /**
     * Returns the standard deviation of the values that have been added.
     * <p>
     * Double.NaN is returned if no values have been added.
     * </p>
     * @return the standard deviation
     */
    @Override
    public double getStandardDeviation() {
        double stdDev = Double.NaN;
        if (getN() > 0) {
            if (getN() > 1) {
                stdDev = JdkMath.sqrt(getVariance());
            } else {
                stdDev = 0.0;
            }
        }
        return stdDev;
    }

    /**
     * Returns the (sample) variance of the available values.
     *
     * <p>Double.NaN is returned if no values have been added.</p>
     *
     * @return the variance
     */
    @Override
    public double getVariance() {
        return getStatistic(varianceImpl, Statistic.VARIANCE);
    }

    /**
     * Returns the maximum of the values that have been added.
     * <p>
     * Double.NaN is returned if no values have been added.
     * </p>
     * @return the maximum
     */
    @Override
    public double getMax() {
        return getStatistic(maxImpl, Statistic.MAX);
    }

    /**
     * Returns the minimum of the values that have been added.
     * <p>
     * Double.NaN is returned if no values have been added.
     * </p>
     * @return the minimum
     */
    @Override
    public double getMin() {
        return getStatistic(minImpl, Statistic.MIN);
    }

    /**
     * Generates a text report displaying summary statistics from values that
     * have been added.
     * @return String with line feeds displaying statistics
     * @since 1.2
     */
    @Override
    public String toString() {
        StringBuilder outBuffer = new StringBuilder();
        String endl = "\n";
        outBuffer.append("SummaryStatistics:").append(endl);
        outBuffer.append("n: ").append(getN()).append(endl);
        outBuffer.append("min: ").append(getMin()).append(endl);
        outBuffer.append("max: ").append(getMax()).append(endl);
        outBuffer.append("sum: ").append(getSum()).append(endl);
        outBuffer.append("mean: ").append(getMean()).append(endl);
        outBuffer.append("variance: ").append(getVariance()).append(endl);
        outBuffer.append("standard deviation: ").append(getStandardDeviation())
            .append(endl);
        return outBuffer.toString();
    }

    /**
     * Resets all statistics and storage.
     */
    public void clear() {
        this.n = 0;
        // No clear method for default statistics. Do not set to null as this invalidates
        // default getters when empty so recreate.
        if (!stats.isEmpty()) {
            values = DoubleStatistics.of(stats);
        }
        action = null;
        clear(sumImpl);
        clear(minImpl);
        clear(maxImpl);
        clear(meanImpl);
        clear(varianceImpl);
    }

    /**
     * Clear the statistic if not null.
     *
     * @param s Statistic.
     */
    private static void clear(StorelessUnivariateStatistic s) {
        if (s != null) {
            s.clear();
        }
    }

    // Getters and setters for statistics implementations

    /**
     * Returns the currently configured Sum implementation.
     * This will be null if using the default implementation.
     * @return the StorelessUnivariateStatistic implementing the sum
     * @since 1.2
     */
    public StorelessUnivariateStatistic getSumImpl() {
        return sumImpl;
    }

    /**
     * <p>
     * Sets the implementation for the Sum.
     * </p>
     * <p>
     * This method cannot be activated after data has been added - i.e.,
     * after {@link #addValue(double) addValue} has been used to add data.
     * If it is activated after data has been added, an IllegalStateException
     * will be thrown.
     * </p>
     * @param sumImpl the StorelessUnivariateStatistic instance to use for
     *        computing the Sum
     * @throws MathIllegalStateException if data has already been added (i.e if n &gt;0)
     * @since 1.2
     */
    public void setSumImpl(StorelessUnivariateStatistic sumImpl)
    throws MathIllegalStateException {
        this.sumImpl = requireImplementation(sumImpl, Statistic.SUM);
    }

    /**
     * Returns the currently configured minimum implementation.
     * This will be null if using the default implementation.
     * @return the StorelessUnivariateStatistic implementing the minimum
     * @since 1.2
     */
    public StorelessUnivariateStatistic getMinImpl() {
        return minImpl;
    }

    /**
     * <p>
     * Sets the implementation for the minimum.
     * </p>
     * <p>
     * This method cannot be activated after data has been added - i.e.,
     * after {@link #addValue(double) addValue} has been used to add data.
     * If it is activated after data has been added, an IllegalStateException
     * will be thrown.
     * </p>
     * @param minImpl the StorelessUnivariateStatistic instance to use for
     *        computing the minimum
     * @throws MathIllegalStateException if data has already been added (i.e if n &gt; 0)
     * @since 1.2
     */
    public void setMinImpl(StorelessUnivariateStatistic minImpl)
    throws MathIllegalStateException {
        this.minImpl = requireImplementation(minImpl, Statistic.MIN);
    }

    /**
     * Returns the currently configured maximum implementation.
     * This will be null if using the default implementation.
     * @return the StorelessUnivariateStatistic implementing the maximum
     * @since 1.2
     */
    public StorelessUnivariateStatistic getMaxImpl() {
        return maxImpl;
    }

    /**
     * <p>
     * Sets the implementation for the maximum.
     * </p>
     * <p>
     * This method cannot be activated after data has been added - i.e.,
     * after {@link #addValue(double) addValue} has been used to add data.
     * If it is activated after data has been added, an IllegalStateException
     * will be thrown.
     * </p>
     * @param maxImpl the StorelessUnivariateStatistic instance to use for
     *        computing the maximum
     * @throws MathIllegalStateException if data has already been added (i.e if n &gt; 0)
     * @since 1.2
     */
    public void setMaxImpl(StorelessUnivariateStatistic maxImpl)
    throws MathIllegalStateException {
        this.maxImpl = requireImplementation(maxImpl, Statistic.MAX);
    }

    /**
     * Returns the currently configured mean implementation.
     * This will be null if using the default implementation.
     * @return the StorelessUnivariateStatistic implementing the mean
     * @since 1.2
     */
    public StorelessUnivariateStatistic getMeanImpl() {
        return meanImpl;
    }

    /**
     * <p>
     * Sets the implementation for the mean.
     * </p>
     * <p>
     * This method cannot be activated after data has been added - i.e.,
     * after {@link #addValue(double) addValue} has been used to add data.
     * If it is activated after data has been added, an IllegalStateException
     * will be thrown.
     * </p>
     * @param meanImpl the StorelessUnivariateStatistic instance to use for
     *        computing the mean
     * @throws MathIllegalStateException if data has already been added (i.e if n &gt; 0)
     * @since 1.2
     */
    public void setMeanImpl(StorelessUnivariateStatistic meanImpl)
    throws MathIllegalStateException {
        this.meanImpl = requireImplementation(meanImpl, Statistic.MEAN);
    }

    /**
     * Returns the currently configured variance implementation.
     * This will be null if using the default implementation.
     * @return the StorelessUnivariateStatistic implementing the variance
     * @since 1.2
     */
    public StorelessUnivariateStatistic getVarianceImpl() {
        return varianceImpl;
    }

    /**
     * <p>
     * Sets the implementation for the variance.
     * </p>
     * <p>
     * This method cannot be activated after data has been added - i.e.,
     * after {@link #addValue(double) addValue} has been used to add data.
     * If it is activated after data has been added, an IllegalStateException
     * will be thrown.
     * </p>
     * @param varianceImpl the StorelessUnivariateStatistic instance to use for
     *        computing the variance
     * @throws MathIllegalStateException if data has already been added (i.e if n &gt; 0)
     * @since 1.2
     */
    public void setVarianceImpl(StorelessUnivariateStatistic varianceImpl)
    throws MathIllegalStateException {
        this.varianceImpl = requireImplementation(varianceImpl, Statistic.VARIANCE);
    }

    /**
     * Checks the implementation can be set.
     * Throws {@link IllegalStateException} if {@code n > 0} or a {@link NullPointerException}
     * if the implementation is null.
     *
     * @param imp Implementation.
     * @param s Statistic to override.
     * @return the implementation
     * @throws MathIllegalStateException if data has been added
     */
    private StorelessUnivariateStatistic requireImplementation(StorelessUnivariateStatistic imp, Statistic s) {
        if (n > 0) {
            throw new MathIllegalStateException(
                LocalizedFormats.VALUES_ADDED_BEFORE_CONFIGURING_STATISTIC, n);
        }
        Objects.requireNonNull(imp, () -> "implementation for " + s);
        // Remove the default implementation
        stats.remove(s);
        return imp;
    }

    /**
     * Returns a copy of this SummaryStatistics instance with the same internal state.
     *
     * @return a copy of this
     */
    public SummaryStatistics copy() {
        SummaryStatistics result = new SummaryStatistics();
        // No try-catch or advertised exception because arguments are guaranteed non-null
        copy(this, result);
        return result;
    }

    /**
     * Copies source to dest.
     * <p>Neither source nor dest can be null.</p>
     *
     * @param source SummaryStatistics to copy
     * @param dest SummaryStatistics to copy to
     * @throws NullArgumentException if either source or dest is null
     */
    public static void copy(SummaryStatistics source, SummaryStatistics dest)
        throws NullArgumentException {
        NullArgumentException.check(source);
        NullArgumentException.check(dest);
        dest.n = source.n;
        // Set up implementations
        dest.stats.retainAll(source.stats);
        dest.sumImpl = copy(source.sumImpl);
        dest.minImpl = copy(source.minImpl);
        dest.maxImpl = copy(source.maxImpl);
        dest.meanImpl = copy(source.meanImpl);
        dest.varianceImpl = copy(source.varianceImpl);
        dest.createAction();
        // populate default implementations
        if (!dest.stats.isEmpty()) {
            dest.values.combine(source.values);
        }
    }

    /**
     * Copy the statistic if not null.
     *
     * @param s Statistic.
     * @return the statistic (or null)
     */
    private static StorelessUnivariateStatistic copy(StorelessUnivariateStatistic s) {
        return s != null ? s.copy() : null;
    }
}
