package com.myapp.client.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CheckListItem {

    private String id;
    private String status;
    private String reasoning;

    public CheckListItem() {}

    public CheckListItem(String id, String status, String reasoning) {
        this.id = id;
        this.status = status;
        this.reasoning = reasoning;
    }


    @Override
    public String toString() {
        return id + "\n" + status.toUpperCase() + "\nREASONING: " + reasoning;
    }


    public String getId() { return id; }
    public String getStatus() { return status; }
    public String getReasoning() { return reasoning; }

    public void setId(String id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

}
