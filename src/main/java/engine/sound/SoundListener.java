package engine.sound;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundListener {

    public record Orientation(Vector3f forward, Vector3f up) {}

    public static final Orientation DEFAULT_ORIENTATION = new Orientation(new Vector3f().set(0.0f, 0.0f, -1.0f), new Vector3f().set(0.0f, 1.0f, 0.0f));

    private Vector3f position;
    private Vector3f velocity;
    private Orientation orientation;

    public SoundListener(float x, float y, float z) {
        this(new Vector3f().set(x, y, z), new Vector3f(), DEFAULT_ORIENTATION);
    }

    public SoundListener(float x, float y, float z, float vx, float vy, float vz) {
        this(new Vector3f().set(x, y, z), new Vector3f().set(vx, vy, vz), DEFAULT_ORIENTATION);
    }

    public SoundListener(float x, float y, float z, float vx, float vy, float vz, float fx, float fy, float fz, float ux, float uy, float uz) {
        this(new Vector3f().set(x, y, z), new Vector3f().set(vx, vy, vz), new Vector3f().set(fx, fy, fz), new Vector3f().set(ux, uy, uz));
    }

    public SoundListener(Vector3f position) {
        this(position, new Vector3f(), DEFAULT_ORIENTATION);
    }

    public SoundListener(Vector3f position, Vector3f velocity) {
        this(position, velocity, DEFAULT_ORIENTATION);
    }

    public SoundListener(Vector3f position, Vector3f velocity, Vector3f forward, Vector3f up) {
        this(position, velocity, new Orientation(forward, up));
    }

    public SoundListener(Vector3f position, Vector3f velocity, Orientation orientation) {
        setPosition(position);
        setVelocity(velocity);
        setOrientation(orientation);

    }

    public Vector3f getPosition() { return position; }
    public Vector3f getVelocity() { return velocity; }
    public Orientation getOrientation() { return orientation; }
    public Vector3f getForward() { return orientation.forward(); }
    public Vector3f getUp() { return orientation.up(); }

    public void setPosition(float x, float y, float z) { setPosition(new Vector3f().set(x, y, z)); }
    public void setPosition(Vector3f position) {
        this.position = position;
        alListener3f(AL_POSITION, this.position.x, this.position.y, this.position.z);
    }

    public void setVelocity(float vx, float vy, float vz) { setVelocity(new Vector3f().set(vx, vy, vz)); }
    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
        alListener3f(AL_VELOCITY, this.velocity.x, this.velocity.y, this.velocity.z);
    }

    public void setOrientation(float fx, float fy, float fz, float ux, float uy, float uz) { setOrientation(new Vector3f().set(fx, fy, fz), new Vector3f().set(ux, uy, uz)); }
    public void setOrientation(Vector3f forward, Vector3f up) { setOrientation(new Orientation(forward, up)); }
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        float[] data =  {
            this.orientation.forward.x, this.orientation.forward.y, this.orientation.forward.z,
            this.orientation.up.x,      this.orientation.up.y,      this.orientation.up.z
        };
        alListenerfv(AL_ORIENTATION, data);
    }

}
