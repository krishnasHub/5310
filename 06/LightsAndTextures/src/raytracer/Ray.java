package raytracer;

import org.joml.Vector4f;

/**
 * Created by krish on 11/17/2017.
 */
public class Ray {

    public Vector4f start;
    public Vector4f direction;
    public float t;

    public Ray(Vector4f start, Vector4f direction) {
        this.start = new Vector4f(start);
        this.direction = new Vector4f(direction);
    }

    public Ray(Ray ray) {
        this.start = new Vector4f(ray.start);
        this.direction = new Vector4f(ray.direction);
        this.t = ray.t;
    }
}
