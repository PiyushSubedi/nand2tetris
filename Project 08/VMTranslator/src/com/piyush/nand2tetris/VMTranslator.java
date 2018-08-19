package com.piyush.nand2tetris;

import java.io.IOException;
import java.nio.file.Paths;

public class VMTranslator {

    public static void main(String[] args) throws IOException, NoSuchFieldException {

        if(args.length == 0) {
            throw new IllegalArgumentException("Please select a vm file to translate");
        }

        String inputFile = args[0];
        String fileName = Paths.get(inputFile).getFileName().toString();
        String outputFile = inputFile.replaceAll(".vm", ".asm");

        Parser pr = new Parser(inputFile);

        CodeWriter cw = new CodeWriter(outputFile);

        while(pr.hasMoreCommands()) {

            pr.advance();

            if(pr.getCommandType().equals(AssemblyCommand.Type.ARITHMETIC)) {

                cw.writeArithmetic(pr.currentCommand);

            } else if(pr.getCommandType().equals(AssemblyCommand.Type.PUSH) ||
                      pr.getCommandType().equals(AssemblyCommand.Type.POP)) {

                cw.writePushPop(pr.getCommandType(), pr.getArg1(), pr.getArg2());

            } else if(pr.getCommandType().equals(AssemblyCommand.Type.BRANCHING)){

                cw.writeBranching(pr.currentCommand.split(" ")[0], pr.getArg1());
            }


        }

        pr.close();
        cw.close();

        System.out.println("Translation complete");
    }
}
