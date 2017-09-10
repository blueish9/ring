package org.ring.dml.transaction.joint;

/**
 * Created by quanle on 6/3/2017.
 */
public class Joint
{
    Object objFk;
    Object objRc;

    public Joint(Object objFk, Object objRc)
    {
        this.objFk = objFk;
        this.objRc = objRc;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Joint that = (Joint) o;

        if (!objFk.equals(that.objFk))
        {
            return false;
        }
        return objRc.equals(that.objRc);

    }

    @Override
    public int hashCode()
    {
        int result = objFk.hashCode();
        result = 31 * result + objRc.hashCode();
        return result;
    }

    public Object getObjFk()
    {
        return objFk;
    }

    public Object getObjRc()
    {
        return objRc;
    }
}
