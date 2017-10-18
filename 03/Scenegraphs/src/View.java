import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.math.Matrix4;
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
  private static final float CAM_DIST = 1f;

  private int WINDOW_WIDTH, WINDOW_HEIGHT;
  private Stack<Matrix4f> modelView;
  private Matrix4f projection, trackballTransform;
  private float trackballRadius;
  private Vector2f mousePos;
  private Vector3f eyePosition;
  private Vector3f savedEyePosition;
  private Vector3f lookAtPosition;
  private Vector3f savedLookAtPosition;
  private Vector3f upVector;




  private util.ShaderProgram program;
  private util.ShaderLocationsVault shaderLocations;
  private int projectionLocation;
  private sgraph.IScenegraph<VertexAttrib> scenegraph;
  private sgraph.IScenegraph<VertexAttrib> scenegraphYMCA;


  private float angle_xz = 0;
  private float angle_yz = 0;
  private float savedAngle_xz = angle_xz;
  private float savedAngle_yz = angle_yz;

  private boolean viewYMCA;

  public View() {
    eyePosition = new Vector3f(0, 0, 0);
    savedEyePosition = new Vector3f(0, 0, 200);
    lookAtPosition = new Vector3f(0, 0, 0);
    savedLookAtPosition = new Vector3f(0, 0, 200);
    upVector = new Vector3f(0, 1, 0);

    updateLookAtPosition();

    projection = new Matrix4f();
    modelView = new Stack<Matrix4f>();
    trackballRadius = 300;
    trackballTransform = new Matrix4f();
    scenegraph = null;
    scenegraphYMCA = null;
    viewYMCA = false;
  }

  public void initScenegraph(GLAutoDrawable gla, InputStream in) throws Exception {
    GL3 gl = gla.getGL().getGL3();

    if (scenegraph != null)
      scenegraph.dispose();

    if (scenegraphYMCA != null)
      scenegraphYMCA.dispose();


    program.enable(gl);

    scenegraph = sgraph.SceneXMLReader.importScenegraph(in, new VertexAttribProducer());
    scenegraphYMCA = sgraph.SceneXMLReader.importScenegraph(getClass().getClassLoader()
            .getResourceAsStream
                    ("scenegraphmodels/YMCA-humanoid.xml"), new VertexAttribProducer());

    sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
    renderer.setContext(gla);
    Map<String, String> shaderVarsToVertexAttribs = new HashMap<String, String>();
    shaderVarsToVertexAttribs.put("vPosition", "position");
    shaderVarsToVertexAttribs.put("vNormal", "normal");
    shaderVarsToVertexAttribs.put("vTexCoord", "texcoord");
    renderer.initShaderProgram(program, shaderVarsToVertexAttribs);
    scenegraph.setRenderer(renderer);
    scenegraphYMCA.setRenderer(renderer);
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
    modelView.peek().lookAt(eyePosition, lookAtPosition, upVector).mul(trackballTransform);


    /*
     *Supply the shader with all the matrices it expects.
    */
    FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
    gl.glUniformMatrix4fv(projectionLocation, 1, false, projection.get(fb));
    //return;


    //gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES
    if (viewYMCA == true) {
      scenegraphYMCA.draw(modelView);
    } else if (viewYMCA == false) {
      scenegraph.draw(modelView);
    }

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
    eyePosition.z -= lookingForward * (float)(DISPLACEMENT * dir * Math.cos(Math.toRadians(angle_xz)));
    eyePosition.x += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_xz)));
    eyePosition.y += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_yz)));
    //eyePosition.z -= (float)(DISPLACEMENT * dir * Math.cos(Math.toRadians(angle_yz)));

    lookAtPosition.z -= lookingForward * (float)(DISPLACEMENT * dir * Math.cos(Math.toRadians(angle_xz)));
    lookAtPosition.x += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_xz)));
    lookAtPosition.y += (float)(DISPLACEMENT * dir * Math.sin(Math.toRadians(angle_yz)));
    //lookAtPosition.z -= (float)(DISPLACEMENT * dir * Math.cos(Math.toRadians(angle_yz)));
  }

  int lookingForward = 1;

  private void updateLookAtPosition() {
    float x = eyePosition.x;
    float y = eyePosition.y;
    float z = eyePosition.z;

    if(angle_yz >= 94 && angle_yz <= 270) {
      upVector.y = -(0.1f + (angle_yz + 2 % 180) / 180.0f);
      lookingForward = -1;
    } else {
      upVector.y = 0.1f + (angle_yz + 2 % 180) / 180.0f;
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

    /*
    if(angle_yz >= 90 && angle_yz <= 270) {
      upVector.y = -1;
    } else {
      upVector.y = 1;
    }*/

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
      case KeyEvent.VK_Y:
        // Save the state of your eye position and what you are looking at in the room.
        savedEyePosition = eyePosition;
        savedLookAtPosition = lookAtPosition;
        savedAngle_xz = angle_xz;
        savedAngle_yz = angle_yz;

        // Reset it to look at YMCA scene.
        eyePosition = new Vector3f(0, 0, 200);
        lookAtPosition = new Vector3f(0, 0, 0);
        angle_xz = 0;
        angle_yz = 0;
        viewYMCA = true;
        break;
      case KeyEvent.VK_R:
        // Reload the saved state before moving to YMCA.
        eyePosition = savedEyePosition;
        lookAtPosition = savedLookAtPosition;
        angle_xz = savedAngle_xz;
        angle_yz = savedAngle_yz;
        viewYMCA = false;
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

    projection = new Matrix4f().perspective((float) Math.toRadians(75), (float) width / height, 0.1f, 10000.0f);
    // proj = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

  }

  public void dispose(GLAutoDrawable gla) {
    GL3 gl = gla.getGL().getGL3();

  }


}
