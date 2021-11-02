package tk.valoeghese.fc0;

import java.util.Optional;

public final class BrandAndVersion {
	private static Optional<String> brand = Optional.empty();
	private static Optional<String> version = Optional.empty();

	public static Optional<String> getBrand() {
		return brand;
	}

	public static Optional<String> getVersion() {
		return version;
	}

	public static boolean isModded() {
		return brand.isPresent();
	}

	public static void setBrand(String brand_) {
		brand = Optional.of(brand_);
	}

	public static void setVersion(String version_) {
		version = Optional.of(version_);
	}
}
