package tk.valoeghese.fc0.client.test;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import tk.valoeghese.fc0.BrandAndVersion;
import tk.valoeghese.fc0.client.Main;

import java.io.File;
import java.io.IOException;

// Because we don't package JGit in production, obviously
public class ClientTestMain {
	public static void main(String[] args) {
		long time = System.nanoTime();
		System.out.println("[Dev] Reading Local GIT Repository.");
		try {
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder.setGitDir(new File("./.git"))
					.readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.build();

			if (repository.getBranch() == null) {
				BrandAndVersion.setVersion("dev-nogit");
				System.out.println("[Dev] Found No GIT Repository in " + (System.nanoTime() - time) + " ns.");
			} else {
				ObjectId head = repository.resolve("HEAD");
				String branch = repository.getBranch();
				BrandAndVersion.setVersion(branch + "@" + Integer.toHexString(head.hashCode()));
				System.out.println("[Dev] Successfully Read Local GIT Repository in " + (System.nanoTime() - time) + " ms.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("[Dev] Failed to Read Local GIT Repository.");
		}

		Main.main(args);
	}
}