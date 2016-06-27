package eu.europeana.util.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A pair of values bundled together
 * 
 * @param <T1>
 * @param <T2>
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11 de Abr de 2013
 */
public class Pair<T1, T2> implements Comparable<Pair<T1, T2>>, Serializable {
    private static final long serialVersionUID = 1;

    /**
     * comparison type
     * 
     * @author Markus Muhr (markus.muhr@theeuropeanlibrary.org)
     * @since Jan 5, 2015
     */
    public enum Comparison {
        /** Comparison V1 */
        V1,
        /** Comparison V2 */
        V2,
        /** Comparison BOTH */
        BOTH
    }

    private T1         v1;
    private T2         v2;
    private Comparison compareOn = Comparison.V1;

    /**************************************************************************
     *************** Constructors ******************
     *************************************************************************/
    /** Creates a new instance of Tuple */
    public Pair() {
    }

    /**
     * Creates a new instance of Tuple
     * 
     * @param v1
     * @param v2
     */
    public Pair(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    /**
     * Creates a new instance of Tuple
     * 
     * @param v1
     * @param v2
     * @param compareOn
     */
    public Pair(T1 v1, T2 v2, Comparison compareOn) {
        this.v1 = v1;
        this.v2 = v2;
        this.compareOn = compareOn;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof Pair) {
            @SuppressWarnings("rawtypes")
            Pair arg = (Pair)arg0;
            if (compareOn == Comparison.V1) return v1.equals(arg.getV1());
            if (compareOn == Comparison.V2) return v2.equals(arg.getV2());
            return v1.equals(arg.getV1()) && v2.equals(arg.getV2());
        } else if (compareOn == Comparison.V1) {
            return v1.equals(arg0);
        } else if (compareOn == Comparison.V2) { return v2.equals(arg0); }
        throw new RuntimeException("Invalid comparison");
    }

    @Override
    public int hashCode() {
        if (compareOn == Comparison.V1) return v1.hashCode();
        if (compareOn == Comparison.V2) return v2.hashCode();
        return v1.hashCode() + v2.hashCode();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public int compareTo(Pair<T1, T2> other) {
        if (compareOn == Comparison.V1) {
            if (v1 instanceof Comparable) {
                Comparable v1c = (Comparable)v1;
                return v1c.compareTo(other.getV1());
            }
            throw new RuntimeException(v1.getClass().getName() + " is not Comparable");
        } else if (compareOn == Comparison.V2) {
            if (v2 instanceof Comparable) {
                Comparable v2c = (Comparable)v2;
                return v2c.compareTo(other.getV2());
            }
            throw new RuntimeException(v1.getClass().getName() + " is not Comparable");
        }
        throw new RuntimeException("Cannot compare on Comparision.BOTH");
    }

    /**************************************************************************
     ************* Properties Methods ******************
     *************************************************************************/

    /**
     * Getter for property v1.
     * 
     * @return Value of property v1.
     * 
     */
    public T1 getV1() {
        return v1;
    }

    /**
     * Setter for property v1.
     * 
     * @param v1
     *            New value of property v1.
     * 
     */
    public void setV1(T1 v1) {
        this.v1 = v1;
    }

    /**
     * Getter for property v2.
     * 
     * @return Value of property v2.
     * 
     */
    public T2 getV2() {
        return v2;
    }

    /**
     * Setter for property v2.
     * 
     * @param v2
     *            New value of property v2.
     * 
     */
    public void setV2(T2 v2) {
        this.v2 = v2;
    }

    /**
     * toString methode: creates a String representation of the object
     * 
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<");
        buffer.append("v1 = ").append(v1);
        buffer.append(", v2 = ").append(v2);
        buffer.append(">");
        return buffer.toString();
    }

    /**
     * @param <C>
     * @param <V>
     * @param col
     * @return all values
     */
    public static <C, V> List<C> getListOfV1(Collection<Pair<C, V>> col) {
        ArrayList<C> ret = new ArrayList<C>(col.size());
        for (Pair<C, V> t : col) {
            ret.add(t.getV1());
        }
        return ret;
    }

    /**
     * @param <C>
     * @param <V>
     * @param col
     * @return all values
     */
    public static <C, V> List<V> getListOfV2(Collection<Pair<C, V>> col) {
        ArrayList<V> ret = new ArrayList<V>(col.size());
        for (Pair<C, V> t : col) {
            ret.add(t.getV2());
        }
        return ret;
    }
}
