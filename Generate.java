/*
 * Copyright (c) 22/2/2018 This class was created by Orber J.
 */

public class Generate extends AbstractGenerate {
    /**
     * Empty Constructor for generate
     * Which is a code generator, extended from AbstractGenerate.
     * */
    public Generate(){}

    /**
     * If an error occurs during code generation this method is called to report the error.
     * I am overriding it and passing the token that caused the error and the explanatory message
     * @param token
     * @param explanatoryMessage
     * @throws CompilationException
     */

    @Override
    public void reportError(Token token, String explanatoryMessage) throws CompilationException {
        System.out.println(new CompilationException(explanatoryMessage));
        throw new CompilationException(explanatoryMessage);
    }
}
