package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.BoundingBox;
import util.Light;

import java.util.List;

/**
 * This abstract class implements the {@link sgraph.INode} interface. It provides default methods
 * for many of the methods, especially the ones that could throw an exception
 * Child classes that do not want these exceptions throws should override these methods
 * @author Amit Shesh
 */
public abstract class AbstractNode implements INode
{
    /**
     * The name given to this node
     */
    protected String name;
    /**
     * The parent of this node. Each node except the root has a parent. The root's parent is null
     */
    protected INode parent;
    /**
     * A reference to the {@link sgraph.IScenegraph} object that this is part of
     */
    protected IScenegraph scenegraph;

    protected BoundingBox boundingBox;

    protected boolean showExploded;

    public AbstractNode(IScenegraph graph, String name)
    {
        this.parent = null;
        scenegraph = graph;
        setName(name);
        this.boundingBox = new BoundingBox();
    }

    /**
     * By default, this method checks only itself. Nodes that have children should override this
     * method and navigate to children to find the one with the correct name
     * @param name name of node to be searched
     * @return the node whose name this is, null otherwise
     */
    public INode getNode(String name)
    {
        if (this.name.equals(name))
        return this;

        return null;
    }

    /**
     * Sets the parent of this node
     * @param parent the node that is to be the parent of this node
     */

    public void setParent(INode parent)
    {
        this.parent = parent;
    }

    /**
     * Sets the scene graph object whose part this node is and then adds itself
     * to the scenegraph (in case the scene graph ever needs to directly access this node)
     * @param graph a reference to the scenegraph object of which this tree is a part
     */
    public void setScenegraph(IScenegraph graph)
    {
        this.scenegraph = graph;
        graph.addNode(this.name,this);
    }

    /**
     * Sets the name of this node
     * @param name the name of this node
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the name of this node
     * @return the name of this node
     */
    public String getName() { return name;}


    public abstract INode clone();

    /**
     * By default, throws an exception. Any nodes that can have children should override this
     * method
     * @param child
     * @throws IllegalArgumentException
     */
    @Override
    public void addChild(INode child) throws IllegalArgumentException
    {
        throw new IllegalArgumentException("Not a composite node");
    }

    /**
     * By default, throws an exception. Any nodes that are capable of storing transformations
     * should override this method
     * @param t
     */

    @Override
    public void setTransform(Matrix4f t)
    {
        throw new IllegalArgumentException(getName()+" is not a transform node");
    }


    /**
     * By default, throws an exception. Any nodes that are capable of storing transformations
     * should override this method
     * @param t
     */

    @Override
    public void setAnimationTransform(Matrix4f t)
    {
        throw new IllegalArgumentException(getName()+" is not a transform node");
    }

    public void animate(int time) {
        // Nothing to be done in this node. It's only applicable in the Transform node.
    }

    /**
     * By default, throws an exception. Any nodes that are capable of storing material should
     * override this method
     * @param m the material object to be associated with this node
     */
    @Override
    public void setMaterial(util.Material m)
    {
        throw new IllegalArgumentException(getName()+" is not a leaf node");
    }

    @Override
    public void setTextureName(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Textures not supported yet!");
    }

    /**
     * Adds a new light to this node.
     * @param l
     */
    public void addLight(Light l) {
        throw new UnsupportedOperationException("Lights not supported yet!");
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }


    public void reCalculateBoundingBox(BoundingBox box) {
        // Re-calculate the bounds for MinBounds.
        if(box.getMinBounds().x < this.boundingBox.getMinBounds().x) {
            this.boundingBox.getMinBounds().x = box.getMinBounds().x;
        }

        if(box.getMinBounds().y < this.boundingBox.getMinBounds().y) {
            this.boundingBox.getMinBounds().y = box.getMinBounds().y;
        }

        if(box.getMinBounds().z < this.boundingBox.getMinBounds().z) {
            this.boundingBox.getMinBounds().z = box.getMinBounds().z;
        }


        // Re-calculate the bounds for MaxBounds.
        if(box.getMaxBounds().x > this.boundingBox.getMaxBounds().x) {
            this.boundingBox.getMaxBounds().x = box.getMaxBounds().x;
        }

        if(box.getMaxBounds().y > this.boundingBox.getMaxBounds().y) {
            this.boundingBox.getMaxBounds().y = box.getMaxBounds().y;
        }

        if(box.getMaxBounds().z > this.boundingBox.getMaxBounds().z) {
            this.boundingBox.getMaxBounds().z = box.getMaxBounds().z;
        }
    }

    public void reCalculateBoundingBox(INode node) {
        reCalculateBoundingBox(node.getBoundingBox());
    }

    public boolean isShowExploded() {
        return showExploded;
    }

    public void setShowExploded(boolean showExploded) {
        this.showExploded = showExploded;
    }
}
