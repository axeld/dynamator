// generated by Dynamator Tue Jan 20 14:44:12 CST 2004

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.*;

public class DynamicAttribute
extends HttpServlet
{
    private String getGreeting()
    {
        return "Hello World, says dynamator!";
    }

    public
    String
    getTextColor()
    {
        return "#cc00cc";
    }

    public
    String
    getParagraphAlignment()
    {
        return "center";
    }

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
        out.write("    <title>Hello World</title>\n");
        out.write("  </head>\n");
        out.write("  <body>\n");
        out.write("    <p id=\"DynamicText\" align=\"");
            out.write(String.valueOf(getParagraphAlignment()));
            out.write("\"><font id=\"DynamicFont\" color=\"");
            out.write(String.valueOf(getTextColor()));
            out.write("\">");
            out.write(String.valueOf(getGreeting()));
            out.write("</font></p>\n");
        out.write("  </body>\n");
        out.write("</html>\n");

        out.flush();
        out.close();
    }
}