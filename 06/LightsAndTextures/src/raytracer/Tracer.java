package raytracer;

import com.jogamp.openal.sound3d.Vec3f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Light;
import util.Material;

import java.util.*;
import java.awt.Color;

/**
 * Created by krish on 11/17/2017.
 */
public abstract class Tracer {

    public static float maxValue = Float.MIN_VALUE;

    public Color getColor1(final Ray r) {
        Vector3f p1, p2, p3;
        Vector3f[] arr = new Vector3f[3];

        /*
        // Right plane
        p1 = new Vector3f(0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(0.5f, -0.5f, 0.5f);
        p3 = new Vector3f(0.5f, 0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        p1 = new Vector3f(0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(0.5f, 0.5f, -0.5f);
        p3 = new Vector3f(0.5f, 0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        // Left plane
        p1 = new Vector3f(-0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(-0.5f, -0.5f, 0.5f);
        p3 = new Vector3f(-0.5f, 0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        p1 = new Vector3f(-0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(-0.5f, 0.5f, -0.5f);
        p3 = new Vector3f(-0.5f, 0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;


        // Top Plane
        p1 = new Vector3f(0.5f, 0.5f, -0.5f);
        p2 = new Vector3f(0.5f, 0.5f, 0.5f);
        p3 = new Vector3f(-0.5f, 0.5f, -0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        p1 = new Vector3f(-0.5f, 0.5f, -0.5f);
        p2 = new Vector3f(0.5f, 0.5f, 0.5f);
        p3 = new Vector3f(-0.5f, 0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        // Bottom Plane
        p1 = new Vector3f(0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(0.5f, -0.5f, 0.5f);
        p3 = new Vector3f(-0.5f, -0.5f, -0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        p1 = new Vector3f(-0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(0.5f, -0.5f, 0.5f);
        p3 = new Vector3f(-0.5f, -0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;
        */

        // Front Plane
        p1 = new Vector3f(0.5f, -0.5f, 0.5f);
        p2 = new Vector3f(0.5f, 0.5f, 0.5f);
        p3 = new Vector3f(-0.5f, 0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        p1 = new Vector3f(0.5f, -0.5f, 0.5f);
        p2 = new Vector3f(-0.5f, 0.5f, 0.5f);
        p3 = new Vector3f(-0.5f, -0.5f, 0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;


        /*
        // Back Plane
        p1 = new Vector3f(0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(0.5f, 0.5f, -0.5f);
        p3 = new Vector3f(-0.5f, 0.5f, -0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;

        p1 = new Vector3f(0.5f, -0.5f, -0.5f);
        p2 = new Vector3f(-0.5f, 0.5f, -0.5f);
        p3 = new Vector3f(-0.5f, -0.5f, -0.5f);

        arr[0] = p1;
        arr[1] = p2;
        arr[2] = p3;

        if(rayCollides(arr, r))
            return Color.GREEN;
        */

        return Color.BLACK;
    }

    public Vector4f getPositionForRay(Ray r) {
        return new Vector4f(new Vector4f(r.start).add(new Vector4f(r.direction).mul(r.t)));
    }

    public abstract Vector4f getNormalForRay(Ray r);

    public abstract void intersectThisRay(Ray ray);

    private static boolean once = false;

    public Color getLightColorAt(Material material, Ray r, List<Light> lights, Vector4f normal, Vector4f position) {
        Vector3f ambient;
        Vector3f diffuse;
        Vector3f specular;
        Color fColor = Color.BLACK;
        final Vector4f fPosition = new Vector4f(position);
        final Vector4f fNormal = new Vector4f(normal);

        Vector3f lightVec;
        Vector3f normalView;
        Vector3f viewVec;
        Vector3f reflectVec;
        float nDotL;
        float rDotV;
        float dDotmL;
        //float spotLightAmount;

        for (int i = 0; i < lights.size(); i++) {
            Light light = new Light(lights.get(i));

            if(!once) {
                System.out.println(light.getPosition().x + " " + light.getPosition().y + " " + light.getPosition().z);
                once = !once;
            }

            Vector4f temp;
            if (light.getPosition().w!=0)
                 temp = (light.getPosition().sub(fPosition));
            else
                temp = (light.getPosition().mul(-1f));
            lightVec = new Vector3f(temp.x, temp.y, temp.z).normalize();

            Vector3f tNormal = new Vector3f(fNormal.x, fNormal.y, fNormal.z);
            tNormal = tNormal.normalize();

            nDotL = tNormal.dot(lightVec);

            viewVec = (new Vector3f(fPosition.x, fPosition.y, fPosition.z).mul(-1)).normalize();

            reflectVec = (new Vector3f(lightVec).mul(-1)).reflect(tNormal);
            reflectVec = reflectVec.normalize();

            rDotV = Math.max(reflectVec.dot(viewVec), 0.0f);

            if(rDotV > maxValue)
                maxValue = rDotV;

            dDotmL = new Vector3f(light.getSpotDirection().x, light.getSpotDirection().y, light.getSpotDirection().z)
                    .dot(new Vector3f(lightVec).mul(-1f));

            boolean inCone = (dDotmL >= Math.cos((float) Math.toRadians(light.getSpotCutoff())));

            ambient = new Vector3f(material.getAmbient().x, material.getAmbient().y, material.getAmbient().z)
                    .mul(new Vector3f(light.getAmbient().x, light.getAmbient().y, light.getAmbient().z));

            diffuse = new Vector3f(material.getDiffuse().x, material.getDiffuse().y, material.getDiffuse().z)
                    .mul(new Vector3f(light.getDiffuse().x, light.getDiffuse().y, light.getDiffuse().z))
                    .mul(Math.max(nDotL, 0));

            if (nDotL>0) {
                specular = new Vector3f(material.getSpecular().x, material.getSpecular().y, material.getSpecular().z)
                        .mul(new Vector3f(light.getSpecular().x, light.getSpecular().y, light.getSpecular().z))
                        .mul((float) Math.pow(rDotV, material.getShininess()))
                ;
            } else
                specular = new Vector3f(0, 0, 0);

            if(inCone) {
                fColor = addColor(fColor, (((new Vector3f(ambient)).add(diffuse)).add(specular)));
            }
        }
        //fColor = fColor * texture(image,fTexCoord.st);


        return fColor;
    }

    protected Color addColor(Color c, Vector3f v) {
        Color ret;

        float[] colors = c.getRGBComponents(null);

        ret = new Color(
                clamp(colors[0] + v.x, 1),
                clamp(colors[1] + v.y, 1),
                clamp(colors[2] + v.z, 1), 1);

        return ret;
    }

    protected float clamp(float a, float max) {
        if(a > max)
            return max;
        return a;
    }

    //public abstract Color getColor(final Ray r, Material material, List<Light> l);

    private static final float kEpsilon = 0.01f;

    public boolean rayCollides(Vector3f[] positions, Ray r) {
// compute plane's normal
        Vector3f v0v1 = positions[1].sub(positions[0]);
        Vector3f v0v2 = positions[2].sub(positions[0]);
        // no need to normalize
        Vector3f N = v0v1.cross(v0v2); // N
        float denom = N.dot(N);

        // Step 1: finding P

        // check if ray and plane are parallel ?
        float NdotRayDirection = N.dot(new Vector3f(r.direction.x, r.direction.y, r.direction.z));
        if (Math.abs(NdotRayDirection) < kEpsilon) // almost 0
            return false; // they are parallel so they don't intersect !

        // compute d parameter using equation 2
        float d = N.dot(positions[0]);

        // compute t (equation 3)
        r.t = (N.dot(new Vector3f(r.start.x, r.start.y, r.start.z)) + d) / NdotRayDirection;
        // check if the triangle is in behind the ray
        if (r.t < 0) return false; // the triangle is behind

        // compute the intersection point using equation 1
        Vector4f P4 = r.start.add(r.direction.mul(r.t));
        Vector3f P = new Vector3f(P4.x, P4.y, P4.z);

        // Step 2: inside-outside test
        Vector3f C; // vector perpendicular to triangle's plane

        // edge 0
        Vector3f edge0 = positions[1].sub(positions[0]);
        Vector3f vp0 = P.sub(positions[0]);
        C = edge0.cross(vp0);
        if (N.dot(C) < 0) return false; // P is on the right side

        // edge 1
        Vector3f edge1 = positions[2].sub(positions[1]);
        Vector3f vp1 = P.sub(positions[1]);
        C = edge1.cross(vp1);
        float u;
        if ((u = N.dot(C)) < 0)  return false; // P is on the right side

        // edge 2
        Vector3f edge2 = positions[0].sub(positions[2]);
        Vector3f vp2 = P.sub(positions[2]);
        C = edge2.cross(vp2);
        float v;
        if ((v = N.dot(C)) < 0) return false; // P is on the right side;

        u /= denom;
        v /= denom;

        return true; // this ray hits the triangle
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
