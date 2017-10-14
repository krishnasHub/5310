import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;


import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly
 * encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View {
  private static final float DISPLACEMENT = 10.0f;
  private static final float PAN_ANGLE = 2f;
  private static final float CAM_DIST = 200.0f;

  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Stack<Matrix4f> modelView;
  private Matrix4f projection, trackballTransform;
  private float trackballRadius;
  private Vector2f mousePos;
  private Vector3f eyePosition;
  private Vector3f lookAtPosition;



  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;
  private int projectionLocation;
  private sgraph.IScenegraph<VertexAttrib> scenegraph;


  private float angle_xz = 0;
  private float angle_yz = 0;

  private void updateLookAtPosition() {
    float x = eyePosition.x;
    float y = eyePosition.y;
    float z = eyePosition.z;

    lookAtPosition.x = x + CAM_DIST * (float) Math.sin(Math.toRadians(angle_xz));
    lookAtPosition.z = z - CAM_DIST * (float) Math.cos(Math.toRadians(angle_xz));


    lookAtPosition.y = y + CAM_DIST * (float) Math.sin(Math.toRadians(angle_yz));
    lookAtPosition.z = z - CAM_DIST * (float) Math.cos(Math.toRadians(angle_yz));
  }

  public View() {
    eyePosition = new Vector3f(0, 0, 200);
    lookAtPosition = new Vector3f(0, 0, 0);
    updateLookAtPosition();

    projection = new Matrix4f();
    modelView = new Stack<Matrix4f>();
    trackballRadius = 300;
    trackballTransform = new Matrix4f();
    scenegraph = null;
  }

  public void initScenegraph(GLAutoDrawable gla, InputStream in) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    if (scenegraph != null)
      scenegraph.dispose();

    program.enable(gl);

    scenegraph = sgraph.SceneXMLReader.importScenegraph(in, new VertexAttribProducer());

    sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
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

    program.createProgram(gl, "shaders/default.vert", "shaders/default"
            + ".frag");

    shaderLocations = program.getAllShaderVariables(gl);

    //get input variables that need to be given to the shader program
    projectionLocation = shaderLocations.getLocation("projection");
  }


  public void draw(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
    gl.glEnable(gl.GL_DEPTH_TEST);


    program.enable(gl);

    while (!modelView.empty())
      modelView.pop();

        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
    modelView.push(new Matrix4f());
    modelView.peek().lookAt(eyePosition, lookAtPosition, new Vector3f(0, 1, 0)).mul(trackballTransform);


    /*
     *Supply the shader with all the matrices it expects.
    */
    FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
    gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));
    //return;


    //gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES

    scenegraph.draw(modelView);
    /*
     *OpenGL batch-processes all its OpenGL commands.
          *  *The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
     *
     *This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to
     *finish drawing.
     *
     *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */
    gl.glFlush();

    program.disable(gl);


  }

  private void zoomInDirection(int dir) {
    float deltaZ = eyePosition.z - lookAtPosition.z;
    float deltaX = eyePosition.x - lookAtPosition.x;

    float tetha = (float) Math.atan(deltaX / deltaZ);

    eyePosition.z -= (float)(DISPLACEMENT * dir * Math.cos(tetha));
    eyePosition.x -= (float)(DISPLACEMENT * dir * Math.sin(tetha));

    lookAtPosition.z -= (float)(DISPLACEMENT * dir * Math.cos(tetha));
    lookAtPosition.x -= (float)(DISPLACEMENT * dir * Math.sin(tetha));
  }




  private void rotateInDirection(int dir) {
    //lookAtPosition.x += dir * DISPLACEMENT;

    angle_xz += PAN_ANGLE * dir;
    updateLookAtPosition();
  }

  private void nodInDirection(int dir) {
    //lookAtPosition.y += dir * DISPLACEMENT;

    angle_yz += PAN_ANGLE * dir;
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

    projection = new Matrix4f().perspective((float) Math.toRadians(60.0f), (float) width / height, 0.1f, 10000.0f);
    // proj = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

  }


}
