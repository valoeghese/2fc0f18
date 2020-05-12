package tk.valoeghese.fc0.client.system;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Camera {
	private Matrix4f view = new Matrix4f();

	public void translate(Vector3f translate) {
		this.view = this.view.translate(translate);
	}

	public void rotate(AxisAngle4f rotation) {
		this.view = this.view.rotate(rotation);
	}

	public void scale(Vector3f scale) {
		this.view = this.view.scale(scale);
	}

	public void render(Model model, Matrix4f transform) {
		model.getShader().uniformMat4f("view", this.view);
		model.render(transform);
	}
}
