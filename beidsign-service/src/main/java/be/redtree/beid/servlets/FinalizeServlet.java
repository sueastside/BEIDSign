package be.redtree.beid.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.redtree.beid.BaseServlet;

public class FinalizeServlet extends BaseServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException 
	{
		byte[] pdf = (byte[])this.getSession().getAttribute("beid.pdf");
		
		
		response.setContentType("application/pdf");
		OutputStream os = response.getOutputStream();
		os.write(pdf, 0, pdf.length);
		os.flush();
		os.close();
		
		/*
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>eID Applet Service</title></head>");
		out.println("<body>");
		out.println("<h1>eID Applet Service</h1>");
		out.println("<p>The eID Applet Service should not be accessed directly.</p>");
		out.println(getSession());
		out.println("</body></html>");
		out.close();
		*/
	}
}
