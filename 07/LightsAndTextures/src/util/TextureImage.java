package util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import org.joml.Vector4f;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Color;

/**
 * A class that represents an image. Provides a function for bilinear
 * interpolation
 */
public class TextureImage {
  private BufferedImage image;
  private String name;
  private Texture texture;

  public TextureImage(String filepath, String imageFormat, String name) throws IOException {
    //read the image
    InputStream in = getClass().getClassLoader().getResourceAsStream(filepath);
    texture = TextureIO.newTexture(in, true, imageFormat);
    in = getClass().getClassLoader().getResourceAsStream(filepath);
    image = ImageIO.read(in);
    this.name = new String(name);
  }

  public Texture getTexture() {
    return texture;
  }

  public String getName() {
    return name;
  }

  public Color getColor2(float x, float y) {
    return new Color(image.getRGB((int)x % image.getWidth(), (int) y % image.getHeight()));
  }

  public Vector4f getColor(float x, float y) {
    int x1, y1, x2, y2;

    x = x - (int) x; //GL_REPEAT
    y = y - (int) y; //GL_REPEAT

    x1 = (int) (x * image.getWidth());
    y1 = (int) (y * image.getHeight());

    x1 = (x1 + image.getWidth()) % image.getWidth();
    y1 = (y1 + image.getHeight()) % image.getHeight();

    x2 = x1 + 1;
    y2 = y1 + 1;

    if (x2 >= image.getWidth())
      x2 = image.getWidth() - 1;

    if (y2 >= image.getHeight())
      y2 = image.getHeight() - 1;

    Vector4f one = ColorToVector4f(new Color(image.getRGB(x1, y1)));
    Vector4f two = ColorToVector4f(new Color(image.getRGB(x2, y1)));
    Vector4f three = ColorToVector4f(new Color(image.getRGB(x1, y2)));
    Vector4f four = ColorToVector4f(new Color(image.getRGB(x2, y2)));

    Vector4f inter1 = one.lerp(three, 1);
    Vector4f inter2 = two.lerp(four, 1);
    Vector4f inter3 = inter1.lerp(inter2, 1);
    /**
     Vector4f inter1 = one.lerp(three, y - (int) y);
     Vector4f inter2 = two.lerp(four, y - (int) y);
     Vector4f inter3 = inter1.lerp(inter2, x - (int) x);
     **/
    return inter3;
  }

  /**
   * Gets the color for box outside textures, like the die)
  **/
  public Vector4f getColorBoxOutside(float x, float y, float side) {
    int x1, y1, x2, y2;

    x = x - (int) x; //GL_REPEAT
    y = y - (int) y; //GL_REPEAT

    x1 = (int) (x * image.getWidth());
    y1 = (int) (y * image.getHeight());

    x1 = (x1 + image.getWidth()) % image.getWidth();
    y1 = (y1 + image.getHeight()) % image.getHeight();

    // Scale down to a quarter of the image (quarter-size increments)
    x1 *= .25;
    y1 *= .25;

    // Then move over to get the right position of the texture image.
    switch ((int) side) {
      // FRONT
      case 1:
        x1 += (image.getWidth() / 4) * 3;
        y1 += (image.getHeight() / 4) * 2;
        break;
      // LEFT
      case 2:
        y1 += (image.getHeight() / 4) * 2;
        break;
      // BACK
      case 3:
        x1 += (image.getWidth() / 4);
        y1 += (image.getHeight() / 4) * 2;
        break;
      // RIGHT
      case 4:
        x1 += (image.getWidth() / 4) * 2;
        y1 += (image.getHeight() / 4) * 2;
        break;
      // TOP
      case 5:
        x1 += (image.getWidth() / 4);
        y1 += (image.getHeight() / 4);
        break;
      // BOTTOM
      case 6:
        x1 += (image.getWidth() / 4);
        y1 += (image.getHeight() / 4) * 3;
        break;
    }

    x2 = x1 + 1;
    y2 = y1 + 1;

    if (x2 >= image.getWidth())
      x2 = image.getWidth() - 1;

    if (y2 >= image.getHeight())
      y2 = image.getHeight() - 1;


    Vector4f one = ColorToVector4f(new Color(image.getRGB((int) x1, (int) y1)));
    Vector4f two = ColorToVector4f(new Color(image.getRGB((int) x2, (int) y1)));
    Vector4f three = ColorToVector4f(new Color(image.getRGB((int) x1, (int) y2)));
    Vector4f four = ColorToVector4f(new Color(image.getRGB((int) x2, (int) y2)));

    Vector4f inter1 = one.lerp(three, 1);
    Vector4f inter2 = two.lerp(four, 1);
    Vector4f inter3 = inter1.lerp(inter2, 1);
    /**
     * Already done above...
     Vector4f inter1 = one.lerp(three, y - (int) y);
     Vector4f inter2 = two.lerp(four, y - (int) y);
     Vector4f inter3 = inter1.lerp(inter2, x - (int) x);
     **/
    return inter3;
  }

  private Vector4f ColorToVector4f(Color c) {
    return new Vector4f((float) c.getRed() / 255, (float) c.getGreen() / 255, (float) c.getBlue() / 255, (float) c.getAlpha() / 255);
  }


}
