import java.io.PrintWriter;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

public class Driver
{
    public static void main(String args[])
    throws Exception
    {
        if ( args.length == 0 || ! args[0].endsWith(".vm") )
        {
            System.err.println(
                "Usage: java Driver x.vm");
            return;
        }
        
        Velocity.init();
        VelocityContext context = new VelocityContext();
        PrintWriter writer = new PrintWriter(System.out, true);
        Velocity.mergeTemplate(args[0], context, writer);
        writer.flush();
        writer.close();
    }
}
