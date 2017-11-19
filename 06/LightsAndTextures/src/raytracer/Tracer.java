package raytracer;

import org.joml.Vector4f;

import java.util.*;
import java.awt.Color;

/**
 * Created by krish on 11/17/2017.
 */
public class Tracer {

    public Color getColor(final Ray r) {
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

        r.t = Math.min(tmin, tmax);

        return Color.BLUE;
    }

    private static float max(float x, float y, float z) {
        if(x > y) {
            if(x > z)
                return x;
            else
                return z;
        } else {
            if(y > z)
                return y;
            else
                return z;
        }
    }

    private static float min(float x, float y, float z) {
        if(x < y) {
            if(x < z)
                return x;
            else
                return z;
        } else {
            if(y < z)
                return y;
            else
                return z;
        }
    }
}
