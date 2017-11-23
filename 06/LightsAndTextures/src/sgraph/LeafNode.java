package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import raytracer.Ray;
import raytracer.TracerFactory;
import util.Light;
import util.PolygonMesh;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * This node represents the leaf of a scene graph. It is the only type of node that has
 * actual geometry to render.
 * @author Amit Shesh
 */
public class LeafNode extends AbstractNode
{
    /**
     * The name of the object instance that this leaf contains. All object instances are stored
     * in the scene graph itself, so that an instance can be reused in several leaves
     */
    protected String objInstanceName;
    /**
     * The material associated with the object instance at this leaf
     */
    protected util.Material material;

    protected List<Light> lights;
    private int lightCount = -1;

    protected String textureName;

    public LeafNode(String instanceOf, IScenegraph graph, String name)
    {
        super(graph,name);
        this.objInstanceName = instanceOf;
        lights = new ArrayList<Light>();
    }


    public void addLight() {
        this.lights.add(new Light());
        lightCount++;
        //LightNode.TotalLightCount++;
    }

    public Light getTopmostLight() {
        return this.lights.get(lightCount);
    }

    /*
	 *Set the material of each vertex in this object
	 */
    @Override
    public void setMaterial(util.Material mat)
    {
        material = new util.Material(mat);
    }

    /**
     * Set texture ID of the texture to be used for this leaf
     * @param name
     */
    @Override
    public void setTextureName(String name)
    {
        textureName = name;
    }

    /*
     * gets the material
     */
    public util.Material getMaterial()
    {
        return material;
    }

    @Override
    public INode clone()
    {
        LeafNode newclone = new LeafNode(this.objInstanceName,scenegraph,name);
        newclone.setMaterial(this.getMaterial());
        return newclone;
    }


    /**
     * Delegates to the scene graph for rendering. This has two advantages:
     * <ul>
     *     <li>It keeps the leaf light.</li>
     *     <li>It abstracts the actual drawing to the specific implementation of the scene graph renderer</li>
     * </ul>
     * @param context the generic renderer context {@link sgraph.IScenegraphRenderer}
     * @param modelView the stack of modelview matrices
     * @throws IllegalArgumentException
     */
    @Override
    public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView) throws IllegalArgumentException
    {
        if (objInstanceName.length()>0)
        {
            MeshInfo meshInfo = new MeshInfo();
            meshInfo.name = objInstanceName;
            meshInfo.material = material;
            meshInfo.textureName = textureName;
            meshInfo.transformation = modelView.peek();

            //context.drawMesh(objInstanceName, material, textureName, modelView.peek());
            context.storeMeshInfo(meshInfo);
        }

        if(lights.size() > 0) {
            lights.forEach(l -> context.storeLight(l, modelView.peek()));
        }
    }


    @Override
    public Color getColorForRay(final Ray ray, Stack<Matrix4f> modelView) {
        this.tracer = TracerFactory.getTracer(objInstanceName);

        if(tracer == null)
            return Color.BLACK;

        Ray newRay = new Ray(ray);

        new Matrix4f(modelView.peek()).invert().transform(newRay.start);
        new Matrix4f(modelView.peek()).invert().transform(newRay.direction);

        List<Light> l = scenegraph.getLights();

        tracer.intersectThisRay(newRay);
        if(newRay.t == -1)
            return Color.BLACK;

        float intersection = newRay.t;
        ray.t = intersection;
        //newRay = new Ray(ray);
        //newRay.t = intersection;


        //new Matrix4f(modelView.peek()).invert().transform(newRay.start);
        //new Matrix4f(modelView.peek()).invert().transform(newRay.direction);


        Vector4f normal = tracer.getNormalForRay(newRay);
        Vector4f position = tracer.getPositionForRay(newRay);

        Ray ray2 = new Ray(ray);
        ray2.t = newRay.t;

        normal = new Matrix4f(modelView.peek()).transform(normal);
        position = new Matrix4f(modelView.peek()).transform(position);

        Color absorptive = tracer.getLightColorAt(this.getMaterial(), ray2, l, normal, position);
        float abs = this.material.getAbsorption();

        return absorptive;
    }


}
