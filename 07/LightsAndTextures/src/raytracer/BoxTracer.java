package raytracer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Light;
import util.Material;

import java.awt.*;
import java.util.*;

/**
 * This tracer is for the box object (not box-inside/box-outside), so texture coordinates won't
 * work for the die. Will need to write different method for getTextureCoordinatesForPoint if so.
 */
public class BoxTracer extends Tracer {

    public float[] getTextureCoordinatesForPoint(Vector4f point) {
        float[] coords = new float[2];

        if ((point.x > -0.5f) && (point.x < 0.5f)) {
            if (point.z == 0.5f) {
                //Front face
                coords[1] = scaleToRange(point.x, 0.5f, -0.5f);
                coords[0] = scaleToRange(point.y, 0.5f, -0.5f);
            }
            if (point.z == -0.5f) {
                //Back face
                coords[1] = scaleToRange(point.x, 0.5f, -0.5f);
                coords[0] = scaleToRange(point.y, -0.5f, 0.5f);
            }

        }

        //Left Side
        if (point.x == -0.5f) {
            if ((point.z > -0.5f) && (point.z < 0.5f)) {
                coords[1] = scaleToRange(point.z, 0.5f, -0.5f);
                coords[0] = scaleToRange(point.y, 0.5f, -0.5f);
            }
        }

        //Right Side
        if (point.x == 0.5f) {
            if ((point.z > -0.5f) && (point.z < 0.5f)) {
                coords[1] = scaleToRange(point.z, -0.5f, 0.5f);
                coords[0] = scaleToRange(point.y, 0.5f, -0.5f);
            }
        }

        //Top Side
        if (point.y == 0.5f) {
            if ((point.z > -0.5f) && (point.z < 0.5f)) {
                coords[1] = scaleToRange(point.x, 0.5f, -0.5f);
                coords[0] = scaleToRange(point.z, -0.5f, 0.5f);
            }
        }

        //Bottom Side
        if (point.y == -0.5f) {
            if ((point.z > -0.5f) && (point.z < 0.5f)) {
                coords[1] = scaleToRange(point.x, 0.5f, -0.5f);
                coords[0] = scaleToRange(point.z, 0.5f, -0.5f);
            }
        }

        return coords;
    }

    @Override
    public Vector4f getNormalForRay(Ray r) {
        Vector4f point = new Vector4f(r.start).add(new Vector4f(r.direction).mul(r.t));

        // Right and left plane
        if(point.x == 0.5f) {
            return new Vector4f(1, 0, 0, 0);
        } else if(point.x == -0.5f) {
            return new Vector4f(-1, 0, 0, 0);
        } else if(point.y == 0.5f) {
            return new Vector4f(0, 1, 0, 0);
        } else if(point.y == -0.5f) {
            return new Vector4f(0, -1, 0, 0);
        } else if(point.z == 0.5f) {
            return new Vector4f(0, 0, 1, 0);
        } else if(point.z == -0.5f) {
            return new Vector4f(0, 0, -1, 0);
        }

        return new Vector4f(point.x, point.y, point.z, point.w);
    }

    public void intersectThisRay(Ray r) {
        r.t = -1;

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
            return;

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
            return;

        if (tzmin > tmin)
            tmin = tzmin;

        if (tzmax < tmax)
            tmax = tzmax;

        if(tmin < 0 && tmax < 0)
            return;

        if(tmin < 0)
            r.t = tmax;
        else if(tmax < 0)
            r.t = tmin;
        else
            r.t = Math.min(tmin, tmax);
    }
}
