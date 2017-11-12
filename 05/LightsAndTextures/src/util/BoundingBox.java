package util;

import org.joml.Vector4f;

import java.util.List;

/**
 * Created by krish on 11/9/2017.
 *
 * A simple class representing a bounding box.
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

    public boolean collides(List<BoundingBox> boxes) {
        boolean ret = false;

        for(int i = 0; i < boxes.size(); ++i)
            if(boxes.get(i) != this)
                ret |= collides(boxes.get(i));

        return ret;
    }

    public static BoundingBox GetBoundingBoxFor(List<Vector4f> positions) {
        BoundingBox ret = new BoundingBox();

        ret.setMinBounds(new Vector4f(positions.get(0)));
        ret.setMaxBounds(new Vector4f(positions.get(0)));

        Vector4f minBounds = ret.getMinBounds();
        Vector4f maxBounds = ret.getMaxBounds();

        for(int i = 0; i < positions.size(); ++i) {
            Vector4f p = positions.get(i);

            if (p.x < minBounds.x) {
                minBounds.x = p.x;
            }

            if (p.x > maxBounds.x) {
                maxBounds.x = p.x;
            }

            if (p.y < minBounds.y) {
                minBounds.y = p.y;
            }

            if (p.y > maxBounds.y) {
                maxBounds.y = p.y;
            }

            if (p.z < minBounds.z) {
                minBounds.z = p.z;
            }

            if (p.z > maxBounds.z) {
                maxBounds.z = p.z;
            }
        }

        return ret;
    }

    public boolean collides(BoundingBox box) {
        if(box == this)
            return true;

        return (box.getMinBounds().x >= this.getMinBounds().x && box.getMinBounds().x <= this.getMaxBounds().x &&
                box.getMinBounds().y >= this.getMinBounds().y && box.getMinBounds().y <= this.getMaxBounds().y &&
                box.getMinBounds().z >= this.getMinBounds().z && box.getMinBounds().z <= this.getMaxBounds().z);
    }

    public Vector4f getCentroid() {
        return new Vector4f((this.getMinBounds().x + this.getMaxBounds().x) / 2.0f,
                (this.getMinBounds().y + this.getMaxBounds().y) / 2.0f,
                (this.getMinBounds().z + this.getMaxBounds().z) / 2.0f,
                1f);
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
