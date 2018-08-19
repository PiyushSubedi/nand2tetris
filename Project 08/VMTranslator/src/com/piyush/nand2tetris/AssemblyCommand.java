package com.piyush.nand2tetris;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold all assembly commands as strings
 */
final class AssemblyCommand {

    enum Type { ARITHMETIC, PUSH, POP, BRANCHING }

    // Arithmetic Commands
    static final String ADD = "add",
                        SUBTRACT = "sub",
                        NEGATE = "neg",
                        EQUAL = "eq",
                        GREATER_THAN = "gt",
                        LESS_THAN = "lt",
                        BITWISE_AND = "and",
                        BITWISE_OR = "or",
                        BITWISE_NOT = "not";

    // Memory Manipulation Commands
    static final String PUSH = "push",
                        POP = "pop";


    // Branching Commands
    static final String LABEL = "label",
                        IF_GOTO = "if-goto",
                        GOTO = "goto";

    // Memory Segments
    static final String LOCAL = "local",
                        ARGUMENT = "argument",
                        THIS = "this",
                        THAT = "that",
                        CONSTANT = "constant",
                        STATIC = "static",
                        TEMP = "temp",
                        POINTER = "pointer";


    final static Map<Type, String> TYPE_PATTERN_MAP;
    final static Map<String, String> MEMORY_SEGMENT_ASSEMBLY_NAME_MAP;

    static
    {
        TYPE_PATTERN_MAP = new HashMap<>();
        TYPE_PATTERN_MAP.put(Type.ARITHMETIC, String.format("^%s|%s|%s|%s|%s|%s|%s|%s|%s$",
                    ADD, SUBTRACT, NEGATE, EQUAL, GREATER_THAN, LESS_THAN, BITWISE_AND, BITWISE_OR, BITWISE_NOT
                ));
        TYPE_PATTERN_MAP.put(Type.PUSH, String.format("^%s.*$", PUSH));
        TYPE_PATTERN_MAP.put(Type.POP, String.format("^%s.*$", POP));
        TYPE_PATTERN_MAP.put(Type.BRANCHING, String.format("^(%s|%s|%s)\\s.*$", LABEL, IF_GOTO, GOTO));


        MEMORY_SEGMENT_ASSEMBLY_NAME_MAP = new HashMap<>();
        MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.put(LOCAL, "LCL");
        MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.put(ARGUMENT, "ARG");
        MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.put(THIS, "THIS");
        MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.put(THAT, "THAT");
        MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.put(TEMP, "5");
        MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.put(STATIC, "%s.%d");
    }


    static Boolean isBooleanCommand(String command) {
        return command.equals(EQUAL) || command.equals(LESS_THAN) || command.equals(GREATER_THAN);
    }

    static Boolean isUnaryCommand(String command) {
        return command.equals(NEGATE) || command.equals(BITWISE_NOT);
    }

}
