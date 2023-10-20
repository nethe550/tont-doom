package engine.scene.light;

import org.joml.Vector3f;

public class PointLight extends Light {

    private Attenuation attenuation;
    private Vector3f color;
    private float intensity;
    private Vector3f position;

    public PointLight(Vector3f color, Vector3f position, float intensity) {
        super(intensity, color);
        this.position = position;
        attenuation = new Attenuation(0, 0, 1);
    }

    public Attenuation getAttenuation() { return attenuation; }
    public Vector3f getPosition() { return position; }

    public void setAttenuation(Attenuation attenuation) { this.attenuation = attenuation; }
    public void setPosition(float x, float y, float z) { position.set(x, y, z); }

    public class Attenuation {

        private float constant;
        private float linear;
        private float exponent;

        public Attenuation(float constant, float linear, float exponent) {
            this.constant = constant;
            this.linear = linear;
            this.exponent = exponent;
        }

        public float getConstant() { return constant; }
        public float getLinear() { return linear; }
        public float getExponent() { return exponent; }

        public void setConstant(float constant) { this.constant = constant; }
        public void setLinear(float linear) { this.linear = linear; }
        public void setExponent(float exponent) { this.exponent = exponent; }

    }

}
