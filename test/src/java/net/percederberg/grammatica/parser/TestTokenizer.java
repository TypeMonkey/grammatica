/*
 * TestTokenizer.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * As a special exception, the copyright holders of this library give
 * you permission to link this library with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is
 * not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 *
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.grammatica.parser;

import java.io.StringReader;

import junit.framework.TestCase;

/**
 * A test case for the Tokenizer class.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.4
 */
public class TestTokenizer extends TestCase {

    /**
     * The end of file token identifier.
     */
    private static final int EOF = 0;

    /**
     * The keyword token identifier.
     */
    private static final int KEYWORD = 1;

    /**
     * The number token identifier.
     */
    private static final int NUMBER = 2;

    /**
     * The whitespace token identifier.
     */
    private static final int WHITESPACE = 3;

    /**
     * The error token identifier.
     */
    private static final int ERROR = 4;

    /**
     * Test various invalid patterns.
     */
    public void testInvalidPattern() {
        Tokenizer     tokenizer = createTokenizer("");
        TokenPattern  pattern;

        pattern = new TokenPattern(NUMBER,
                                   "NUMBER",
                                   TokenPattern.REGEXP_TYPE + 13,
                                   "13");
        failAddPattern(tokenizer, pattern);
        pattern = new TokenPattern(NUMBER,
                                   "NUMBER",
                                   TokenPattern.REGEXP_TYPE,
                                   "1(3");
        failAddPattern(tokenizer, pattern);
    }

    /**
     * Tests the tokenizer with empty input.
     */
    public void testEmptyInput() {
        Tokenizer  tokenizer = createDefaultTokenizer("");

        readToken(tokenizer, EOF);
    }

    /**
     * Tests the ignored tokens.
     */
    public void testIgnoreTokens() {
        Tokenizer  tokenizer = createDefaultTokenizer(" 12 keyword 0 ");

        readToken(tokenizer, NUMBER);
        readToken(tokenizer, KEYWORD);
        readToken(tokenizer, NUMBER);
        readToken(tokenizer, EOF);
    }

    /**
     * Tests the ignored tokens.
     */
    public void testErrorTokens() {
        Tokenizer  tokenizer = createDefaultTokenizer("12 error1  ");

        readToken(tokenizer, NUMBER);
        failReadToken(tokenizer);
        readToken(tokenizer, NUMBER);
        readToken(tokenizer, EOF);
    }

    /**
     * Test the parse error recovery.
     */
    public void testParseError() {
        Tokenizer  tokenizer = createDefaultTokenizer("12 (keyword)");

        readToken(tokenizer, NUMBER);
        failReadToken(tokenizer);
        readToken(tokenizer, KEYWORD);
        failReadToken(tokenizer);
        readToken(tokenizer, EOF);
    }

    /**
     * Tests the token list functions.
     */
    public void testTokenList() {
        Tokenizer  tokenizer = createDefaultTokenizer("12 keyword 0");
        Token      token;

        assertEquals("default token list setting",
                     false,
                     tokenizer.getUseTokenList());
        tokenizer.setUseTokenList(true);
        token = readToken(tokenizer, NUMBER);
        readToken(tokenizer, KEYWORD);
        readToken(tokenizer, NUMBER);
        readToken(tokenizer, EOF);
        assertEquals("previous token", null, token.getPreviousToken());
        token = token.getNextToken();
        assertEquals("token id", WHITESPACE, token.getId());
        token = token.getNextToken();
        assertEquals("token id", KEYWORD, token.getId());
        token = token.getNextToken();
        assertEquals("token id", WHITESPACE, token.getId());
        token = token.getNextToken();
        assertEquals("token id", NUMBER, token.getId());
        assertEquals("next token", null, token.getNextToken());
        token = token.getPreviousToken();
        assertEquals("token id", WHITESPACE, token.getId());
        token = token.getPreviousToken();
        assertEquals("token id", KEYWORD, token.getId());
        token = token.getPreviousToken();
        assertEquals("token id", WHITESPACE, token.getId());
        token = token.getPreviousToken();
        assertEquals("token id", NUMBER, token.getId());
    }

    /**
     * Creates a new tokenizer.
     *
     * @param input          the input string
     *
     * @return a new tokenizer
     */
    private Tokenizer createTokenizer(String input) {
        return new Tokenizer(new StringReader(input));
    }

    /**
     * Creates a new default tokenizer that recognizes a trivial
     * language.
     *
     * @param input          the input string
     *
     * @return a new tokenizer
     */
    private Tokenizer createDefaultTokenizer(String input) {
        Tokenizer     tokenizer = createTokenizer(input);
        TokenPattern  pattern;

        pattern = new TokenPattern(KEYWORD,
                                   "KEYWORD",
                                   TokenPattern.STRING_TYPE,
                                   "keyword");
        addPattern(tokenizer, pattern);
        pattern = new TokenPattern(NUMBER,
                                   "NUMBER",
                                   TokenPattern.REGEXP_TYPE,
                                   "[0-9]+");
        addPattern(tokenizer, pattern);
        pattern = new TokenPattern(WHITESPACE,
                                   "WHITESPACE",
                                   TokenPattern.REGEXP_TYPE,
                                   "[ \t\n]+");
        pattern.setIgnore();
        addPattern(tokenizer, pattern);
        pattern = new TokenPattern(ERROR,
                                   "ERROR",
                                   TokenPattern.STRING_TYPE,
                                   "error");
        pattern.setError();
        addPattern(tokenizer, pattern);

        return tokenizer;
    }

    /**
     * Adds a pattern to the tokenizer and reports a test failure if
     * it failed.
     *
     * @param tokenizer      the tokenizer
     * @param pattern        the pattern to add
     */
    private void addPattern(Tokenizer tokenizer, TokenPattern pattern) {
        try {
            tokenizer.addPattern(pattern);
        } catch (ParserCreationException e) {
            fail("couldn't add pattern " + pattern.getName() + ": " +
                 e.getMessage());
        }
    }

    /**
     * Adds a pattern to the tokenizer and reports a test failure if
     * it failed.
     *
     * @param tokenizer      the tokenizer
     * @param pattern        the pattern to add
     */
    private void failAddPattern(Tokenizer tokenizer, TokenPattern pattern) {
        try {
            tokenizer.addPattern(pattern);
            fail("could add pattern " + pattern.getName());
        } catch (ParserCreationException e) {
            // Failure was expected
        }
    }

    /**
     * Reads the next token. This method reports a test failure if a
     * token couldn't be read.
     *
     * @param tokenizer      the tokenizer to use
     *
     * @return the token read
     */
    private Token readToken(Tokenizer tokenizer) {
        try {
            return tokenizer.next();
        } catch (ParseException e) {
            fail("couldn't read next token: " + e.getMessage());
            return null; // Unreachable
        }
    }

    /**
     * Reads the next token and checks it's id. This method reports a
     * test failure if the right token couldn't be read.
     *
     * @param tokenizer      the tokenizer to use
     * @param id             the expected token id
     *
     * @return the token read
     */
    private Token readToken(Tokenizer tokenizer, int id) {
        Token  token = readToken(tokenizer);

        if (id == EOF) {
            if (token != null) {
                fail("expected end of file, found " + token);
            }
        } else {
            if (token != null) {
                assertEquals("token id", id, token.getId());
            } else {
                fail("expected " + id + ", found EOF");
            }
        }
        return token;
    }

    /**
     * Fails to read the next token. This method reports a test
     * failure if a token could be read.
     *
     * @param tokenizer      the tokenizer to use
     */
    private void failReadToken(Tokenizer tokenizer) {
        Token  token;

        try {
            token = tokenizer.next();
            fail("could read token " + token.toString());
        } catch (ParseException e) {
            // Failure was expected
        }
    }
}
