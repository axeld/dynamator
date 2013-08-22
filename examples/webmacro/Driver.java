import java.io.PrintWriter;

import org.webmacro.*;

public class Driver
{
    public static void main(String args[])
    throws Exception
    {
        if ( args.length == 0 || ! args[0].endsWith(".wm") )
        {
            System.err.println(
                "Usage: java Driver x.wm");
            return;
        }
        
        WM wm = new WM();
        Context context = new Context(wm.getBroker());
        Template template = wm.getTemplate(args[0]);
        FastWriter writer = wm.getFastWriter(System.out, "UTF8");
        template.write(writer, context);
        writer.close();
    }
}
