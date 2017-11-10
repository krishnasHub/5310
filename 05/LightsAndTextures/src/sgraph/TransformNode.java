package sgraph;

import org.joml.Matrix4f;
import util.BoundingBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This node represents a transformation in the scene graph. It has only one child. The transformation
 * can be viewed as changing from its child's coordinate system to its parent's coordinate system
 * This also stores an animation transform that can be tweaked at runtime
 * @author Amit Shesh
 */
public class TransformNode extends AbstractNode
{
    /**
     * Matrices storing the static and animation transformations separately, so that they can be
     * changed separately
     */
    protected Matrix4f transform, animation_transform;

    private List<Animator> animations;

    /**
     * A reference to its only child
     */
    INode child;

    public TransformNode(IScenegraph graph, String name)
    {
        super(graph,name);
        this.transform = new Matrix4f();
        animation_transform = new Matrix4f();
        animations = new ArrayList<>();
        child = null;
    }

    /**
     * Creates a deep copy of the subtree rooted at this node
     * @return a deep copy of the subtree rooted at this node
     */
    @Override
    public INode clone()
    {
        INode newchild;

        if (child!=null)
        {
            newchild = child.clone();
        }
        else
        {
            newchild = null;
        }

        TransformNode newtransform = new TransformNode(scenegraph,name);
        newtransform.setTransform(new Matrix4f(this.transform));
        newtransform.setAnimationTransform(new Matrix4f(animation_transform));

        if (newchild!=null)
        {
            try
            {
                newtransform.addChild(newchild);
            }
            catch (IllegalArgumentException e)
            {

            }
        }
        return newtransform;
    }

    /**
     * Determines if this node has the specified name and returns itself if so. Otherwise it recurses
     * into its only child
     * @param name name of node to be searched
     * @return
     */
    public INode getNode(String name)
    {
        INode n = super.getNode(name);
        if (n!=null)
        return n;

        if (child!=null)
        {
            return child.getNode(name);
        }

        return null;
    }

    /**
     * Since this node can have a child, it override this method and adds the child to itself
     * This will overwrite any children set for this node previously.
     * @param child the child of this node
     * @throws IllegalArgumentException this method does not throw this exception
     */
    public void addChild(INode child) throws IllegalArgumentException
    {
        if (this.child!=null)
            throw new IllegalArgumentException("Transform node already has a child");
        this.child = child;
        this.child.setParent(this);
    }

    /**
     * Draws the scene graph rooted at this node
     * After preserving the current top of the modelview stack, this "post-multiplies" its
     * animation transform and then its transform in that order to the top of the model view
     * stack, and then recurses to its child. When the child is drawn, it restores the modelview
     * matrix
     * @param context the generic renderer context {@link sgraph.IScenegraphRenderer}
     * @param modelView the stack of modelview matrices
     */

    @Override
    public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView)
    {
        modelView.push(new Matrix4f(modelView.peek()));
        modelView.peek().mul(transform)
                        .mul(animation_transform);
        if (child!=null)
            child.draw(context,modelView);
        modelView.pop();
    }


    public void explodeNode() {
        // Do nothing..
        if(this.showExploded)
            child.setShowExploded(true);
        if(child != null)
            child.explodeNode();
    }

    public void calculateBoundingBox() {
        if(child == null)
            return;

        System.out.println("Calculating bb for Transform");

        // First calculate the boundingBoxes for all children.
        child.calculateBoundingBox();


        // Get the child's bb
        BoundingBox bb = new BoundingBox(child.getBoundingBox());

        // Transform the child's bb
        Matrix4f currTransform = new Matrix4f()
                .mul(transform)
                .mul(animation_transform);

        currTransform.transform(bb.getMinBounds());
        currTransform.transform(bb.getMaxBounds());

        // Recalculate my own bb based on the transformed bb.
        reCalculateBoundingBox(bb);

        System.out.println("Done calculating bb for Transform");
    }

    @Override
    public void animate(int time) {
        Matrix4f animationToApply = new Matrix4f();

        for(int i = 0; i < animations.size(); ++i) {
            animationToApply = animationToApply.mul((animations.get(i).animate(time)));
        }

        this.setAnimationTransform(animationToApply);

        if(this.child != null)
            this.child.animate(time);
    }


    /**
     * Sets the animation transform of this node
     * @param mat the animation transform of this node
     */
    public void setAnimationTransform(Matrix4f mat)
    {
        animation_transform = new Matrix4f(mat);
    }

    public void addAnimatiors(String animators) {
        String[] animatorNames = animators.split("\\+");

        for(int i = 0; i < animatorNames.length; ++i) {
            try {
                String className = "sgraph." + animatorNames[i].trim();
                Class c = Class.forName(className);
                Animator anim = (Animator) c.newInstance();
                this.animations.add(anim);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Gets the transform at this node (not the animation transform)
     * @return
     */
    public Matrix4f getTransform()
    {
        return transform;
    }

    /**
     * Sets the transformation of this node
     * @param t
     * @throws IllegalArgumentException
     */
    @Override
    public void setTransform(Matrix4f t)throws IllegalArgumentException
    {
        this.transform = new Matrix4f(t);
    }

    /**
     * Gets the animation transform of this node
     * @return
     */
    Matrix4f getAnimationTransform()
    {
        return animation_transform;
    }

    /**
     * Sets the scene graph object of which this node is a part, and then recurses to its child
     * @param graph a reference to the scenegraph object of which this tree is a part
     */
    @Override
    public void setScenegraph(IScenegraph graph)
    {
        super.setScenegraph(graph);
        if (child!=null)
        {
            child.setScenegraph(graph);
        }
    }
}
