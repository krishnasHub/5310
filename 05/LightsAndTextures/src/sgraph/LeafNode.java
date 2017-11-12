package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.Light;
import util.Material;
import util.P;
import util.PolygonMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
        newclone.setMaterial(new Material(this.getMaterial()));
        return newclone;
    }

    public List<Vector4f> getAllVertices() {
        return ((PolygonMesh)scenegraph.getPolygonMeshes().get(objInstanceName)).getAllPositionVertices();
    }

    public void calculateBoundingBox() {
        // Do nothing for a Leaf node. It's already being done in draw().
        // Get the bounding box for this Leaf node.
        P.P("Calculating bb for Leaf");

        this.boundingBox  = ((PolygonMesh)scenegraph.getPolygonMeshes().get(objInstanceName)).getBoundingBox();

        P.P("Done calculating bb for Leaf");
    }


    public void explodeNode() {
        // Do nothing.
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


}
