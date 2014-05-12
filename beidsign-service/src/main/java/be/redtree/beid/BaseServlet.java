package be.redtree.beid;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public abstract class BaseServlet extends HttpServlet
{
	public static HttpSession getSession() 
	{
	    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
	    return attr.getRequest().getSession(true); // true == allow create
	}
}
