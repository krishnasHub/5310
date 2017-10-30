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


import java.awt.event.KeyEvent;
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

  private static final String SCENE = "scenegraphmodels/testScene.xml";
  private static final String TABLE = "scenegraphmodels/table.xml";
  private static final String HUMANOID = "scenegraphmodels/humanoid-lights.xml";
  private static final String SCENE_GRAPH_XML = SCENE;

  private static final float DISPLACEMENT = 10.0f;
  private static final float PAN_ANGLE = 2f;
  private static final float CAM_DIST = 1f;

  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Matrix4f proj;
  private Stack<Matrix4f> modelView;

  private Matrix4f trackballTransform;
  private float trackballRadius;
  private Vector2f mousePos;
  private boolean mipmapped;


  private sgraph.IScenegraph<VertexAttrib> scenegraph;
  private sgraph.IScenegraph<VertexAttrib> scenegraphYMCA;


  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;
  private int modelviewLocation, projectionLocation;

  private int textureLocation;
  private int numLightsLocation;
  int angleOfRotation;

  private float angle_xz = 0;
  private float angle_yz = 0;
  private float savedAngle_xz = angle_xz;
  private float savedAngle_yz = angle_yz;

  private sgraph.IScenegraphRenderer renderer;

  public View() {
    proj = new Matrix4f();
    proj.identity();

    modelView = new Stack<Matrix4f>();
    trackballTransform = new Matrix4f();
    angleOfRotation = 0;
    trackballRadius = 300;
    mipmapped = true;

  }

  public void toggleMipmapping() {
    mipmapped = !mipmapped;
  }

  private void initShaderVariables() {
    //get input variables that need to be given to the shader program
    projectionLocation = shaderLocations.getLocation("projection");
    modelviewLocation = shaderLocations.getLocation("modelview");
    numLightsLocation = shaderLocations.getLocation("numLights");

  }

  public void initSceneGraph(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();
    if (scenegraph != null)
      scenegraph.dispose();

    InputStream in = getClass().getClassLoader().getResourceAsStream(SCENE_GRAPH_XML);


    //program.enable(gl);

    scenegraph = sgraph.SceneXMLReader.importScenegraph(in, new VertexAttribProducer());
    renderer = new sgraph.GL3ScenegraphRenderer();
    renderer.setContext(gla);
    Map<String, String> shaderVarsToVertexAttribs = new HashMap<String, String>();
    shaderVarsToVertexAttribs.put("vPosition", "position");
    shaderVarsToVertexAttribs.put("vNormal", "normal");
    shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
    renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
    scenegraph.setRenderer(renderer);

    //program.disable(gl);
  }

  public void init(GLAutoDrawable gla) throws Exception {
    GL3 gl = gla.getGL().getGL3();


    //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
    program = new util.ShaderProgram();
    program.createProgram(gl, "shaders/phong-multiple.vert",
            "shaders/phong-multiple.frag");
    shaderLocations = program.getAllShaderVariables(gl);

    initShaderVariables();
    initSceneGraph(gla);
  }

  private Vector3f eyePosition = new Vector3f(0.0f, 0.0f, 200.0f);
  private Vector3f lookAtPosition = new Vector3f(0, 0, 0);
  private Vector3f upVector = new Vector3f(0, 1, 0);
  private int timer = 0;

  public void draw(GLAutoDrawable gla) {
    timer++;
    renderer.clearRenderer();
    if(timer == Integer.MAX_VALUE - 10)
      timer = 0;

    GL3 gl = gla.getGL().getGL3();
    FloatBuffer fb16 = Buffers.newDirectFloatBuffer(16);

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

    if(scenegraph != null) {
      scenegraph.animate(timer);

      while (!modelView.empty())
        modelView.pop();

      modelView.push(new Matrix4f());
      modelView.peek().lookAt(eyePosition, lookAtPosition, upVector).mul(trackballTransform);

      scenegraph.draw(modelView);
      //all the light properties, except positions
      gl.glUniform1i(numLightsLocation, renderer.getLightCount());

      renderer.drawLight();
      renderer.drawMeshes();
    }

    gl.glFlush();

    program.disable(gl);
  }

  private void zoomInDirection(int dir) {
    eyePosition.z -= lookingForward * (float)(DISPLACEMENT * dir * Math.cos(Math.toRadians(angle_xz)));
    eyePosition.x += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_xz)));
    eyePosition.y += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_yz)));

    lookAtPosition.z -= lookingForward * (float)(DISPLACEMENT * dir * Math.cos(Math.toRadians(angle_xz)));
    lookAtPosition.x += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_xz)));
    lookAtPosition.y += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_yz)));
  }

  int lookingForward = 1;

  private void updateLookAtPosition() {
    float x = eyePosition.x;
    float y = eyePosition.y;
    float z = eyePosition.z;

    if(angle_yz >= 94 && angle_yz <= 270) {
      upVector.y = -1;
      lookingForward = -1;
    } else {
      upVector.y = 1;
      lookingForward = 1;
    }

    lookAtPosition.x = x + CAM_DIST * (float) Math.sin(Math.toRadians(angle_xz));
    lookAtPosition.z = z - lookingForward * CAM_DIST * (float) Math.cos(Math.toRadians(angle_xz));
    lookAtPosition.y = y + CAM_DIST * (float) Math.sin(Math.toRadians(angle_yz));

    //printCam();
  }

  private void printCam() {
    System.out.println("eyePosition: x=" + eyePosition.x + ", y=" + eyePosition.y + ", z=" + eyePosition.z);
    System.out.println("lookAtPosition: x=" + lookAtPosition.x + ", y=" + lookAtPosition.y + ", z=" + lookAtPosition.z);
  }

  private void rotateInDirection(int dir) {
    //lookAtPosition.x += dir * DISPLACEMENT;

    angle_xz += PAN_ANGLE * dir;

    if(angle_xz < 0)
      angle_xz = 360f + angle_xz;

    angle_xz = angle_xz % 360;

    System.out.println("angle_xz=" + angle_xz);


    updateLookAtPosition();
  }

  private void nodInDirection(int dir) {
    //lookAtPosition.y += dir * DISPLACEMENT;

    angle_yz += PAN_ANGLE * dir;

    if(angle_yz < 0)
      angle_yz = 360f + angle_yz;

    angle_yz = angle_yz % 360;

    System.out.println("angle_yz=" + angle_yz);

    updateLookAtPosition();
  }


  public void keyEvent(KeyEvent e) {


    switch(e.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        rotateInDirection(-1);
        break;
      case KeyEvent.VK_RIGHT:
        rotateInDirection(1);
        break;
      case KeyEvent.VK_UP:
        zoomInDirection(1);
        break;
      case KeyEvent.VK_DOWN:
        zoomInDirection(-1);
        break;
      case KeyEvent.VK_W:
        nodInDirection(1);
        break;
      case KeyEvent.VK_S:
        nodInDirection(-1);
        break;
    }
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

    proj = new Matrix4f().perspective((float) Math.toRadians(75), (float) width / height, 0.1f, 10000.0f);

  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

  }
}
