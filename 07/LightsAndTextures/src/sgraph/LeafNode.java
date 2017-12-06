package sgraph;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import raytracer.Ray;
import raytracer.RayPurpose;
import raytracer.TracerFactory;
import util.Light;
import util.PolygonMesh;
import util.TextureImage;

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

    public void collectAllLights(Stack<Matrix4f> modelView) {
        // Do nothing..
    }


    private int inShadow(Vector4f position) {
        List<Light> l = scenegraph.getLights();
        Ray shadowRay;
        Vector4f start, direction;
        int ret = 0;

        for(int i = 0; i < l.size(); ++i) {
            Light light = new Light(l.get(i));

            direction = new Vector4f(position).sub(new Vector4f(light.getPosition()));
            direction.w = 0;
            direction = direction.normalize();
            direction = direction.mul(-1);

            start = new Vector4f(position);

            shadowRay = new Ray(start, direction);
            shadowRay.purpose = RayPurpose.RAY_SHADOW;
            shadowRay.originators.add(this);

            Color shadowColor = scenegraph.getColorForRay(shadowRay);
            if(shadowColor != Color.BLACK)
                ret++;
        }


        return ret;
    }


    private Color getReflectionColor(Vector4f position, Vector4f normal, Ray oldRay) {
        Ray reflectiveRay;

        Vector4f start = new Vector4f(position);

        Vector3f normalizedPosition = new Vector3f(position.x, position.y, position.z).normalize();
        Vector3f normalizedNormal = new Vector3f(normal.x, normal.y, normal.z).normalize();
        Vector3f reflectedVector = new Vector3f(normalizedPosition)
                .reflect(new Vector3f(normalizedNormal) );
        reflectedVector = reflectedVector.normalize();

        Vector4f direction = new Vector4f(reflectedVector.x, reflectedVector.y, reflectedVector.z, 0);
        direction = direction.normalize();

        start = start.add(new Vector4f(direction).mul(1.1f));

        reflectiveRay = new Ray(start, direction);
        reflectiveRay.purpose = RayPurpose.RAY_REFLECT;
        reflectiveRay.originators.add(this);

        if(oldRay != null) {
            reflectiveRay.originators.addAll(oldRay.originators);
        }

        Color reflecColor = scenegraph.getColorForRay(reflectiveRay);

        return reflecColor;
    }

    @Override
    public Color getColorForRay(final Ray ray, Stack<Matrix4f> modelView) {
        this.tracer = TracerFactory.getTracer(objInstanceName);

        if(tracer == null)
            return Color.BLACK;

        Ray newRay = new Ray(ray);

        // The ray is now in Object's coordinate system.
        new Matrix4f(modelView.peek()).invert().transform(newRay.start);
        new Matrix4f(modelView.peek()).invert().transform(newRay.direction);

        List<Light> l = scenegraph.getLights();

        tracer.intersectThisRay(newRay);
        ray.t = newRay.t;

        if(newRay.t == -1)
            return Color.BLACK;

        float intersection = newRay.t;
        ray.t = intersection;

        if(ray.purpose == RayPurpose.RAY_SHADOW) {
            if(ray.originators.contains(this)) {
                ray.t = -1f;
                return Color.BLACK;
            }
            return Color.WHITE;
        }

        Vector4f normal = tracer.getNormalForRay(newRay);
        Vector4f position = tracer.getPositionForRay(newRay);

        Vector4f positionInObjSpace = new Vector4f(position);

        Ray ray2 = new Ray(ray);
        ray2.t = intersection;

        // normal and position are in World coordinate system.
        normal = new Matrix4f(modelView.peek()).transform(normal);
        position = new Matrix4f(modelView.peek()).transform(position);

        Color absorptive = tracer.getLightColorAt(this.getMaterial(), ray2, l, normal, position);
        float abs = this.material.getAbsorption();

        if(abs != 0)
            absorptive = tracer.fractionOf(absorptive, abs);

        int shadowHits = inShadow(position);
        if(shadowHits > 0) {
            for(int i = 0; i < shadowHits; ++i) {
                absorptive = absorptive.darker();
            }
        }

        TextureImage textureImage = null;

        if(textureName == null || textureName == ""){
            //ret = absorptive;
        } else {
            textureImage = scenegraph.getTexture(textureName);

            // latitude and longitude on the sphere/box in teh range 0 - 1 for X and Y axes.
            float[] coords = tracer.getTextureCoordinatesForPoint(positionInObjSpace);
            //Vector4f txColor = textureImage.getColor(coords[0] * 255, coords[1] * 255);
            Vector4f texColor;

            //If dieflip, use the box-outside method to get specific areas of the texture image.
            if (objInstanceName.equals("box-outside")) {
                texColor = textureImage.getColorBoxOutside(coords[0], coords[1], coords[2]);
            } else {
                texColor = textureImage.getColor(coords[0], coords[1]);
            }

            try {
                Color ret = addTextureColortoColor(absorptive, texColor, false);
                absorptive = new Color(ret.getRGB());
                //ret = absorptive;

                return ret;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If this ray is reflective, return the reflective color instead of going ahead...
        if (ray.purpose == RayPurpose.RAY_REFLECT) {

            // If this ray has been bouncing around for MAX_REFLECT_COUNT times, return the current color.
            if(ray.originators.size() >= Ray.MAX_REFLECT_COUNT) {
                return absorptive;
            }

            if(this.material.getReflection() == 0) {
                return absorptive;
            }
            // Else, shoot another ray from here and return that color with the current color.
            Color reflectionColor = getReflectionColor(position, normal, ray);

            // If this re-reflected ray didn't hit anyone, return the current color.
            if(reflectionColor == Color.BLACK) {
                return absorptive;
            }

            reflectionColor = tracer.fractionOf(reflectionColor, material.getReflection());

            return tracer.addColor(absorptive, reflectionColor);
        }

        if(material.getReflection() != 0) {
            Color reflectionColor = getReflectionColor(position, normal, null);
            if (reflectionColor != Color.BLACK) {
                absorptive = tracer.addColor(absorptive, reflectionColor);
            }
        }



        return absorptive;
    }

    private Color addTextureColortoColor(Color absorptive, Color texColor) {
        float[] arr = texColor.getRGBColorComponents(null);

        return addTextureColortoColor(absorptive, new Vector4f(arr[0], arr[1], arr[2], 1.0f), false);
    }


    private Color addTextureColortoColor(Color absorptive, Vector4f textureColor, boolean scale) {
        Color ret = null;
        float[] colorAsVec = absorptive.getColorComponents(null);

        if(scale) {
            ret = new Color(textureColor.x * tracer.scaleToRange(colorAsVec[0], 0, 255),
                    textureColor.y * tracer.scaleToRange(colorAsVec[1], 0, 255),
                    textureColor.z * tracer.scaleToRange(colorAsVec[2], 0, 255),
                    1);
        } else {
            ret = new Color(textureColor.x * colorAsVec[0],
                    textureColor.y * colorAsVec[1],
                    textureColor.z * colorAsVec[2],
                    1);
        }

        return ret;
    }


}
