package fi.wsnusbcollect.utils.stats;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ----------------------
 * BoxAndWhiskerItem.java
 * ----------------------
 * (C) Copyright 2003-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 27-Aug-2003 : Version 1 (DG);
 * 01-Mar-2004 : Added equals() method and implemented Serializable (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 15-Nov-2006 : Added toString() method override (DG);
 * 02-Oct-2007 : Added new constructor (for convenience) (DG);
 *
 */

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


/**
 * Represents one data item within a box-and-whisker dataset.  Instances of
 * this class are immutable.
 */
public class BoxAndWhiskerItem implements Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7329649623148167423L;

    /** The mean. */
    private Number mean;

    /** The median. */
    private Number median;

    /** The first quarter. */
    private Number q1;

    /** The third quarter. */
    private Number q3;

    /** The minimum regular value. */
    private Number minRegularValue;

    /** The maximum regular value. */
    private Number maxRegularValue;

    /** The minimum outlier. */
    private Number minOutlier;

    /** The maximum outlier. */
    private Number maxOutlier;

    /** The outliers. */
    private List outliers;

    /**
     * Creates a new box-and-whisker item.
     *
     * @param mean  the mean (<code>null</code> permitted).
     * @param median  the median (<code>null</code> permitted).
     * @param q1  the first quartile (<code>null</code> permitted).
     * @param q3  the third quartile (<code>null</code> permitted).
     * @param minRegularValue  the minimum regular value (<code>null</code>
     *                         permitted).
     * @param maxRegularValue  the maximum regular value (<code>null</code>
     *                         permitted).
     * @param minOutlier  the minimum outlier (<code>null</code> permitted).
     * @param maxOutlier  the maximum outlier (<code>null</code> permitted).
     * @param outliers  the outliers (<code>null</code> permitted).
     */
    public BoxAndWhiskerItem(Number mean,
                             Number median,
                             Number q1,
                             Number q3,
                             Number minRegularValue,
                             Number maxRegularValue,
                             Number minOutlier,
                             Number maxOutlier,
                             List outliers) {

        this.mean = mean;
        this.median = median;
        this.q1 = q1;
        this.q3 = q3;
        this.minRegularValue = minRegularValue;
        this.maxRegularValue = maxRegularValue;
        this.minOutlier = minOutlier;
        this.maxOutlier = maxOutlier;
        this.outliers = outliers;

    }

    /**
     * Creates a new box-and-whisker item.
     *
     * @param mean  the mean.
     * @param median  the median
     * @param q1  the first quartile.
     * @param q3  the third quartile.
     * @param minRegularValue  the minimum regular value.
     * @param maxRegularValue  the maximum regular value.
     * @param minOutlier  the minimum outlier value.
     * @param maxOutlier  the maximum outlier value.
     * @param outliers  a list of the outliers.
     *
     * @since 1.0.7
     */
    public BoxAndWhiskerItem(double mean, double median, double q1, double q3,
            double minRegularValue, double maxRegularValue, double minOutlier,
            double maxOutlier, List outliers) {

        // pass values to other constructor
        this(new Double(mean), new Double(median), new Double(q1),
                new Double(q3), new Double(minRegularValue),
                new Double(maxRegularValue), new Double(minOutlier),
                new Double(maxOutlier), outliers);

    }

    /**
     * Returns the mean.
     *
     * @return The mean (possibly <code>null</code>).
     */
    public Number getMean() {
        return this.mean;
    }

    /**
     * Returns the median.
     *
     * @return The median (possibly <code>null</code>).
     */
    public Number getMedian() {
        return this.median;
    }

    /**
     * Returns the first quartile.
     *
     * @return The first quartile (possibly <code>null</code>).
     */
    public Number getQ1() {
        return this.q1;
    }

    /**
     * Returns the third quartile.
     *
     * @return The third quartile (possibly <code>null</code>).
     */
    public Number getQ3() {
        return this.q3;
    }

    /**
     * Returns the minimum regular value.
     *
     * @return The minimum regular value (possibly <code>null</code>).
     */
    public Number getMinRegularValue() {
        return this.minRegularValue;
    }

    /**
     * Returns the maximum regular value.
     *
     * @return The maximum regular value (possibly <code>null</code>).
     */
    public Number getMaxRegularValue() {
        return this.maxRegularValue;
    }

    /**
     * Returns the minimum outlier.
     *
     * @return The minimum outlier (possibly <code>null</code>).
     */
    public Number getMinOutlier() {
        return this.minOutlier;
    }

    /**
     * Returns the maximum outlier.
     *
     * @return The maximum outlier (possibly <code>null</code>).
     */
    public Number getMaxOutlier() {
        return this.maxOutlier;
    }

    /**
     * Returns a list of outliers.
     *
     * @return A list of outliers (possibly <code>null</code>).
     */
    public List getOutliers() {
        if (this.outliers == null) {
            return null;
        }
        return Collections.unmodifiableList(this.outliers);
    }

    /**
     * Returns a string representation of this instance, primarily for
     * debugging purposes.
     *
     * @return A string representation of this instance.
     */
    public String toString() {
        return super.toString() + "[mean=" + this.mean + ",median="
                + this.median + ",q1=" + this.q1 + ",q3=" + this.q3 + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BoxAndWhiskerItem other = (BoxAndWhiskerItem) obj;
        if (this.mean != other.mean && (this.mean == null || !this.mean.equals(other.mean))) {
            return false;
        }
        if (this.median != other.median && (this.median == null || !this.median.equals(other.median))) {
            return false;
        }
        if (this.q1 != other.q1 && (this.q1 == null || !this.q1.equals(other.q1))) {
            return false;
        }
        if (this.q3 != other.q3 && (this.q3 == null || !this.q3.equals(other.q3))) {
            return false;
        }
        if (this.minRegularValue != other.minRegularValue && (this.minRegularValue == null || !this.minRegularValue.equals(other.minRegularValue))) {
            return false;
        }
        if (this.maxRegularValue != other.maxRegularValue && (this.maxRegularValue == null || !this.maxRegularValue.equals(other.maxRegularValue))) {
            return false;
        }
        if (this.minOutlier != other.minOutlier && (this.minOutlier == null || !this.minOutlier.equals(other.minOutlier))) {
            return false;
        }
        if (this.maxOutlier != other.maxOutlier && (this.maxOutlier == null || !this.maxOutlier.equals(other.maxOutlier))) {
            return false;
        }
        if (this.outliers != other.outliers && (this.outliers == null || !this.outliers.equals(other.outliers))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.mean != null ? this.mean.hashCode() : 0);
        hash = 29 * hash + (this.median != null ? this.median.hashCode() : 0);
        hash = 29 * hash + (this.q1 != null ? this.q1.hashCode() : 0);
        hash = 29 * hash + (this.q3 != null ? this.q3.hashCode() : 0);
        hash = 29 * hash + (this.minRegularValue != null ? this.minRegularValue.hashCode() : 0);
        hash = 29 * hash + (this.maxRegularValue != null ? this.maxRegularValue.hashCode() : 0);
        hash = 29 * hash + (this.minOutlier != null ? this.minOutlier.hashCode() : 0);
        hash = 29 * hash + (this.maxOutlier != null ? this.maxOutlier.hashCode() : 0);
        hash = 29 * hash + (this.outliers != null ? this.outliers.hashCode() : 0);
        return hash;
    }
}
