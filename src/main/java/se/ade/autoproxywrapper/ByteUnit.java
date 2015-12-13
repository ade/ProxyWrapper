package se.ade.autoproxywrapper;

public enum ByteUnit {

	BYTE(1, 1000L, "B"),
	KILOBYTE(BYTE.maxSize, BYTE.maxSize * 1000L, "KB"),
	MEGABYTE(KILOBYTE.maxSize, KILOBYTE.maxSize * 1000L, "MB"),
	GIGABYTE(MEGABYTE.maxSize, MEGABYTE.maxSize * 1000L, "GB"),
	TERABYTE(GIGABYTE.maxSize, GIGABYTE.maxSize * 1000L, "TB"),
	PETABYTE(TERABYTE.maxSize, TERABYTE.maxSize * 1000L, "PB");

	private long minSize;
	private long maxSize;
	private String representation;

	ByteUnit(long minSize, long maxSize, String representation) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.representation = representation;
	}

	public static ByteUnit getUnitForValue(long value) {
		for (ByteUnit byteUnit : values()) {
			if (value < byteUnit.getMaxSize()) {
				return byteUnit;
			}
		}
		return null;
	}

	public long getMinSize() {
		return minSize;
	}

	public long getMaxSize() {
		return this.maxSize;
	}

	public String getRepresentation() {
		return representation;
	}
}
