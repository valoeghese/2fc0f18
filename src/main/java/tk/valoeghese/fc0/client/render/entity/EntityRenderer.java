package tk.valoeghese.fc0.client.render.entity;

import valoeghese.scalpel.Model;

public abstract class EntityRenderer {
	private Model model;

	public Model getOrCreateModel() {
		if (this.model == null) {
			this.model = this.createModel();
		}

		return this.model;
	}

	protected abstract Model createModel();
}
