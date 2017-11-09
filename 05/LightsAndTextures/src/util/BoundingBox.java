package util;

import org.joml.Vector4f;

/**
 * Created by krish on 11/9/2017.
 */
public class BoundingBox {

    protected Vector4f minBounds, maxBounds;

    public BoundingBox() {
        this.minBounds = new Vector4f();
        this.maxBounds = new Vector4f();
    }

    public BoundingBox(BoundingBox boundingBox) {
        this.minBounds = new Vector4f(boundingBox.getMinBounds());
        this.maxBounds = new Vector4f(boundingBox.getMaxBounds());
    }

    public BoundingBox(Vector4f minBounds, Vector4f maxBounds) {
        this.minBounds = minBounds;
        this.maxBounds = maxBounds;
    }

    public Vector4f getMinBounds() {
        return minBounds;
    }

    public void setMinBounds(Vector4f minBounds) {
        this.minBounds = minBounds;
    }

    public Vector4f getMaxBounds() {
        return maxBounds;
    }

    public void setMaxBounds(Vector4f maxBounds) {
        this.maxBounds = maxBounds;
    }
}
