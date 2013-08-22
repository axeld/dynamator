import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

public class TermsDictionaryDriver
{
    // Velocity doesn't fully support dictionaries/maps:
    // ignores keys
    
    private static final Hashtable dictionary = new Hashtable();
    private static final Vector names = new Vector();

    static
    {
        dictionary.put(
            "marker",
            "An invisible HTML element or attribute that identifies a "
            + "location in an HTML file.  The HTML 4.0 markers are "
            + "div, span, id, and class.");
        dictionary.put(
            "locator",
            "An element in a Dynamator annotations file that identifies "
            + "the set of elements to which a set of overrides will apply.");
        dictionary.put(
            "prolog",
            "The beginning of the generated file, before the template.");
        dictionary.put(
            "epilog",
            "The end of the generated file, after the template.");
        dictionary.put(
            "override",
            "An element in a Dynamator annotations file that specifies "
            + "a set of changes to be applied to the template.  "
            + "(AKA \"modifier\")");
        dictionary.put(
            "template",
            "The file containing the static text pattern to be output "
            + " by the generated program. ");

        Enumeration e = dictionary.keys();
        while ( e.hasMoreElements() )
        {
            names.addElement(e.nextElement());
        }
    }
    
    public static void main(String args[])
    throws Exception
    {
        Velocity.init();
        VelocityContext context = new VelocityContext();
        context.put("dictionary", dictionary);
        context.put("names", names);
        PrintWriter writer = new PrintWriter(System.out, true);
        Velocity.mergeTemplate("TermsDictionary.vm", context, writer);
        writer.flush();
        writer.close();
    }
}
