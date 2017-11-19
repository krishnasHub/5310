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
        context.storeLight(this.light, modelView.peek());

        /*
        Vector4f posn = this.light.getPosition();
        posn.x += dir * 1f;

        if(posn.x >= 300 || posn.x <= -300)
            dir *= -1;

        this.light.setPosition(posn);
        */
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
