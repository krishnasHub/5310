package sgraph;

import org.joml.Matrix4f;

/**
 * Created by krish on 10/28/2017.
 */
public class RotateByZ implements Animator {
    @Override
    public Matrix4f animate(int time) {
        Matrix4f anim = new Matrix4f()
                .mul(new Matrix4f().rotate((float) Math.toRadians(0.5f * time), 0, 1, 0));

        return anim;
    }

}
