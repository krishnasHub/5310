import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import sgraph.GL3ScenegraphRenderer;
import sgraph.LightLocation;
import sgraph.LightNode;
import util.ObjectInstance;


import java.io.*;
import java.nio.FloatBuffer;
import java.util.*;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {

  private static final String TABLE = "scenegraphmodels/table.xml";
  private static final String HUMANOID = "scenegraphmodels/humanoid-lights.xml";
  private static final String SCENE_GRAPH_XML = TABLE;

  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Matrix4f proj;
  private Stack<Matrix4f> modelView;
  private List<ObjectInstance> meshObjects;
  private List<util.TextureImage> textures;
  private List<util.Material> materials;
  private List<Matrix4f> transforms;
  private List<util.Light> lights;
  //0-meshObjects.size()-1 are object coordinates, then world and then view
  private List<Integer> lightCoordinateSystems;
  private Matrix4f trackballTransform, textureTransform;
  private float trackballRadius;
  private Vector2f mousePos;
  private boolean mipmapped;


  private sgraph.IScenegraph<VertexAttrib> scenegraph;
  private sgraph.IScenegraph<VertexAttrib> scenegraphYMCA;


  util.ShaderProgram program;
  util.ShaderLocationsVault shaderLocations;
  private int modelviewLocation, projectionLocation, normalmatrixLocation, texturematrixLocation;
  private int materialAmbientLocation, materialDiffuseLocation, materialSpecularLocation, materialShininessLocation;
  private int textureLocation;
  private List<LightLocation> lightLocations;
  private int numLightsLocation;
  int angleOfRotation;

  private sgraph.IScenegraphRenderer renderer;

  public View() {
    proj = new Matrix4f();
    proj.identity();

    modelView = new Stack<Matrix4f>();

    meshObjects = new ArrayList<ObjectInstance>();
    transforms = new ArrayList<Matrix4f>();
    materials = new ArrayList<util.Material>();
    lights = new ArrayList<util.Light>();
    lightCoordinateSystems = new ArrayList<Integer>();
    lightLocations = new ArrayList<LightLocation>();
    textures = new ArrayList<util.TextureImage>();
    textureTransform = new Matrix4f();

    trackballTransform = new Matrix4f();
    angleOfRotation = 0;
    trackballRadius = 300;
    mipmapped = true;

  }

  public void toggleMipmapping() {
    mipmapped = !mipmapped;
  }

  private void initObjects(GL3 gl) throws FileNotFoundException, IOException {

    util.PolygonMesh<?> tmesh;

    InputStream in;

    in = getClass().getClassLoader().getResourceAsStream
            ("models/sphere.obj");

    tmesh = util.ObjImporter.importFile(new VertexAttribProducer(), in, true);
    util.ObjectInstance obj;

    Map<String, String> shaderToVertexAttribute = new HashMap<String, String>();

    shaderToVertexAttribute.put("vPosition", "position");
    shaderToVertexAttribute.put("vNormal", "normal");
    shaderToVertexAttribute.put("vTexCoord", "texcoord");


    obj = new util.ObjectInstance(
            gl,
            program,
            shaderLocations,
            shaderToVertexAttribute,
            tmesh, new String(""));
    meshObjects.add(obj);
    util.Material mat;

    mat = new util.Material();

    mat.setAmbient(0.3f, 0.3f, 0.3f);
    mat.setDiffuse(0.7f, 0.7f, 0.7f);
    mat.setSpecular(0.7f, 0.7f, 0.7f);
    mat.setShininess(100);
    materials.add(mat);

    Matrix4f t;

    t = new Matrix4f().translate(-200, 0, 0).scale(1, 1, 1);
    transforms.add(t);

    // textures

    util.TextureImage textureImage;

    textureImage = new util.TextureImage("textures/white.png",
            "png",
            "earthmap");

    Texture tex = textureImage.getTexture();


    tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
    tex.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
    tex.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
    tex.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

    textures.add(textureImage);


  }

  private void initLights() {
    util.Light l = new util.Light();
    l.setAmbient(0.8f, 0.8f, 0.8f);
    l.setDiffuse(0.5f, 0.5f, 0.5f);
    l.setSpecular(0.5f, 0.5f, 0.5f);
    l.setPosition(00, 00, 100);
    //lights.add(l);
    //read comments above where this list is declared
    //lightCoordinateSystems.add(meshObjects.size()); //world


  }

  private void initShaderVariables() {
    //get input variables that need to be given to the shader program
    projectionLocation = shaderLocations.getLocation("projection");
    modelviewLocation = shaderLocations.getLocation("modelview");
    normalmatrixLocation = shaderLocations.getLocation("normalmatrix");
    texturematrixLocation = shaderLocations.getLocation("texturematrix");
    materialAmbientLocation = shaderLocations.getLocation("material.ambient");
    materialDiffuseLocation = shaderLocations.getLocation("material.diffuse");
    materialSpecularLocation = shaderLocations.getLocation("material.specular");
    materialShininessLocation = shaderLocations.getLocation("material.shininess");

    textureLocation = shaderLocations.getLocation("image");

    numLightsLocation = shaderLocations.getLocation("numLights");
    for (int i = 0; i < lights.size(); i++) {
      LightLocation ll = new LightLocation();
      String name;

      name = "light[" + i + "]";
      ll.ambient = shaderLocations.getLocation(name + "" + ".ambient");
      ll.diffuse = shaderLocations.getLocation(name + ".diffuse");
      ll.specular = shaderLocations.getLocation(name + ".specular");
      ll.position = shaderLocations.getLocation(name + ".position");
      lightLocations.add(ll);
    }
  }

  public void initSceneGraph(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();
    if (scenegraph != null)
      scenegraph.dispose();

    InputStream in = getClass().getClassLoader().getResourceAsStream(SCENE_GRAPH_XML);


    program.enable(gl);

    scenegraph = sgraph.SceneXMLReader.importScenegraph(in, new VertexAttribProducer());
    renderer = new sgraph.GL3ScenegraphRenderer();
    renderer.setContext(gla);
    Map<String, String> shaderVarsToVertexAttribs = new HashMap<String, String>();
    shaderVarsToVertexAttribs.put("vPosition", "position");
    shaderVarsToVertexAttribs.put("vNormal", "normal");
    shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
    renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
    scenegraph.setRenderer(renderer);

    program.disable(gl);
  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new util.ShaderProgram();
    program.createProgram(gl, "shaders/phong-multiple.vert",
            "shaders/phong-multiple.frag");
    shaderLocations = program.getAllShaderVariables(gl);

    initObjects(gl);
    initLights();
    initShaderVariables();
    initSceneGraph(gla);
  }

  private Vector3f eyePosition = new Vector3f(0.0f, 0.0f, 200.0f);
  private Vector3f lookAtPosition = new Vector3f(0, 0, 0);
  private Vector3f upVector = new Vector3f(0, 1, 0);

  public void draw(GLAutoDrawable gla) {
    renderer.clearRenderer();

    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);
    FloatBuffer fb4 = Buffers.newDirectFloatBuffer(4);

    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glEnable(GL.GL_DEPTH_TEST);

    program.enable(gl);

    gl.glEnable(GL.GL_TEXTURE_2D);
    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glUniform1i(textureLocation, 0);


    /*
     *Supply the shader with all the matrices it expects.
    */
    gl.glUniformMatrix4fv(projectionLocation, 1, false, proj.get(fb16));
    //return;

    if(scenegraph != null) {
      while (!modelView.empty())
        modelView.pop();

      modelView.push(new Matrix4f());
      modelView.peek().lookAt(eyePosition, lookAtPosition, upVector).mul(trackballTransform);

      scenegraph.draw(modelView);
      //all the light properties, except positions
      gl.glUniform1i(numLightsLocation, renderer.getLightCount());

      //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
      renderer.drawLight();
      renderer.drawMeshes();

      //System.out.println("-------------------------------------------------------------------------");

    } else {
      //all the light properties, except positions
      gl.glUniform1i(numLightsLocation, lights.size());

    }

    gl.glFlush();

    program.disable(gl);


  }

  public void mousePressed(int x, int y) {
    mousePos = new Vector2f(x, y);
  }

  public void mouseReleased(int x, int y) {
    System.out.println("Released");
  }

  public void mouseDragged(int x, int y) {
    Vector2f newM = new Vector2f(x, y);

    Vector2f delta = new Vector2f(newM.x - mousePos.x, newM.y - mousePos.y);
    mousePos = new Vector2f(newM);

    trackballTransform = new Matrix4f().rotate(delta.x / trackballRadius, 0, 1, 0)
            .rotate(delta.y / trackballRadius, 1, 0, 0)
            .mul(trackballTransform);
  }

  public void reshape(GLAutoDrawable gla, int x, int y, int width, int height) {
    GL gl = gla.getGL();
    WINDOW_WIDTH = width;
    WINDOW_HEIGHT = height;
    gl.glViewport(0, 0, width, height);

    proj = new Matrix4f().perspective((float)Math.toRadians(60f),(float)
            width/height,0.1f,10000.0f);
    //proj = new Matrix4f().ortho(-50, 50, -50, 50, -50.0f, 10000.0f);

  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

  }


}
