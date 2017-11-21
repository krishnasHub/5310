package raytracer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Light;
import util.Material;

import java.awt.*;
import java.util.*;

/**
 * Created by krish on 11/19/2017.
 */
public class BoxTracer extends Tracer {

    @Override
    public Vector3f getNormalForRay(Ray r) {
        Vector4f point = new Vector4f(r.start).add(new Vector4f(r.direction).mul(r.t)).normalize();

        // Right and left plane
        if(point.x == 0.5f) {
            return new Vector3f(1, 0, 0);
        } else if(point.x == -0.5f) {
            return new Vector3f(-1, 0, 0);
        } else if(point.y == 0.5f) {
            return new Vector3f(0, 1, 0);
        } else if(point.y == -0.5f) {
            return new Vector3f(0, -1, 0);
        } else if(point.z == 0.5f) {
            return new Vector3f(0, 0, 1);
        } else if(point.z == -0.5f) {
            return new Vector3f(0, 0, -1);
        }

        return new Vector3f(point.x, point.y, point.z);
    }

    @Override
    public Color getColor(final Ray r, Material material, java.util.List<Light> lights) {
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

        Color l = getLightColorAt(material, r, lights);

        return l;
    }
}
