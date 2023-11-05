package engine.scene.model;

import engine.scene.view.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BillboardEntity extends Entity {

    private final Camera camera;
    private final boolean x;
    private final boolean y;

    public BillboardEntity(Camera camera, String id, String modelID, boolean x, boolean y) {
        super(id, modelID);
        this.camera = camera;
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
        Vector3f direction = new Vector3f(camera.getPosition()).sub(position).normalize();

        Quaternionf rot = new Quaternionf().identity();
        if (x) rot.rotateX((float) -Math.atan2(direction.y, direction.z));
        if (y) rot.rotateY((float) -Math.atan2(-direction.x, direction.z));
        modelMatrix.translationRotateScale(position, rot, scale);

        super.update();
    }

}
