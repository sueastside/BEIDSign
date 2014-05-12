
package be.redtree.beidsign.evaluators;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;

public class BeidSignSignatureTypeEvaluator extends DefaultSubComponentEvaluator{

	private final String SIG_TYPE_PDF = "pdf";
	private final String SIG_TYPE_PARAM = "sigType";
	
	/**
     * Decides if this is a signable doc or not
     *
     * @param context
     * @param params
     * @return true if this is a signable pdf document.
     */
    @Override
    public boolean evaluate(RequestContext context, Map<String, String> params)
    {
    	String type = context.getParameter(SIG_TYPE_PARAM);
    	
    	if(type != null && type.equals(SIG_TYPE_PDF))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
}
