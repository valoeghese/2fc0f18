package tk.valoeghese.fc0.client.system;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class Camera {
	private Matrix4f view = new Matrix4f();
	private Vector3f pos = new Vector3f();
	private float pitch = 0.0f;
	private float yaw = 0.0f;

	public void translateScene(Vector3f translate) {
		this.pos = this.pos.add(translate);
		this.rebuildView();
	}

	public void rotateYaw(float yaw) {
		this.yaw += yaw;
		this.rebuildView();
	}

	public void rotatePitch(float pitch) {
		this.pitch += pitch;

		if (this.pitch < -NINETY_DEGREES) {
			this.pitch = -NINETY_DEGREES;
		} else if (this.pitch > NINETY_DEGREES) {
			this.pitch = NINETY_DEGREES;
		}
		this.rebuildView();
	}

	private void rebuildView() {
		this.view = new Matrix4f();
		this.view.rotate(new AxisAngle4f(this.yaw, 0.0f, 1.0f, 0.0f));
		this.view.rotate(new AxisAngle4f(this.pitch, -sin(this.yaw - NINETY_DEGREES), 0.0f, cos(this.yaw - NINETY_DEGREES)));
		this.view.translate(this.pos);
	}

	public void render(Model model, Matrix4f transform) {
		model.getShader().uniformMat4f("view", this.view);
		model.render(transform);
	}

	public float getYaw() {
		return this.yaw;
	}

	public float getPitch() {
		return this.pitch;
	}

	public void wrapYaw() {
		double twopi = 2 * Math.PI;

		while (this.yaw > twopi) {
			this.yaw -= twopi;
		}

		while (this.yaw < 0) {
			this.yaw += twopi;
		}
	}
	private static final float NINETY_DEGREES = (float) Math.toRadians(90);
}
