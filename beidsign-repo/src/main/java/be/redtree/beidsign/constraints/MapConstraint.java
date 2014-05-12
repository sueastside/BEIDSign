package be.redtree.beidsign.constraints;


import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.constraint.BaseParameterConstraint;

/**
 *	MapConstraint
 *
 *	@project	countersign
 *	@author		ntmcminn
 *	@link		https://github.com/ntmcminn/CounterSign
 */
public class MapConstraint extends BaseParameterConstraint
{
    private HashMap<String, String> cm = new HashMap<String, String>();

    public MapConstraint()
    {
    }


    public void setConstraintMap(Map<String, String> m)
    {
        cm.putAll(m);
    }


    public Map<String, String> getAllowableValues()
    {
        return cm;
    }


    @Override
    protected Map<String, String> getAllowableValuesImpl()
    {
        return cm;
    }
}
