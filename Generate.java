/*
 * Copyright (c) 22/2/2018 This class was created by Orber J.
 */

import java.io.*;
public class Generate extends AbstractGenerate {
    private StringWriter errors = new StringWriter();
    public Generate() {
        // Constructor
    }

    @Override
    public void reportError(Token token, String explanatoryMessage) throws CompilationException{
        printStackTrace(explanatoryMessage, new CompilationException(explanatoryMessage));
    }

    public void printStackTrace(String explanatoryMessage, CompilationException exception) throws CompilationException{
        String message = explanatoryMessage + "\tStack Trace:\t"+ errors + "\t";
        exception.printStackTrace(new PrintWriter(errors));
        throw new CompilationException(message);
    }
}
