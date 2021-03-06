package raytracer;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.List;

import util.Light;
import util.Material;

public class SphereTracer extends Tracer {

  public float[] getTextureCoordinatesForPoint(Vector4f point) {
    float[] coords = new float[2];

    float tetha =  (float) Math.acos(point.y);
    float phi = (float) Math.atan2(point.z, point.x);

    coords[0] = scaleToRange(phi, (float) -Math.PI, (float) Math.PI);
    coords[1] = scaleToRange(tetha, 0.0f, (float) Math.PI);

    return coords;
  }


  @Override
  public Vector4f getNormalForRay(Ray r) {
    Vector4f point = new Vector4f(r.start).add(new Vector4f(r.direction).mul(r.t));
    point.w = 0;
    return point.normalize();
  }

  @Override
  public void intersectThisRay(Ray r) {
    r.t = -1f;
    //TODO: What to do to get actual radius of the sphere if not a unit sphere?
    //ANS: Assume the sphere (and box) are unit ones where you are assuming the size.
    //Otherwise this formula will need to change.
    float radius = 1;

    float A = (float) ((Math.pow(r.direction.x, 2))
            + (Math.pow(r.direction.y, 2))
            + (Math.pow(r.direction.z, 2)));
    float B = (float) 2 *
            //r.start.x minus center of sphere's x. Isn't it always 0 if sphere is at the center?
            //Is there a case where the sphere's center/origin is not at (0,0,0)?
            (r.direction.x * (r.start.x)
                    //same comment for y and z.
                    + r.direction.y * (r.start.y)
                    + r.direction.z * (r.start.z));
    float C = (float) (Math.pow(r.start.x, 2)
            + Math.pow(r.start.y, 2)
            + Math.pow(r.start.z, 2)
            - Math.pow(radius, 2)); //radius squared

    float discriminant = (float) Math.sqrt(Math.pow(B, 2) - 4 * A * C);

    /**
     System.out.println(A);
     System.out.println(B);
     System.out.println(C);
     System.out.println(discriminant);
     **/

    //If discriminant is negative, square root is imaginary and line and sphere don't exist.
    //TODO: Double check that when 2*A == 0, return black or something else.
    if ((discriminant <= 0)
            || (Double.isNaN(discriminant))
            || (A == 0)) {
      return;
    }

    //Otherwise there is an intersection, take the smallest of the two (ray enters and exits sphere)
    float t1 = (-B + discriminant)
            / (2 * A);
    float t2 = (-B - discriminant)
            / (2 * A);
    float tClosest;

    //Case where the ray grazes the sphere at exactly one point.
    if (t1 == t2) {
      tClosest = t1;
    } else {
      tClosest = Math.min(t1, t2);
    }

    // Only want positive values of t.
    if (tClosest > 0) {
      r.t = tClosest;
    } else {
      return;
    }

    /**
     System.out.println(tClosest);
     System.out.println(t1);
     System.out.println(t2);
     */
    //Color l = getLightColorAt(material, r, lights);
    // If it reached the end here, the ray hit the object, return a color for the object.
    //Color l = Color.YELLOW;
  }

}
