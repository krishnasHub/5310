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

    protected Vector4f getPositionForRay(Ray r) {
        return new Vector4f(new Vector4f(r.start).add(new Vector4f(r.direction).mul(r.t)));
    }

    public abstract Vector4f getNormalForRay(Ray r);

    protected Color getLightColorAt(Material material, Ray r, List<Light> lights) {
        Vector4f ambient = material.getAmbient();
        Vector4f diffuse;
        Vector4f specular;
        Color c = new Color(ambient.x, ambient.y, ambient.z);
        Color fColor = Color.BLACK;
        final Vector4f fPosition = getPositionForRay(r);
        final Vector4f fNormal = getNormalForRay(r);

        Vector4f lightVec;
        Vector4f normalView;
        Vector4f viewVec;
        Vector3f reflectVec;
        float nDotL;
        float rDotV;
        float dDotmL;
        //float spotLightAmount;

        for (int i = 0; i < lights.size(); i++) {
            if (lights.get(i).getPosition().w!=0)
                lightVec = (lights.get(i).getPosition().sub(fPosition)).normalize();
            else
                lightVec = (lights.get(i).getPosition().mul(-1f)).normalize();

            Vector4f tNormal = new Vector4f(fNormal);
            normalView = tNormal.normalize();
            nDotL = normalView.dot(lightVec);

            viewVec = new Vector4f(fPosition).mul(-1);
            viewVec = viewVec.normalize();


            reflectVec = (new Vector3f(lightVec.x, lightVec.y, lightVec.z).mul(-1)).reflect(new Vector3f(normalView.x, normalView.y, normalView.z));
            reflectVec = reflectVec.normalize();


            rDotV = Math.max(reflectVec.dot(new Vector3f(viewVec.x, viewVec.y, viewVec.z)), 0.0f);

            dDotmL = lights.get(i).getSpotDirection().dot(new Vector4f(lightVec).mul(-1f));

            boolean inCone = (dDotmL >= Math.cos(lights.get(i).getSpotCutoff()));

            ambient = new Vector4f(material.getAmbient()).mul(new Vector4f(lights.get(i).getAmbient().x,
                    lights.get(i).getAmbient().y,
                    lights.get(i).getAmbient().z, 0));

            diffuse = new Vector4f(material.getDiffuse())
                    .mul(new Vector4f(
                            lights.get(i).getDiffuse().x,
                            lights.get(i).getDiffuse().y,
                            lights.get(i).getDiffuse().z,
                            0))
                    .mul(Math.max(nDotL, 0));

            if (nDotL>0)
                specular = new Vector4f(material.getSpecular())
                        .mul(new Vector4f(
                                lights.get(i).getSpecular().x,
                                lights.get(i).getSpecular().y,
                                lights.get(i).getSpecular().z,
                                0))
                        .mul((float) Math.pow(rDotV, material.getShininess()));
            else
                specular = new Vector4f();

            if(inCone)
            {
                //spotLightAmount = (dDotmL - cos(light[i].spotCutoff)) / dDotmL;
                //spotLightAmount = 1;
                fColor = addColor(fColor, new Vector4f(ambient).add(diffuse).add(specular));
            }
        }
        //fColor = fColor * texture(image,fTexCoord.st);


        return fColor;
    }

    protected Color addColor(Color c, Vector4f v) {
        Color ret = c;

        float[] colors = c.getRGBComponents(null);

        ret = new Color(colors[0] + v.x, colors[1] + v.y, colors[2] + v.z, 1);

        return ret;
    }

    public abstract Color getColor(final Ray r, Material material, List<Light> l);

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
