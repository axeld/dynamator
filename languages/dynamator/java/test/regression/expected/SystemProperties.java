// generated by Dynamator Tue Jan 20 14:44:13 CST 2004

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.http.*;

public class SystemProperties
extends HttpServlet
{
    public 
    void
    doGet(
        HttpServletRequest request,
        HttpServletResponse response
        )
    throws IOException
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
        out.write("<html>\n");
        out.write("  <head>\n");
        out.write("    <title>System Properties</title>\n");
        out.write("  </head>\n");
        out.write("  <body>\n");
        out.write("    <dl id=\"Properties\">\n");
        {
            final java.util.Properties $systemProperties = System.getProperties();
            String property;
            String propertyName;
            final java.util.Enumeration $systemPropertiesNames = $systemProperties.propertyNames();
            while ( $systemPropertiesNames.hasMoreElements() )
            {
                propertyName = (String) $systemPropertiesNames.nextElement();
                property = $systemProperties.getProperty(propertyName);
                out.write("      <dt id=\"PropertyName\">");
                    out.write(String.valueOf(propertyName));
                    out.write("</dt>\n");
                out.write("      <dd id=\"PropertyValue\">");
                    out.write(String.valueOf(property));
                    out.write("</dd>\n");

            }
        }
        out.write("    </dl>\n");
        out.write("  </body>\n");
        out.write("</html>\n");

        out.flush();
        out.close();
    }
}
