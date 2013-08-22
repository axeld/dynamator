import java.io.PrintWriter;
import java.util.Calendar;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

public class TheTimeDriver
{
    public static void main(String args[])
    throws Exception
    {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put("now", calendar.getTime());
        context.put("hour", new Integer(hour));
        PrintWriter writer = new PrintWriter(System.out, true);
        Velocity.mergeTemplate("TheTime.vm", context, writer);
        writer.flush();
        writer.close();
    }
}
