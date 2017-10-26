package sgraph;

import org.joml.Matrix4f;
import util.Light;

import java.util.Stack;

/**
 * Created by krish on 10/25/2017.
 */
public class LightNode extends AbstractNode {

    public static int TotalLightCount = 0;

    private Light light;

    public LightNode(IScenegraph graph,String name) {
        super(graph,name);
        this.light = new Light();
        TotalLightCount++;
    }

    @Override
    public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView) {
        context.drawLight(this.light, modelView.peek());
    }

    @Override
    public INode clone() {
        LightNode ret = new LightNode(scenegraph, name);
        ret.setLight(light.clone());

        return ret;
    }

    public Light getLight() {
        return light;
    }

    public void setLight(Light light) {
        this.light = light;
    }
}
