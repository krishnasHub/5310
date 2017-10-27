package sgraph;

/**
 * Created by krish on 10/25/2017.
 * Something to represent the struct LightProperties
 */
public class LightLocation {
    public int ambient, diffuse, specular, position, spotDirection, spotCutoff;

    public LightLocation() {
        ambient = diffuse = specular = position = spotDirection = spotCutoff = -1;
    }
}