package sgraph;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.util.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import util.IVertexData;
import util.Light;
import util.TextureImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.*;

/**
 * This is a scene graph renderer implementation that works specifically with the JOGL library
 * It mandates OpenGL 3 and above.
 * @author Amit Shesh
 */
public class GL3ScenegraphRenderer implements IScenegraphRenderer {
    /**
     * The JOGL specific rendering context
     */
    protected GLAutoDrawable glContext;
    /**
     * A table of shader locations and variable names
     */
    protected util.ShaderLocationsVault shaderLocations;
    /**
     * A table of shader variables -> vertex attribute names in each mesh
     */
    protected Map<String,String> shaderVarsToVertexAttribs;

    /**
     * A map to store all the textures
     */
    protected Map<String, TextureImage> textures;
    /**
     * A table of renderers for individual meshes
     */
    protected Map<String,util.ObjectInstance> meshRenderers;

    private static Map<String, util.TextureImage> imageMap;

    /**
     * A variable tracking whether shader locations have been set. This must be done before
     * drawing!
     */
    private boolean shaderLocationsSet;

    private boolean mipmapped = false;

    public GL3ScenegraphRenderer()
    {
        meshRenderers = new HashMap<String,util.ObjectInstance>();
        shaderLocations = new util.ShaderLocationsVault();
        shaderLocationsSet = false;

        buildImageMap();
    }

    private void buildImageMap() {
        if(imageMap != null)
            return;

        imageMap = new HashMap<>();

        try {
            File textureFolder = new File("src/textures");

            String[] files = textureFolder.list();

            for(int i = 0; i < files.length; ++i) {
                String textureName = files[i];
                int ind = textureName.lastIndexOf('.');

                util.TextureImage textureImage = new util.TextureImage("textures/" + textureName,
                        textureName.substring(ind + 1), textureName.substring(0, ind));
                imageMap.put(textureName, textureImage);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


    }

    /**
     * Specifically checks if the passed rendering context is the correct JOGL-specific
     * rendering context {@link GLAutoDrawable}
     * @param obj the rendering context (should be {@link GLAutoDrawable})
     * @throws IllegalArgumentException if given rendering context is not {@link GLAutoDrawable}
     */
    @Override
    public void setContext(Object obj) throws IllegalArgumentException
    {
        if (obj instanceof GLAutoDrawable)
        {
            glContext = (GLAutoDrawable)obj;
        }
        else
            throw new IllegalArgumentException("Context not of type GLAutoDrawable");
    }

    /**
     * Add a mesh to be drawn later.
     * The rendering context should be set before calling this function, as this function needs it
     * This function creates a new
     * {@link util.ObjectInstance} object for this mesh
     * @param name the name by which this mesh is referred to by the scene graph
     * @param mesh the {@link util.PolygonMesh} object that represents this mesh
     * @throws Exception
     */
    @Override
    public <K extends IVertexData> void addMesh(String name, util.PolygonMesh<K> mesh) throws Exception
    {
        if (!shaderLocationsSet)
            throw new Exception("Attempting to add mesh before setting shader variables. Call initShaderProgram first");
        if (glContext==null)
            throw new Exception("Attempting to add mesh before setting GL context. Call setContext and pass it a GLAutoDrawable first.");

        if (meshRenderers.containsKey(name))
            return;

        //verify that the mesh has all the vertex attributes as specified in the map
        if (mesh.getVertexCount()<=0)
            return;
        K vertexData = mesh.getVertexAttributes().get(0);
      GL3 gl = glContext.getGL().getGL3();

      for (Map.Entry<String,String> e:shaderVarsToVertexAttribs.entrySet()) {
            if (!vertexData.hasData(e.getValue()))
                throw new IllegalArgumentException("Mesh does not have vertex attribute "+e.getValue());
        }
      util.ObjectInstance obj = new util.ObjectInstance(gl,
              shaderLocations,shaderVarsToVertexAttribs,mesh,name);

      meshRenderers.put(name,obj);
    }

    @Override
    public void addTexture(String name,String path)
    {
        TextureImage image = null;
        String imageFormat = path.substring(path.indexOf('.')+1);
        try {
            image = new TextureImage(path,imageFormat,name);
        } catch (IOException e) {
            throw new IllegalArgumentException("Texture "+path+" cannot be read!");
        }
        textures.put(name,image);
    }

    /**
     * Begin rendering of the scene graph from the root
     * @param root
     * @param modelView
     */
    @Override
    public void draw(INode root, Stack<Matrix4f> modelView)
    {
        root.draw(this,modelView);
    }

    @Override
    public void dispose()
    {
        for (util.ObjectInstance s:meshRenderers.values())
            s.cleanup(glContext);
    }




    /**
     * Draws a specific mesh.
     * If the mesh has been added to this renderer, it delegates to its correspond mesh renderer
     * This function first passes the material to the shader. Currently it uses the shader variable
     * "vColor" and passes it the ambient part of the material. When lighting is enabled, this method must
     * be overriden to set the ambient, diffuse, specular, shininess etc. values to the shader
     * @param name
     * @param material
     * @param transformation
     */
    @Override
    public void drawMesh(String name, util.Material material,String textureName,final Matrix4f transformation) {
        if (meshRenderers.containsKey(name))
        {
            GL3 gl = glContext.getGL().getGL3();
            FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
            FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);
            Texture tex = null;
            util.TextureImage textureImage = null;

            if(textureName == null || textureName == "")
                textureName = "white.png";

            textureImage = imageMap.get(textureName);
            tex = textureImage.getTexture();

            tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);


            Matrix4f normalmatrix = new Matrix4f(transformation);
            normalmatrix = normalmatrix.invert().transpose();
            gl.glUniformMatrix4fv(shaderLocations.getLocation("modelview"), 1, false, transformation.get(fb16));
            gl.glUniformMatrix4fv(shaderLocations.getLocation("normalmatrix"), 1, false, normalmatrix.get(fb16));

            Matrix4f textureTransform = new Matrix4f();

            if(textureImage.getTexture().getMustFlipVertically()) {
                textureTransform = new Matrix4f().translate(0, 1, 0).scale(1, -1, 1);
            }
            textureTransform = new Matrix4f(textureTransform);

            gl.glUniformMatrix4fv(shaderLocations.getLocation("texturematrix"), 1, false, textureTransform.get(fb16));
            gl.glUniform3fv(shaderLocations.getLocation("material.ambient"), 1, material.getAmbient().get(fb4));
            gl.glUniform3fv(shaderLocations.getLocation("material.diffuse"), 1, material.getDiffuse().get(fb4));
            gl.glUniform3fv(shaderLocations.getLocation("material.specular"), 1, material.getSpecular().get(fb4));
            gl.glUniform1f(shaderLocations.getLocation("material.shininess"), material.getShininess());

            if (mipmapped)
                textureImage.getTexture().setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
            else
                textureImage.getTexture().setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            textureImage.getTexture().bind(gl);


            if(textures != null) {
                textures.get(name).getTexture().setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
                textures.get(name).getTexture().bind(gl);
            }

            meshRenderers.get(name).draw(glContext);
            //System.out.println("Done drawing Mesh");
        }
    }


    int dir = 1;
    private void tinkerLightPos(Light light) {
        // Table Light
        if(light.getSpotDirection().y != -1)
            return;

        Vector4f pos = light.getPosition();

        pos.y += dir * 0.05f;

        if(pos.y >= 50 || pos.y <= 0)
            dir *= -1;

        light.setPosition(pos);
    }

    public void drawLight() {
        GL3 gl = glContext.getGL().getGL3();

        for(int i = 0; i < allLights.size(); ++i) {
            Light light = allLights.get(i);
            //tinkerLightPos(light);
            Matrix4f transformation = allTransformations.get(i);
            FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

            LightLocation ll = new LightLocation();
            String name;

            name = "light[" + i + "]";
            ll.ambient = shaderLocations.getLocation(name + ".ambient");
            ll.diffuse = shaderLocations.getLocation(name + ".diffuse");
            ll.specular = shaderLocations.getLocation(name + ".specular");
            ll.position = shaderLocations.getLocation(name + ".position");
            ll.spotCutoff = shaderLocations.getLocation(name + ".spotCutoff");
            ll.spotDirection = shaderLocations.getLocation(name + ".spotDirection");

            //gl.glUniform4fv(light.getPosition(), 1, pos.get(fb4));

            Vector4f pos = light.getPosition();
            Matrix4f lightTransformation = new Matrix4f(transformation);

            pos = lightTransformation.transform(pos);
            gl.glUniform4fv(ll.position, 1, pos.get(fb4));
            gl.glUniform3fv(ll.ambient, 1, light.getAmbient().get(fb4));
            gl.glUniform3fv(ll.diffuse, 1, light.getDiffuse().get(fb4));
            gl.glUniform3fv(ll.specular, 1, light.getSpecular().get(fb4));
            gl.glUniform1f(ll.spotCutoff, light.getSpotCutoff());
            gl.glUniform3fv(ll.spotDirection, 1, light.getSpotDirection().get(fb4));
            //System.out.println(name + " x=" + pos.x + " y=" + pos.y + " z=" + pos.z);

            //System.out.println("Done drawing Light");
        }


    }

    public void drawMeshes() {
        meshInfoList.forEach(m -> {
            drawMesh(m.name, m.material, m.textureName, m.transformation);
        });
    }

    List<MeshInfo> meshInfoList;
    List<Light> allLights = null;
    List<Matrix4f> allTransformations = null;

    public void storeLight(Light light, final Matrix4f transformation) {
        allLights.add(light);
        allTransformations.add(transformation);
    }

    public void storeMeshInfo(MeshInfo meshInfo) {
        meshInfoList.add(meshInfo);
    }

    public int getMeshCount() {
        return meshInfoList.size();
    }

    public int getLightCount() {
        return allLights.size();
    }

    public void clearRenderer() {
        if(allLights == null)
            allLights = new ArrayList<>();
        else
            allLights.clear();

        if(allTransformations == null)
            allTransformations = new ArrayList<>();
        else
            allTransformations.clear();

        if(meshInfoList == null)
            meshInfoList = new ArrayList<>();
        else
            meshInfoList.clear();
    }

    /**
     * Queries the shader program for all variables and locations, and adds them to itself
     * @param shaderProgram
     */
    @Override
    public void initShaderProgram(util.ShaderProgram shaderProgram, Map<String,String> shaderVarsToVertexAttribs)
    {
        Objects.requireNonNull(glContext);
        GL3 gl = glContext.getGL().getGL3();

        this.shaderLocations = shaderProgram.getAllShaderVariables(gl);
        this.shaderVarsToVertexAttribs = new HashMap<String,String>(shaderVarsToVertexAttribs);
        this.shaderLocationsSet = true;

    }


    @Override
    public int getShaderLocation(String name)
    {
        return shaderLocations.getLocation(name);
    }
}