package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import raytracer.Ray;
import util.Light;

import java.awt.*;
import java.util.Stack;

/**
 * Created by krish on 10/25/2017.
 */
public class LightNode extends AbstractNode {

    //public static int TotalLightCount = 0;

    private Light light;

    public LightNode(IScenegraph graph, String name) {
        super(graph,name);
        this.light = new Light();
        //TotalLightCount++;
    }

    int dir = 1;

    @Override
    public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView) {
        Light newLight = new Light(this.light);

        // This is for OpenGL
        context.storeLight(this.light, modelView.peek());


        // This is for the Ray Tracer
        //Vector4f posn = newLight.getPosition();
        //posn = new Matrix4f(modelView.peek()).invert().transform(posn);
        /*
        // normals and refractions..
        posn.x =  -0.25f * posn.x;
        posn.y =  -0.25f * posn.y;
        posn.z =  0.5f * posn.z;
        */
        //newLight.setPosition(posn);
        this.scenegraph.storeLight(newLight);
    }

    @Override
    public INode clone() {
        LightNode ret = new LightNode(scenegraph, name);
        ret.setLight(new Light(light));

        return ret;
    }

    public Light getLight() {
        return light;
    }

    public void setLight(Light light) {
        this.light = light;
    }

    public Color getColorForRay(final Ray ray, Stack<Matrix4f> modelView) {
        return Color.BLACK;
    }
}
