package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.Light;

import java.util.ArrayList;
import java.util.List;
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

    public void calculateBoundingBox() {
        // Do nothing for a light node. Light has no bounding box. So, we don't really care about it.
    }

    public void explodeNode() {
        // Do nothing..
    }

    public List<Vector4f> getAllVertices() {
        List<Vector4f> ret = new ArrayList<>();

        return ret;

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
}
