package sgraph;

import org.joml.Matrix4f;

/**
 * Created by krish on 10/28/2017.
 */
public class Revolve100 implements Animator {
    private int radius = 150;

    @Override
    public Matrix4f animate(int time) {
        Matrix4f anim = new Matrix4f()
                .mul(new Matrix4f().rotate((float)Math.toRadians(0.1 * time), 0, 1, 0))
                .mul(new Matrix4f().translate(radius, 0, 0))
                .mul(new Matrix4f().rotate((float) Math.toRadians(-90), 1, 0, 0))
                ;

        return anim;
    }
}
