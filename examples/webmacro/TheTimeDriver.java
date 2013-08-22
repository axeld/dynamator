import java.io.PrintWriter;
import java.util.Calendar;

import org.webmacro.*;

public class TheTimeDriver
{
    public static void main(String args[])
    throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        WM wm = new WM();
        Context context = new Context(wm.getBroker());
        context.put("now", calendar.getTime());
        context.put("hour", new Integer(hour));

        Template template = wm.getTemplate("TheTime.wm");
        FastWriter writer = wm.getFastWriter(System.out, "UTF8");
        template.write(writer, context);
        writer.close();
    }
}
