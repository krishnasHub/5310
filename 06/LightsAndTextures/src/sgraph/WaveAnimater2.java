package sgraph;

import org.joml.Matrix4f;

/**
 * Created by krish on 10/30/2017.
 */
public class WaveAnimater2 implements Animator {
    private float angle = 0.0f;
    private int dir = 1;

    @Override
    public Matrix4f animate(int time) {
        angle += dir * 1f;

        if(angle >= 40.0f || angle <= -20f)
            dir *= -1;

        Matrix4f anim = new Matrix4f()
                .mul(new Matrix4f().rotate((float) Math.toRadians(angle), 0, 0, 1));

        return anim;
    }
}
