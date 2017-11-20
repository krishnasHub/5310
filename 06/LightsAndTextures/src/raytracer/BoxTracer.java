package raytracer;

import org.joml.Vector4f;
import util.Material;

import java.awt.*;

/**
 * Created by krish on 11/19/2017.
 */
public class BoxTracer extends Tracer {

    @Override
    public Color getColor(final Ray r, Material material) {
        Vector4f min = new Vector4f(-0.5f, -0.5f,-0.5f, 0);
        Vector4f max = new Vector4f(0.5f, 0.5f, 0.5f,  0);

        float tmin = (min.x - r.start.x) / r.direction.x;
        float tmax = (max.x - r.start.x) / r.direction.x;

        if (tmin > tmax)  {
            float temp = tmin;
            tmin = tmax;
            tmax = temp;
        }

        float tymin = (min.y - r.start.y) / r.direction.y;
        float tymax = (max.y - r.start.y) / r.direction.y;

        if (tymin > tymax)  {
            float temp = tymin;
            tymin = tymax;
            tymax = temp;
        }

        if ((tmin > tymax) || (tymin > tmax))
            return Color.BLACK;

        if (tymin > tmin)
            tmin = tymin;

        if (tymax < tmax)
            tmax = tymax;

        float tzmin = (min.z - r.start.z) / r.direction.z;
        float tzmax = (max.z - r.start.z) / r.direction.z;

        if (tzmin > tzmax) {
            float temp = tzmin;
            tzmin = tzmax;
            tzmax = temp;
        }

        if ((tmin > tzmax) || (tzmin > tmax))
            return Color.BLACK;

        if (tzmin > tmin)
            tmin = tzmin;

        if (tzmax < tmax)
            tmax = tzmax;

        if(tmin < 0)
            r.t = tmax;
        else if(tmax < 0)
            r.t = tmin;
        else
            r.t = Math.min(tmin, tmax);

        Vector4f ambient = material.getAmbient();

        Color c = new Color(ambient.x, ambient.y, ambient.z);

        return c;
    }
}
