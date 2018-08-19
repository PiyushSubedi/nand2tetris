package com.piyush.nand2tetris;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

class CodeWriter {

    private PrintWriter writer;
    private AssemblyWriter aw;
    private String inputFileName;

    CodeWriter(String fileName) throws IOException {
        this.writer = new PrintWriter(new FileWriter(fileName));
        this.aw = new AssemblyWriter(this.writer);
    }

    void setFileName(String fileName) {
        this.inputFileName = fileName;
    }


    void writeArithmetic(String command) {

        aw.comment(command + " operation");

        addLogic(command);

        aw.addEmptyLine();
    }



    void writePushPop(AssemblyCommand.Type commandType, String segment, int index) {

        if(commandType.equals(AssemblyCommand.Type.PUSH)) {
            writePush(segment, index);
        } else if(commandType.equals(AssemblyCommand.Type.POP)) {
            writePop(segment, index);
        }

    }


    void writeBranching(String command, String label) {

        if(AssemblyCommand.LABEL.equals(command)) {
            // declaration
            aw.addLabel(label);
        } else if(AssemblyCommand.GOTO.equals(command)) {
            // unconditional jump
            aw.jumpTo(label);
        } else if(AssemblyCommand.IF_GOTO.equals(command)) {
            // unconditional jump
            aw.select("SP");
            aw.add("A=M-1");
            aw.add("D=M");
            aw.select(label);
            aw.add("D;JNE");
            aw.addEmptyLine();
            aw.reset();
        }
    }

    void close() {
        this.aw.end();
        this.writer.close();
    }



    private void addLogic(String command) {

        if(AssemblyCommand.isUnaryCommand(command)) {

            aw.select("SP");
            aw.add("A=M-1");
            aw.add(command.equals(AssemblyCommand.NEGATE) ? "M=-M" : "M=!M");

        } else {

            aw.pop();

            aw.add("D=M");
            aw.select("SP");
            aw.add("A=M-1");

            if(AssemblyCommand.isBooleanCommand(command)) {
                Map<String, String> commandMap = new HashMap<>();
                commandMap.put(AssemblyCommand.EQUAL, "JEQ");
                commandMap.put(AssemblyCommand.GREATER_THAN, "JGT");
                commandMap.put(AssemblyCommand.LESS_THAN, "JLT");

                aw.add("D=M-D");

                String jumpLbl = aw.makeUnique("SET_TRUE");

                aw.select(jumpLbl);
                aw.add("D;" + commandMap.get(command));
                aw.select("SP");
                aw.add("A=M-1");
                aw.add("M=0");
                aw.skipAndJump(3);

                aw.addLabel(jumpLbl);
                aw.select("SP");
                aw.add("A=M-1");
                aw.add("M=-1");

            } else {
                Map<String, String> commandMap = new HashMap<>();
                commandMap.put(AssemblyCommand.ADD, "+");
                commandMap.put(AssemblyCommand.SUBTRACT, "-");
                commandMap.put(AssemblyCommand.BITWISE_AND, "&");
                commandMap.put(AssemblyCommand.BITWISE_OR, "|");

                aw.add("M=M" + commandMap.get(command) + "D");
            }

        }
    }


    private void writePush(String segment, int index) {

        aw.comment(String.format("push %s %d", segment, index));

        if(AssemblyCommand.CONSTANT.equals(segment)) {
            aw.select("" + index);
            aw.add("D=A");

        } else if(AssemblyCommand.MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.containsKey(segment)) {

            String segmentName = AssemblyCommand.MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.get(segment);

            if(AssemblyCommand.STATIC.equals(segment)) {
                segmentName = String.format(segmentName, this.inputFileName, index);
            }
            aw.select(segmentName);

            if(segment.equals(AssemblyCommand.TEMP)){
                aw.add("D=A");
            }else {
                aw.add("D=M");
            }

            aw.select("" + index);
            aw.add("A=D+A");
            aw.add("D=M");
        } else if(AssemblyCommand.POINTER.equals(segment)) {

            // *SP = THIS/THAT; SP++

            String thisOrThat = index == 0 ? AssemblyCommand.THIS : AssemblyCommand.THAT;

            aw.select(AssemblyCommand.MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.get(thisOrThat));
            aw.add("D=M");
        }

        // push the value
        aw.push();
        aw.addEmptyLine();
    }

    private void writePop(String segment, int index) {

        aw.comment(String.format("pop %s %d", segment, index));

        if(AssemblyCommand.MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.containsKey(segment)) {

            String segmentName = AssemblyCommand.MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.get(segment);

            if(AssemblyCommand.STATIC.equals(segment)) {
                segmentName = String.format(segmentName, this.inputFileName, index);
            }
            aw.select(segmentName);

            if(segment.equals(AssemblyCommand.TEMP)){
                aw.add("D=A");
            }else {
                aw.add("D=M");
            }

            aw.select("" + index);
            aw.add("D=D+A");
            aw.select("addr");
            aw.add("M=D");

            aw.select("SP");
            aw.add("M=M-1");
            aw.add("A=M");
            aw.add("D=M");
            aw.select("addr");
            aw.add("A=M");
            aw.add("M=D");

        } else if(AssemblyCommand.POINTER.equals(segment)) {

            // *SP = THIS/THAT; SP++

            String thisOrThat = index == 0 ? AssemblyCommand.THIS : AssemblyCommand.THAT;

            aw.pop();
            aw.add("D=M");
            aw.select(AssemblyCommand.MEMORY_SEGMENT_ASSEMBLY_NAME_MAP.get(thisOrThat));
            aw.add("M=D");
        }

        aw.addEmptyLine();
    }



    private class AssemblyWriter
    {
        PrintWriter writer;

        int tab;

        int linesWritten;

        private final int tabLength = 1;

        AssemblyWriter(PrintWriter writer) {
            this.writer = writer;
            this.tab = 0;
            this.linesWritten = 0;
        }


        void pop() {
            select("SP");
            print("M=M-1");
            print("A=M");
        }

        void push() {
            select("SP");
            add("M=M+1");
            add("A=M-1");
            add("M=D");
        }



        void comment(String text) {
            print("// " + text, false);
        }

        void add(String text) {
            print(text);
        }


        String makeUnique(String label) {
            return label + "_" + this.linesWritten;
        }

        void addLabel(String label) {
            label = label.toUpperCase();
            print("(" + label + ")", false);
            tab();
        }

        void addEmptyLine() {
            //deTab();
            writer.println();
        }

        void select(String ref) {
            print("@" + ref);
        }

        void jumpTo(String label) {
            select(label);
            add("0;JMP");
            reset();
        }

        void skipAndJump(int noOfInsToSkip) {
            jumpTo(String.valueOf(this.linesWritten + noOfInsToSkip + 2)); // 2 for actual jump ins
        }

        void end() {
            addLabel("END");
            print("@END");
            print("0;JMP");
        }

        void reset() {
            deTab();
        }

        private void tab() {
            this.tab+=tabLength;
        }

        private void deTab() {
            this.tab = this.tab > tabLength ? this.tab - this.tabLength : 0;
        }

        private void print(String text) {
            print(text, true);
        }

        private void print(String text, Boolean addLineNumber) {
            for(int i=0;i<tab; i++) writer.printf("\t");
            writer.printf("%s", text);
            writer.println();

            if(addLineNumber){
                this.linesWritten++;
            }
        }
    }
}
