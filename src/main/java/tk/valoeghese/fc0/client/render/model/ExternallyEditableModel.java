package tk.valoeghese.fc0.client.render.model;

public interface ExternallyEditableModel {
	void addTriangle(int i0, int i1, int i2);
	int addVertex(float x, float y, float z, float u, float v, float light);
}
