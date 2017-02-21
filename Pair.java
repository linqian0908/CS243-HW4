package submit;

import java.util.*;

public class Pair<L, R>
{
    public final L left;
    public final R right;
 
    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }
 
    public L getKey()
    {
        return left;
    }
 
    public R getValue()
    {
        return right;
    }
 
    @Override
    public int hashCode()
    {
        return right.hashCode() ^ right.hashCode();
    }
 
    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (!(o instanceof Pair))
        {
            return false;
        }
        Pair pairo = (Pair) o;
        return this.left.equals(pairo.getKey()) &&
                this.right.equals(pairo.getValue());
    }
 
    @Override
    public String toString()
    {
        return left + "," + right;
    }
}
