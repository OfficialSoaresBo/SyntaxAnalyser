/*
 * Copyright (c) 22/2/2018 This class was created by Orber J.
 */

import java.io.IOException;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser{

    /**
     * needed to know what file is being compiled
     */
    private String filename;
    /**
     * SyntaxAnalyser constructor, takes file name as parameter and instantiates lex(Lexical Analyser on the file)
     * @param filename - name of file to be loaded
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     */
    public SyntaxAnalyser(String filename) throws IOException {
        this.filename = filename.substring(16);
        lex = new LexicalAnalyser(filename);
    }

    /**
     * Begin processing the first (top level) token.
     * Start the statement
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    @Override
    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<statement part>");
        acceptTerminal(Token.beginSymbol);
        try{
            listStatement();
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<statement part>", nextToken));
        }
        acceptTerminal(Token.endSymbol);
        myGenerate.finishNonterminal("<statement part>");
    }

    /**
     * Accepts a token based on it's content so if the symbol of the token matches a symbol defined in the token class
     * then the generator will insert it as a terminal and lex will get next token
     * If not then it will report an error saying "Token couldn't be identified"
     * @param symbol
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    @Override
    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        if(nextToken.symbol == symbol){
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
        }else{
            myGenerate.reportError(nextToken, "line number " + nextToken.lineNumber + " in " + filename + ":"+ " token (" + Token.getName(nextToken.symbol) + ").\n");
        }
    }

    /**
     * Creates a list of statements, so if statements, while statements etc.
     * Until you have reached a semi colon which then indicates the end of the statement
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void listStatement() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("<statement list>");
        try{
            statement();
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("list of statements", nextToken));
        }

        while(nextToken.symbol == Token.semicolonSymbol){
            acceptTerminal(Token.semicolonSymbol);
            try{
                statement();
            }catch (CompilationException e) {
                myGenerate.reportError(nextToken,formatError("semicolonSymbol", nextToken));
            }
        }
        myGenerate.finishNonterminal("<statement list>");
    }

    /**
     * Check the token symbol and depending on the token symbol then call the respective function
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void statement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<statement>");
        try{
            switch(nextToken.symbol){
                case Token.identifier:
                    assignmentStatement();
                    break;
                case Token.ifSymbol:
                    ifStatement();
                    break;
                case Token.whileSymbol:
                    whileStatement();
                    break;
                case Token.callSymbol:
                    procedureStatement();
                    break;
                case Token.doSymbol:
                    untilStatement();
                    break;
                case Token.forSymbol:
                    forStatement();
                    break;
                default:
                    myGenerate.reportError(nextToken,formatError("statement: <identifier>, <if>, <while>, <call>, <do> or <for>",nextToken));
                    break;
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("statement: <identifier>, <if>, <while>, <call>, <do> or <for>", nextToken));
        }
        myGenerate.finishNonterminal("<statement>");
    }

    /**
     * This method is called when the token contains an assignment statement and will add it respectively
     * If the following token contains a constant then this one will also be accepted
     * if not then run te expression method.
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void assignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<assignment statement>");
        try{
            switch(nextToken.symbol){
                case Token.identifier:
                    acceptTerminal(Token.identifier);
                    acceptTerminal(Token.becomesSymbol);
                    if(nextToken.symbol == Token.stringConstant){
                        acceptTerminal(Token.stringConstant);
                    }else{
                        expression();
                    }
                    break;
                default:
                    myGenerate.reportError(nextToken,formatError("<identifier>",nextToken));
                    break;
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<identifier>", nextToken));
        }
        myGenerate.finishNonterminal("<assignment statement>");
    }

    /**
     * Method called when the token contains an if statement, then accept it and run the condition method
     * after the condition has been accepted then call the list method once again to check for integrity of the list
     * If the next symbol is now an else statement also accept it since else statements must follow an if statement
     * Call the list again and if the the next token now contains an end symbol then accept it, and then accept the if statement following it
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void ifStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<if statement>");

        acceptTerminal(Token.ifSymbol);
        try{
            condition();
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<if>", nextToken));
        }
        acceptTerminal(Token.thenSymbol);

        try{
            listStatement();
            if(nextToken.symbol == Token.endSymbol){
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.ifSymbol);
            }else{
                acceptTerminal(Token.elseSymbol);
                listStatement();
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.ifSymbol);
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<end>", nextToken));
        }
        myGenerate.finishNonterminal("<if statement>");
    }

    /**
     * Similar to the ifStatement method but checks if the token is a while symbol if so then accepts it otherwise rejects it
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void whileStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<while statement>");
        try{
            if(nextToken.symbol == Token.whileSymbol){
                acceptTerminal(Token.whileSymbol);
                condition();
                acceptTerminal(Token.loopSymbol);
                listStatement();
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.loopSymbol);
            }else{
                myGenerate.reportError(nextToken,formatError("<while>",nextToken));
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<while>", nextToken));
        }

        myGenerate.finishNonterminal("<while statement>");
    }

    /**
     * checks if token is a procedure statement and accepts it, otherwise return error
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void procedureStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<procedure statement>");
        try{
            if(nextToken.symbol == Token.callSymbol){
                acceptTerminal(Token.callSymbol);
                acceptTerminal(Token.identifier);
                acceptTerminal(Token.leftParenthesis);
                listArgument();
                acceptTerminal(Token.rightParenthesis);
            }else{
                myGenerate.reportError(nextToken,formatError("<call> , <identifier>, <(> , <)>",nextToken));
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<call> , <identifier>, <(> , <)>", nextToken));
        }
        myGenerate.finishNonterminal("<procedure statement>");
    }

    /**
     * checks if token is an 'until statement' and accepts it, otherwise return error
     * @throws CompilationException - exception thrown to report errors during Compilation
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     */
    private void untilStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("<until statement>");
        try{
            if(nextToken.symbol == Token.doSymbol){
                acceptTerminal(Token.doSymbol);
                listStatement();
                acceptTerminal(Token.untilSymbol);
                condition();
            }else{
                myGenerate.reportError(nextToken,formatError("<until>",nextToken));
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<until>", nextToken));
        }
        myGenerate.finishNonterminal("<until statement>");
    }

    /**
     * checks if token is an 'for statement' and accepts it, otherwise return error
     */
    private void forStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("<for statement>");
        try{
            if (nextToken.symbol == Token.forSymbol) {
                acceptTerminal(Token.forSymbol);
                while (nextToken.symbol == Token.leftParenthesis)
                {
                    acceptTerminal(Token.leftParenthesis);
                    assignmentStatement();
                    acceptTerminal(Token.semicolonSymbol);
                    condition();
                    acceptTerminal(Token.semicolonSymbol);
                    assignmentStatement();
                    acceptTerminal(Token.rightParenthesis);
                }
                acceptTerminal(Token.doSymbol);
                listStatement();
                acceptTerminal(Token.endSymbol);
                acceptTerminal(Token.loopSymbol);
            } else {
                myGenerate.reportError(nextToken,formatError("<for>",nextToken));
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<for>", nextToken));
        }

        myGenerate.finishNonterminal("<for statement>");
    }

    /**
     * checks if token is a condition and accepts it, otherwise return error
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void condition() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<condition>");
        try{
            if(nextToken.symbol == Token.identifier){
                acceptTerminal(Token.identifier);
                conditionalOperator();
                switch(nextToken.symbol){
                    case Token.identifier:
                        acceptTerminal(Token.identifier);
                        break;
                    case Token.numberConstant:
                        acceptTerminal(Token.numberConstant);
                        break;
                    case Token.stringConstant:
                        acceptTerminal(Token.stringConstant);
                        break;
                    default:
                        // It's handled outside in the else.
                        break;
                }
            }else{
                myGenerate.reportError(nextToken,formatError("<identifier>, <numberConstant> or <stringConstant>",nextToken));
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<identifier>, <numberConstant> or <stringConstant>", nextToken));
        }
        myGenerate.finishNonterminal("<condition>");
    }

    /**
     * checks if token is a conditional operator and accepts it, otherwise return error
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    public void conditionalOperator() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("<conditional operator>");

        try{
            switch(nextToken.symbol){
                case Token.greaterThanSymbol:
                    acceptTerminal(Token.greaterThanSymbol);
                    break;
                case Token.greaterEqualSymbol:
                    acceptTerminal(Token.greaterEqualSymbol);
                    break;
                case Token.equalSymbol:
                    acceptTerminal(Token.equalSymbol);
                    break;
                case Token.notEqualSymbol:
                    acceptTerminal(Token.notEqualSymbol);
                    break;
                case Token.lessThanSymbol:
                    acceptTerminal(Token.lessThanSymbol);
                    break;
                case Token.lessEqualSymbol:
                    acceptTerminal(Token.lessEqualSymbol);
                    break;
                default:
                    myGenerate.reportError(nextToken,formatError("<'greaterThanSymbol'>, <'greaterEqualSymbol'>, <'equalSymbol'>, <'notEqualSymbol'>, <'lessThanSymbol'> or <'lessEqualSymbol'>",nextToken));
                    break;
            }
        }catch (CompilationException e) {
            myGenerate.reportError(nextToken,formatError("<'greaterThanSymbol'>, <'greaterEqualSymbol'>, <'equalSymbol'>, <'notEqualSymbol'>, <'lessThanSymbol'> or <'lessEqualSymbol'>", nextToken));
        }


        myGenerate.finishNonterminal("<conditional operator>");
    }

    /**
     * checks if token is a list argument and accepts it, otherwise return error
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void listArgument() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("<argument list>");
        acceptTerminal(Token.identifier);
        try{
            while(nextToken.symbol == Token.commaSymbol){
                acceptTerminal(Token.commaSymbol);
                listArgument();
            }
        }catch (CompilationException e) {
            throw new CompilationException(formatError("<,>", nextToken), e);
        }

        myGenerate.finishNonterminal("<argument list>");
    }

    /**
     * checks if token is an expression and accepts it, otherwise return error
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void expression() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("<expression>");
        try{
            term();
            while(nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol){
                switch(nextToken.symbol){
                    case Token.plusSymbol:
                        acceptTerminal(Token.plusSymbol);
                        term();
                        break;
                    case Token.minusSymbol:
                        acceptTerminal(Token.minusSymbol);
                        term();
                        break;
                    default:
                        myGenerate.reportError(nextToken,formatError("<+> or <->",nextToken));
                        break;
                }
            }
        }catch (CompilationException e) {
            throw new CompilationException(formatError("<+> or <->", nextToken), e);
        }


        myGenerate.finishNonterminal("<expression>");
    }

    /**
     * checks if token is a term and accepts it, otherwise return error
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void term() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("<term>");
        try{
            factor();
            while(nextToken.symbol == Token.timesSymbol || nextToken.symbol == Token.divideSymbol){
                switch(nextToken.symbol){
                    case Token.timesSymbol:
                        acceptTerminal(Token.timesSymbol);
                        term();
                        break;
                    case Token.divideSymbol:
                        acceptTerminal(Token.divideSymbol);
                        term();
                        break;
                    default:
                        myGenerate.reportError(nextToken,formatError("<*> or </>",nextToken));
                        break;
                }
            }
        }catch (CompilationException e) {
            throw new CompilationException(formatError("<*> or </>", nextToken), e);
        }
        myGenerate.finishNonterminal("<term>");
    }

    /**
     * checks if token is a factor and accepts it, otherwise return error
     * @throws IOException - InputOutput exception to detect if something went wrong with the file
     * @throws CompilationException - exception thrown to report errors during Compilation
     */
    private void factor() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("<factor>");
        try{
            switch(nextToken.symbol){
                case Token.identifier:
                    acceptTerminal(Token.identifier);
                    break;
                case Token.numberConstant:
                    acceptTerminal(Token.numberConstant);
                    break;
                case Token.leftParenthesis:
                    acceptTerminal(Token.leftParenthesis);
                    expression();
                    acceptTerminal(Token.rightParenthesis);
                    break;
                default:
                    myGenerate.reportError(nextToken,formatError("<identifier>, <number constant>, <(> or <)>",nextToken));
                    break;
            }
        }catch (CompilationException e) {
            throw new CompilationException(formatError("<identifier>, <number constant>, <(> or <)>", nextToken), e);
        }

        myGenerate.finishNonterminal("<factor>");
    }

    /**
     * Formats error before returning it
     * @param expected
     * @param token
     * @return
     */
    private String formatError(String expected, Token token) {
        return "line number " + token.lineNumber + " in " + filename + ":\n\t- Looking for " + expected + " but found (" + Token.getName(token.symbol) + ").\n";
    }
}
