package tk.valoeghese.fc0.client.system;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Camera {
	private Matrix4f view = new Matrix4f();
	private Vector3f pos = new Vector3f();
	private float pitch = 0.0f;
	private float yaw = 0.0f;

	public void translateScene(Vector3f translate) {
		this.pos = this.pos.add(translate);
		this.rebuildView();
	}

	public void setRotateYaw(float yaw) {
		this.yaw = yaw;
		this.rebuildView();
	}

	public void setRotatePitch(float pitch) {
		this.pitch = pitch;
		this.rebuildView();
	}

	private void rebuildView() {
		this.view = new Matrix4f();
		this.view.rotate(new AxisAngle4f(this.yaw, 0.0f, 1.0f, 0.0f));
		this.view.rotate(new AxisAngle4f(this.pitch, 1.0f, 0.0f, 0.0f));
		this.view.translate(this.pos);
	}

	public void render(Model model, Matrix4f transform) {
		model.getShader().uniformMat4f("view", this.view);
		model.render(transform);
	}
}
