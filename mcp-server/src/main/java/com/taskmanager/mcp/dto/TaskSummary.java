package com.taskmanager.mcp.dto;

import java.util.Map;

public class TaskSummary {

    private long total;
    private Map<String, Long> byStatus;

    public TaskSummary() {
    }

    public TaskSummary(long total, Map<String, Long> byStatus) {
        this.total = total;
        this.byStatus = byStatus;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Map<String, Long> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<String, Long> byStatus) {
        this.byStatus = byStatus;
    }
}
