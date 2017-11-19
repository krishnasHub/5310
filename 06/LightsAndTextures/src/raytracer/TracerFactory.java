package raytracer;

/**
 * Created by krish on 11/19/2017.
 */
public class TracerFactory {
    public static Tracer getTracer(String objInstanceName) {

        if(objInstanceName == null || objInstanceName.equals(""))
            return null;

        switch(objInstanceName) {
            case "box":
                return new BoxTracer();

                default:
                    return null;
        }
    }
}
