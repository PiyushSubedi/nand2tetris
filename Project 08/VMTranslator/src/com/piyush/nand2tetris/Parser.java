package com.piyush.nand2tetris;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

class Parser {

    String currentCommand;

    private BufferedReader reader;

    Parser(String filename) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filename));
    }


    boolean hasMoreCommands() throws IOException {

        while((this.currentCommand = reader.readLine()) != null) {
            this.currentCommand = this.currentCommand.trim();

            // not comment or empty line
            if(!this.currentCommand.startsWith("//") && !this.currentCommand.isEmpty()){
                break;
            }
        }
        return this.currentCommand != null;
    }

    void advance() {
        // already advanced when checking for more commands!
        // nothing to do
    }


    AssemblyCommand.Type getCommandType() throws NoSuchFieldException {

        // cache?
        for(AssemblyCommand.Type type : AssemblyCommand.TYPE_PATTERN_MAP.keySet()) {
            if(this.currentCommand.matches(AssemblyCommand.TYPE_PATTERN_MAP.get(type))) {
                return type;
            }
        }

        throw new NoSuchFieldException("Unknown command");
    }



    String getArg1() {
        return this.currentCommand.split(" ")[1].trim();
    }

    int getArg2() {
        return Integer.valueOf(this.currentCommand.split(" ")[2].trim());
    }


    void close() throws IOException {
        reader.close();
    }
}
