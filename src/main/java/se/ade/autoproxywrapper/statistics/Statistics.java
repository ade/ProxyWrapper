package se.ade.autoproxywrapper.statistics;

import java.io.Serializable;
import java.time.LocalDate;

public class Statistics implements Serializable{

	private LocalDate date;
	private long bytesSent = 0;
	private long bytesReceived = 0;

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public long getBytesSent() {
		return bytesSent;
	}

	public long getBytesReceived() {
		return bytesReceived;
	}

	public void addBytesSent(long bytes) {
		this.bytesSent += bytes;
	}

	public void addBytesReceived(long bytes) {
		this.bytesReceived += bytes;
	}

	void setBytesSent(long bytesSent) {
		this.bytesSent = bytesSent;
	}

	void setBytesReceived(long bytesReceived) {
		this.bytesReceived = bytesReceived;
	}
}
