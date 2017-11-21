package sgraph;

import org.joml.Matrix4f;
import raytracer.Ray;
import util.IVertexData;
import util.Light;
import util.PolygonMesh;

import java.awt.*;
import java.util.*;

/**
 * A specific implementation of this scene graph. This implementation is still independent
 * of the rendering technology (i.e. OpenGL)
 * @author Amit Shesh
 */
public class Scenegraph<VertexType extends IVertexData> implements IScenegraph<VertexType>
{
    /**
     * The root of the scene graph tree
     */
    protected INode root;

    /**
     * A map to store the (name,mesh) pairs. A map is chosen for efficient search
     */
    protected Map<String,PolygonMesh<VertexType>> meshes;

    /**
     * A map to store the (name,node) pairs. A map is chosen for efficient search
     */
    protected Map<String, INode> nodes;

    protected Map<String,String> textures;

    /**
     * The associated renderer for this scene graph. This must be set before attempting to
     * render the scene graph
     */
    protected IScenegraphRenderer renderer;

    protected java.util.List<util.Light> lightList;


    public Scenegraph()
    {
        root = null;
        meshes = new HashMap<String,PolygonMesh<VertexType>>();
        nodes = new HashMap<String, INode>();
        textures = new HashMap<String,String>();
        lightList = new ArrayList<>(10);
    }

    public void dispose()
    {
        renderer.dispose();
    }

    /**
     * Sets the renderer, and then adds all the meshes to the renderer.
     * This function must be called when the scene graph is complete, otherwise not all of its
     * meshes will be known to the renderer
     * @param renderer The {@link IScenegraphRenderer} object that will act as its renderer
     * @throws Exception
     */
    @Override
    public void setRenderer(IScenegraphRenderer renderer) throws Exception {
        this.renderer = renderer;

        //now add all the meshes
        for (String meshName:meshes.keySet())
        {
            this.renderer.addMesh(meshName,meshes.get(meshName));
        }

    }


    /**
     * Set the root of the scenegraph, and then pass a reference to this scene graph object
     * to all its node. This will enable any node to call functions of its associated scene graph
     * @param root
     */

    @Override
    public void makeScenegraph(INode root)
    {
        this.root = root;
        this.root.setScenegraph(this);

    }

    /**
     * Draw this scene graph. It delegates this operation to the renderer
     * @param modelView
     */
    @Override
    public void draw(Stack<Matrix4f> modelView) {
        if ((root!=null) && (renderer!=null))
        {
            renderer.draw(root,modelView);
        }
    }


    @Override
    public void addPolygonMesh(String name, util.PolygonMesh<VertexType> mesh)
    {
        meshes.put(name,mesh);
    }




    @Override
    public void animate(float time) {
        // Look for <animationType> tags in the scenegraph where animators are added as simple snippets.
        if ((root!=null) && (renderer!=null)) {
            root.animate((int) time);
        }
    }

    @Override
    public void addNode(String name, INode node) {
        nodes.put(name,node);
    }


    @Override
    public INode getRoot() {
        return root;
    }

    @Override
    public Map<String, PolygonMesh<VertexType>> getPolygonMeshes() {
       Map<String,PolygonMesh<VertexType>> meshes = new HashMap<String, PolygonMesh<VertexType>>(this.meshes);
        return meshes;
    }

    @Override
    public Map<String, INode> getNodes() {
        Map<String, INode> nodes = new TreeMap<String, INode>();
        nodes.putAll(this.nodes);
        return nodes;
    }

    @Override
    public void addTexture(String name, String path) {
        textures.put(name,path);
    }


    @Override
    public Color getColorForRay(final Ray ray) {
        Stack<Matrix4f> modelView = new Stack<>();
        modelView.push(new Matrix4f());

        return root.getColorForRay(ray, modelView);
    }

    @Override
    public void storeLight(Light light) {
        if(lightList == null)
            lightList = new ArrayList<>();

        lightList.add(light);
    }

    @Override
    public java.util.List<Light> getLights() {
        return lightList;
    }
}
