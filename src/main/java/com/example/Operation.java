package com.example;

import java.util.List;

public class Operation {
    private String operation;
    private String command;
    private List<String> args;

    public Operation() {
    }

    public Operation(String operation, String command, List<String> args) {
        this.operation = operation;
        this.command = command;
        this.args = args;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}
