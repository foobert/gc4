package net.funkenburg.gc.backend.web;

import lombok.Data;

@Data
public class RequestForm {
    private String location;
    private int distance = 10000;
}
