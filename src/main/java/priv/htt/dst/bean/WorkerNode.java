package priv.htt.dst.bean;

import java.util.Objects;

public class WorkerNode {

	private Long sessionId;

	private Long workerId;

	private String ip;

	private Long updateTime;

	private Long createTime;

	public WorkerNode() {
	}

	public WorkerNode(Long sessionId, Long workerId,
					  String ip, Long updateTime, Long createTime) {
		this.sessionId = sessionId;
		this.workerId = workerId;
		this.ip = ip;
		this.updateTime = updateTime;
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "WorkerNode{" +
				"sessionId=" + sessionId +
				", workerId=" + workerId +
				", ip='" + ip + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WorkerNode that = (WorkerNode) o;
		return Objects.equals(sessionId, that.sessionId) &&
				Objects.equals(workerId, that.workerId) &&
				Objects.equals(ip, that.ip);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sessionId, workerId, ip);
	}

	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}

	public Long getWorkerId() {
		return workerId;
	}

	public void setWorkerId(Long workerId) {
		this.workerId = workerId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
}
