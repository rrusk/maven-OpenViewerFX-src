/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2017 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * PostscriptFactory.java
 * ---------------
 */

package org.jpedal.function;

import java.util.Arrays;

import org.jpedal.io.types.StreamReaderUtils;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.NumberUtils;


public class PostscriptFactory {

    static final int[] scale = {1, 26 * 26, 26 * 26 * 26, 26 * 26 * 26 * 26}; //alphabet so base 26 makes it unique

    static final double toBase10 = Math.log(10);

    private int level;

    private static final byte START_BRACE = 123;
    private static final byte END_BRACE = 125;

    private final byte[] stream;
    private final int streamLength;
    private int ptr;

    private static final boolean debug = false;

    protected boolean testingFunction;

    protected double[] stack = new double[100];
    private double[] safeStack;
    protected int[] stackType = new int[100];
    private int[] safeStackType;

    protected int stkPtr;
    private int safeStkPtr;
    protected int stkTypePtr;
    private int safeStkTypePrt;
    protected int currentType;

    boolean cont;

    /**
     * constant for conversion
     */
    static final double radiansToDegrees = 180f / Math.PI;

    //value for boolean true
    protected static final double isTrue = 1;

    // value for boolean false
    protected static final double isFalse = 0;

    // ----- PS types ------
    // PS intger (java int)
    protected static final int PS_INTEGER = 1;

    // PS real (java double)
    protected static final int PS_REAL = 2;

    // PS boolean (java boolean)
    protected static final int PS_BOOLEAN = 3;

    // left in just in case as a safety feature
    protected static final int PS_UNKNOWN = 0;


    public PostscriptFactory(final byte[] stream) {
        this.stream = stream; //raw data
        streamLength = stream.length;


        //System.out.println("-> PostScript STREAM data:");
        //System.out.println("");
        //for(int i=0;i<streamLength;i++){
        //System.out.print((char)stream[i]);
        //}
        //System.out.println("");

    }


    /**
     * main method to run the software as standalone application *
     * public static void main(String[] args) {
     * <p>
     * }
     * <p>
     * static{
     * <p>
     * String[] cmds = new String[]{
     * "abs","add","atan","ceiling","cos","cvi","cvr","div","exp","floor","idiv",
     * "ln","log","mod","mul","neg","sin","sqrt","sub","round", "truncate","and",
     * "bitshift","eq","false","ge","gt","le","lt","ne","not","or","true", "xor",
     * "if", "ifelse","copy","exch","pop","dup","index","roll"};
     * <p>
     * <p>
     * // generate a unique checksum for each based on first 4 characters
     * Map checkvalues=new HashMap();
     * <p>
     * <p>
     * int commandCount = cmds.length;
     * <p>
     * // build table of values
     * for(int i=0;i<commandCount;i++){
     * int key=0;
     * int keyLength=cmds[i].length();
     * if(keyLength>4)
     * keyLength=4;
     * <p>
     * for(int j=0;j<keyLength;j++)
     * key = key + (cmds[i].charAt(j)-(int)'a')*scale[j];
     * <p>
     * Integer mappedKey =new Integer(key);
     * <p>
     * if(checkvalues.containsKey(mappedKey)){
     * System.out.println("Duplicate on "+checkvalues.get(mappedKey)+" "+cmds[i]);
     * }
     * <p>
     * checkvalues.put(mappedKey,cmds[i]);
     * <p>
     * System.out.println("\n//unique id for "+cmds[i]);
     * <p>
     * <p>
     * if(cmds[i].length()>4)
     * System.out.println("final protected static int PS_"+cmds[i].substring(0,4)+" = "+mappedKey+";");
     * else
     * System.out.println("final protected static int PS_"+cmds[i]+" = "+mappedKey+";");
     * }
     * <p>
     * // build cases
     * <p>
     * System.out.println("//identify command\nprotected static int getCommandID(int value) {\nint id = -1;\nswitch (value) {");
     * for(int i=0;i<commandCount;i++){
     * int key=0;
     * int keyLength=cmds[i].length();
     * if(keyLength>3)
     * keyLength=3;
     * <p>
     * for(int j=0;j<keyLength;j++)
     * key = key + (cmds[i].charAt(j)-(int)'a')*scale[j];
     * <p>
     * if(cmds[i].length()>4){
     * System.out.println("case PS_"+cmds[i].substring(0,4)+":");
     * System.out.println("id=PS_"+cmds[i].substring(0,4)+";");
     * }else{
     * System.out.println("case PS_"+cmds[i]+":");
     * System.out.println("id=PS_"+cmds[i]+";");
     * }
     * System.out.println("break;");
     * }
     * <p>
     * System.out.println("\n}\nreturn id;\n}");
     * <p>
     * <p>
     * // build getString
     * <p>
     * //System.out.println("//identify command\nprotected static int getCommandID(int value) {\nint id = -1;\nswitch (value) {");
     * for(int i=0;i<commandCount;i++){
     * int key=0;
     * int keyLength=cmds[i].length();
     * if(keyLength>3)
     * keyLength=3;
     * <p>
     * for(int j=0;j<keyLength;j++)
     * key = key + (cmds[i].charAt(j)-(int)'a')*scale[j];
     * <p>
     * if(cmds[i].length()>4){
     * System.out.println("case PS_"+cmds[i].substring(0,4)+":");
     * }else{
     * System.out.println("case PS_"+cmds[i]+":");
     * }
     * System.out.println("str=\""+cmds[i]+"\";");
     * <p>
     * System.out.println("break;");
     * }
     * <p>
     * System.out.println("\n}\nreturn id;\n}");
     * }
     * <p>
     * /
     **/

    //unique id for abs
    protected static final int PS_abs = 317044;

    //unique id for add
    protected static final int PS_add = 54756;

    //unique id for atan
    protected static final int PS_atan = 5953532;

    //unique id for ceiling
    protected static final int PS_ceil = 5170050;

    //unique id for cos
    protected static final int PS_cos = 325834;

    //unique id for cvi
    protected static final int PS_cvi = 154806;

    //unique id for cvr
    protected static final int PS_cvr = 312990;

    //unique id for div
    protected static final int PS_div = 374507;

    //unique id for exp
    protected static final int PS_exp = 279192;

    //unique id for floor
    protected static final int PS_floo = 6651169;

    //unique id for idiv
    protected static final int PS_idiv = 9739140;

    //unique id for ln
    protected static final int PS_ln = 8799;

    //unique id for log
    protected static final int PS_log = 114931;

    //unique id for mod
    protected static final int PS_mod = 62204;

    //unique id for mul
    protected static final int PS_mul = 206868;

    //unique id for neg
    protected static final int PS_neg = 108173;

    //unique id for sin
    protected static final int PS_sin = 233914;

    //unique id for sqrt
    protected static final int PS_sqrt = 8992170;

    //unique id for sub
    protected static final int PS_sub = 31114;

    //unique id for round
    protected static final int PS_roun = 6301689;

    //unique id for truncate
    protected static final int PS_trun = 6303719;

    //unique id for and
    protected static final int PS_and = 61516;

    //unique id for bitshift
    protected static final int PS_bits = 8564921;

    //unique id for eq
    protected static final int PS_eq = 10820;

    //unique id for false
    protected static final int PS_fals = 8418909;

    //unique id for ge
    protected static final int PS_ge = 2710;

    //unique id for gt
    protected static final int PS_gt = 12850;

    //unique id for le
    protected static final int PS_le = 2715;

    //unique id for lt
    protected static final int PS_lt = 12855;

    //unique id for ne
    protected static final int PS_ne = 2717;

    //unique id for not
    protected static final int PS_not = 343421;

    //unique id for or
    protected static final int PS_or = 11506;

    //unique id for true
    protected static final int PS_true = 2190935;

    //unique id for xor
    protected static final int PS_xor = 308279;

    //unique id for if
    protected static final int PS_if = 3388;

    //unique id for ifelse
    protected static final int PS_ifel = 5100428;

    //unique id for copy
    protected static final int PS_copy = 11240530;

    //unique id for exch
    protected static final int PS_exch = 3249536;

    //unique id for pop
    protected static final int PS_pop = 273119;

    //unique id for dup
    protected static final int PS_dup = 277163;

    //unique id for index
    protected static final int PS_inde = 1889428;

    //unique id for roll
    protected static final int PS_roll = 5229553;

    //identify command
    protected static int getCommandID(final byte[] cmds) {

        //default no key value
        int id = -1;

        //build key with same formula we used to create Constants (we've checked they are unique)
        int key = 0;
        int keyLength = cmds.length;
        if (keyLength > 4) {
            keyLength = 4;
        }

        for (int j = 0; j < keyLength; j++) {
            key += (cmds[j] - 'a') * scale[j];
        }

        switch (key) {
            case PS_abs:
                id = PS_abs;
                break;
            case PS_add:
                id = PS_add;
                break;
            case PS_atan:
                id = PS_atan;
                break;
            case PS_ceil:
                id = PS_ceil;
                break;
            case PS_cos:
                id = PS_cos;
                break;
            case PS_cvi:
                id = PS_cvi;
                break;
            case PS_cvr:
                id = PS_cvr;
                break;
            case PS_div:
                id = PS_div;
                break;
            case PS_exp:
                id = PS_exp;
                break;
            case PS_floo:
                id = PS_floo;
                break;
            case PS_idiv:
                id = PS_idiv;
                break;
            case PS_ln:
                id = PS_ln;
                break;
            case PS_log:
                id = PS_log;
                break;
            case PS_mod:
                id = PS_mod;
                break;
            case PS_mul:
                id = PS_mul;
                break;
            case PS_neg:
                id = PS_neg;
                break;
            case PS_sin:
                id = PS_sin;
                break;
            case PS_sqrt:
                id = PS_sqrt;
                break;
            case PS_sub:
                id = PS_sub;
                break;
            case PS_roun:
                id = PS_roun;
                break;
            case PS_trun:
                id = PS_trun;
                break;
            case PS_and:
                id = PS_and;
                break;
            case PS_bits:
                id = PS_bits;
                break;
            case PS_eq:
                id = PS_eq;
                break;
            case PS_fals:
                id = PS_fals;
                break;
            case PS_ge:
                id = PS_ge;
                break;
            case PS_gt:
                id = PS_gt;
                break;
            case PS_le:
                id = PS_le;
                break;
            case PS_lt:
                id = PS_lt;
                break;
            case PS_ne:
                id = PS_ne;
                break;
            case PS_not:
                id = PS_not;
                break;
            case PS_or:
                id = PS_or;
                break;
            case PS_true:
                id = PS_true;
                break;
            case PS_xor:
                id = PS_xor;
                break;
            case PS_if:
                id = PS_if;
                break;
            case PS_ifel:
                id = PS_ifel;
                break;
            case PS_copy:
                id = PS_copy;
                break;
            case PS_exch:
                id = PS_exch;
                break;
            case PS_pop:
                id = PS_pop;
                break;
            case PS_dup:
                id = PS_dup;
                break;
            case PS_inde:
                id = PS_inde;
                break;
            case PS_roll:
                id = PS_roll;
                break;

        }
        return id;
    }

    /**
     * @param id execute commands or return -1 as error
     */
    @SuppressWarnings("OverlyLongMethod")
    protected int execute(final int id) {

        //will be reset by default if command unrecognised
        int returnValue = 0;

        //code taken from elsewhere so ignore

        // debug data

        int fType = 0;
        int sType = 0;

        int firstInt;

        double first;
        final double second;

        //ADD COMMANDS in AlphaBetical order!!!!
        switch (id) {

            case PostscriptFactory.PS_abs:

                // take val of the top of the stack
                first = pop();


                // put the absolute val back at the top
                if (first < 0) {
                    push(-first, PS_INTEGER);
                } else {
                    push(first, PS_INTEGER);
                }

                break;

            case PostscriptFactory.PS_add:

                // simple add

                // check if enough elements on the stack
                if (stack.length < 2) {
                    throw new RuntimeException("ADD - not enough elements on the stack");
                }


                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                if (fType == PS_REAL || sType == PS_REAL) {
                    push(first + second, PS_REAL);
                } else {
                    push(first + second, PS_INTEGER);
                }
                break;

            case PostscriptFactory.PS_and:

                //performs a bitwise and on int and booleans

                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                if (fType == PS_INTEGER && sType == PS_INTEGER) {
                    push((int) first & (int) second, PS_INTEGER);
                } else if (fType == PS_BOOLEAN && sType == PS_BOOLEAN) {
                    push((int) first & (int) second, PS_BOOLEAN);
                } else if (LogWriter.isRunningFromIDE) {
                    // should never happend, will exit
                    throw new RuntimeException("Critical error in PS_and");
                }

                break;

            case PostscriptFactory.PS_atan:

                calculateAtan();

                break;

            case PostscriptFactory.PS_bits:
                // bit shift operation times shift

                final int shift = popInt();
                fType = currentType;
                firstInt = popInt();
                sType = currentType;

                if (fType != PS_INTEGER || sType != PS_INTEGER) {
                    // should never happend, will exit
                    if (LogWriter.isRunningFromIDE) {
                        throw new RuntimeException("Critical error in PS_bits");
                    }
                }

                if (shift > 0) {
                    firstInt <<= shift;
                }

                if (shift < 0) {
                    firstInt >>= -shift;
                }


                push(firstInt, PS_INTEGER);

                break;

            case PostscriptFactory.PS_ceil:
                // get element of the stack

                first = pop();
                fType = currentType;
                // if negative, cast to int will strip the dec part
                if (first < 0) {
                    push((int) first, fType);
                } else {
                    final int temp = (int) first;

                    // has even the smallest dec part, round the number up
                    if (first > temp) {
                        push(temp + 1, fType);
                    } else {
                        push(first, fType);
                    }
                }

                break;

            case PostscriptFactory.PS_copy:
            /*
			 * In PS the function is designed to work with any object or
			 * type. Due to smaller amount of types and procedures implemented
			 * by adobe, we shall only implement one case (when int is a param).
			 */

                firstInt = popInt();
                fType = currentType;

                if (fType == PS_INTEGER && firstInt > 0) {
                    final double[] items = new double[firstInt];
                    final int[] types = new int[firstInt];

                    // take elements off
                    for (int i = 0; i < items.length; i++) {
                        items[i] = pop();
                        types[i] = currentType;
                    }

                    // put them back on (remember about the order)
                    for (int ii = items.length; ii > 0; ii--) {
                        push(items[ii - 1], types[ii - 1]);
                    }

                    // and now put the copied ones on top
                    for (int ii = items.length; ii > 0; ii--) {
                        push(items[ii - 1], types[ii - 1]);
                    }


                } else if (fType == PS_INTEGER && firstInt == 0) {
                    //no need to do anything
                } else if (LogWriter.isRunningFromIDE) {
                    // never expected to happend, while dealing with PDF
                    throw new RuntimeException("Critical error in PS_copy");
                }


                break;

            case PostscriptFactory.PS_cos:

                // calculates the cos of a given angle (angle given in deg)
                first = pop();

                // calc deg -> rad

                final double rad = (first / radiansToDegrees);

                double angle = Math.cos(rad);

                //allow for rounding error
                if (angle > 0 && angle < 0.0000001) {
                    angle = 0;
                } else if (angle < 0 && angle > -0.0000001) {
                    angle = 0;
                }

                //push(Math.cos(rad));

                push(angle, PS_REAL);

                break;

            case PostscriptFactory.PS_cvi:
                // convert to integer

                first = pop();

                push((int) first, PS_INTEGER);

                break;

            case PostscriptFactory.PS_cvr:

                // convert to a double.
                first = pop();

                push(first, PS_REAL);

                break;

            case PostscriptFactory.PS_div:

                // dividing, resutlt is a double (has decimal part)
                first = pop();
                second = pop();

                push(second / first, PS_REAL);

                break;

            case PostscriptFactory.PS_dup:

                calculateDup();

                break;

            case PostscriptFactory.PS_eq:
                // pushes true if the objects are equal, and false if they are not
                first = pop();
                second = pop();

                if (first == second) {
                    push(isTrue, PS_BOOLEAN);
                } else {
                    push(isFalse, PS_BOOLEAN);
                }

                break;

            case PostscriptFactory.PS_exch:

                //exchange the top 2 elements

                // check if enough elements on the stack
                if (stack.length < 2) {
                    throw new RuntimeException("EXCH - not enough elements on the stack");
                }

                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                push(first, fType);
                push(second, sType);


                break;

            case PostscriptFactory.PS_exp:

                // raises second to the ower of first.
                first = pop();
                second = pop();

                push(Math.pow(second, first), PS_REAL);

                break;

            case PostscriptFactory.PS_fals:

                //puts a false boolean value at the top of the stacks
                push(0, PS_BOOLEAN);

                break;

            case PostscriptFactory.PS_floo:

                // puts the lower value back on to the stack
                // 3.2 -> 3.0 , -4.8 -> -5.0

                first = pop();
                fType = currentType;

                if (first > 0) {
                    push((int) first, fType);
                } else {

                    final int temp = (int) first;

                    if (temp > first) {
                        push(temp - 1, fType);
                    } else {
                        push(first, fType);
                    }

                }

                break;

            case PostscriptFactory.PS_ge:

                // if the first operand is greater or equal than the other push true else false

                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                if ((fType == PS_INTEGER || fType == PS_REAL) && (sType == PS_INTEGER || sType == PS_REAL)) {
                    if (second >= first) {
                        push(1, PS_BOOLEAN);
                    } else {
                        push(0, PS_BOOLEAN);
                    }
                } else if (LogWriter.isRunningFromIDE) {
                    // should never happend, will exit
                    throw new RuntimeException("Critical error in PS_ge");
                }
                break;

            case PostscriptFactory.PS_gt:

                // if the first operand is greater than the other push true else false

                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                if ((fType == PS_INTEGER || fType == PS_REAL) && (sType == PS_INTEGER || sType == PS_REAL)) {
                    if (second > first) {
                        push(1, PS_BOOLEAN);
                    } else {
                        push(0, PS_BOOLEAN);
                    }
                } else if (LogWriter.isRunningFromIDE) {
                    // should never happend, will exit
                    throw new RuntimeException("Critical error in PS_gt");
                }
                break;

            case PostscriptFactory.PS_idiv:

                // like div but the result is striped of its dec part
                final int one = popInt();
                final int two = popInt();

                push((two / one), PS_INTEGER);

                break;

            case PostscriptFactory.PS_if:

			/*
			 * No examples found to properly test this method.
			 * According to doc first would recieve a instruction
			 * set, which execution depends on second being true(exec)
			 * or false(do not exec).
			 */

                if (!cont) {
                    System.arraycopy(safeStack, 0, stack, 0, 100);
                    System.arraycopy(safeStackType, 0, stackType, 0, 100);
                    this.stkPtr = safeStkPtr;
                    this.stkTypePtr = safeStkTypePrt;

                }

                cont = false;

                break;

            case PostscriptFactory.PS_inde:

                calculateIndex();

                break;

            case PostscriptFactory.PS_le:

                // if the first operand is less or equal than the other push true else false

                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                if ((fType == PS_INTEGER || fType == PS_REAL) && (sType == PS_INTEGER || sType == PS_REAL)) {
                    if (second <= first) {
                        push(1, PS_BOOLEAN);
                    } else {
                        push(0, PS_BOOLEAN);
                    }
                } else if (LogWriter.isRunningFromIDE) {
                    // should never happend, will exit
                    throw new RuntimeException("Critical error in PS_le");
                }
                break;

            case PostscriptFactory.PS_lt:
                // if the first operand is less than the other push true else false

                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                if ((fType == PS_INTEGER || fType == PS_REAL) && (sType == PS_INTEGER || sType == PS_REAL)) {
                    if (second < first) {
                        push(1, PS_BOOLEAN);
                    } else {
                        push(0, PS_BOOLEAN);
                    }
                } else if (LogWriter.isRunningFromIDE) {
                    // should never happend, will exit
                    throw new RuntimeException("Critical error in PS_lt");
                }
                break;

            case PostscriptFactory.PS_ln:

                // the same as log but the base is e (natural logarithm)
                first = pop();

                push(Math.log(first), PS_REAL);

                break;

            case PostscriptFactory.PS_log:

                // calculates the log bse 10 of the top element on the stack (takes it off)
                // and puts the res back on top
                first = pop();

                //push(Math.log10(first), PS_REAL);
                push(Math.log(first) / toBase10, PS_REAL);
                break;

            case PostscriptFactory.PS_mod:

                // get top two elements of the stack

                if (fType != PS_INTEGER || sType != PS_INTEGER) {
                    System.err.println("PS_mod - both values must be integers!");
                }

                first = pop();
                //fType = currentType;
                second = pop();
                //sType = currentType;


                // put result on stack
                push(second % first, PS_INTEGER);

                break;

            case PostscriptFactory.PS_mul:

                // multiply two top elements of the stack

                // check if enough elements on the stack
                if (stack.length < 2) {
                    throw new RuntimeException("MUL - not enough elements on the stack");
                }


                first = pop();
                second = pop();

			/*
			 *  If the result would happend to be outside of integer range
			 *  the PS_type should be set to real, for the time being I am leaving
			 *  it as integer though as the other situation is quite unliekly to
			 *  happend.
			 */

                push(first * second, PS_INTEGER);

                break;

            case PostscriptFactory.PS_ne:

                //pop two obj of the stack, if equal push FALSE else TRUE
                first = pop();
                second = pop();

                if (first != second) {
                    push(isTrue, PS_BOOLEAN);
                } else {
                    push(isFalse, PS_BOOLEAN);
                }

                break;

            case PostscriptFactory.PS_neg:

                //showStack();
                // if top of the stack is zero don't negate

                final double temp = pop();
                fType = currentType;
                if (temp != 0) {
                    push(-temp, fType);
                } else {
                    push(temp, fType);
                }
                //showStack();
                break;

            case PostscriptFactory.PS_not:

                first = pop();
                fType = currentType;

                //int type=getType(first);

                if (first == 0 && fType == PS_BOOLEAN) {
                    // assume is boolean
                    push(isTrue, PS_BOOLEAN);
                } else if (first == 1 && fType == PS_BOOLEAN) {
                    // assume is boolean
                    push(isFalse, PS_BOOLEAN);
                } else {
                    // push bitwise not
                    push(~(int) first, PS_INTEGER);
                }

                break;

            case PostscriptFactory.PS_or:

                // prefroms bitwise or, works with boolean and int

                first = pop();
                //fType = currentType;
                second = pop();
                //fType = currentType;

                if (fType == PS_BOOLEAN && sType == PS_BOOLEAN) {
                    push((int) first | (int) second, PS_BOOLEAN);
                } else if (fType == PS_INTEGER && sType == PS_INTEGER) {
                    push((int) first | (int) second, PS_INTEGER);
                } else if (LogWriter.isRunningFromIDE) {
                    // should never happend, will exit
                    throw new RuntimeException("Critical error in PS_or");
                }
                break;

            case PostscriptFactory.PS_pop:

                // discard the the element at the top of the stack
                pop();

                break;

            case PostscriptFactory.PS_roll:

                calculateRoll();

                break;

            case PostscriptFactory.PS_roun:

                // works almost like PS_floo

                first = pop();
                fType = currentType;

                first += 0.5f;

                if (first > 0) {
                    push((int) first, fType);
                } else {

                    final int tem = (int) first;

                    if (tem > first) {
                        push((tem - 1), fType);
                    } else {
                        push((int) first, fType);
                    }

                }


                break;

            case PostscriptFactory.PS_sin:

                // calc sin of a given angle
                first = pop();

                push(Math.sin(first / PostscriptFactory.radiansToDegrees), PS_REAL);

                break;

            case PostscriptFactory.PS_sqrt:

                first = pop();

                // make sure the number is not negative
                if (first >= 0) {
                    push(Math.sqrt(first), PS_REAL);
                } else {
                    System.err.println("SQRT - cant sqrt a negative number!");
                }

                break;

            case PostscriptFactory.PS_sub:

                // subtract the element at the top of the
                // stack form the one blow it.

                // check if enough elements on the stack
                if (stack.length < 2) {
                    throw new RuntimeException("SUB - not enough elements on the stack");
                }


                first = pop();
                fType = currentType;
                second = pop();
                sType = currentType;

                if (fType == PS_REAL || sType == PS_REAL) {
                    push(second - first, PS_REAL);
                } else {
                    push(second - first, PS_INTEGER);
                }

			/*
			 *  If the result would happend to be outside of integer range
			 *  the PS_type should be set to real, for the time being I am leaving
			 *  it as integer though as the other situation is quite unliekly to
			 *  happend.
			 */

                break;

            case PostscriptFactory.PS_trun:

                // strip the decimal part so
                // 3.2 -> 3.0  , -4.8 -> -4.0
                first = pop();
                fType = currentType;

                push((int) first, fType);

                break;

            case PostscriptFactory.PS_true:

                // puts a true boolean val at the top of the stack
                push(1, PS_BOOLEAN);

                break;

            case PostscriptFactory.PS_xor:

                firstInt = popInt();
                fType = currentType;
                final int secondInt = popInt();
                sType = currentType;

                if (fType == PS_BOOLEAN && sType == PS_BOOLEAN) {
                    push((firstInt ^ secondInt), PS_BOOLEAN);
                } else if (fType == PS_INTEGER && sType == PS_INTEGER) {
                    push((firstInt ^ secondInt), PS_INTEGER);
                } else if (LogWriter.isRunningFromIDE) {
                    // should never happend, will exit
                    throw new RuntimeException("Critical error in PS_xor");
                }

                break;

            //==============================
            //flag error
            default:
                returnValue = -1;
                break;

        }

        return returnValue;

    }


    private void calculateAtan() {
        final double first;
        final double second;
        first = pop();
        second = pop();

        // both params cant be zero!
        if (first == 0 && second == 0) {
            System.err.println("ATAN - invalid parameters");
        }

        // calc the tangent
        final double tangent = second / first;

        // depending on which quardrant in the x,y space we end up in
        //
        if (first >= 0 && second >= 0) {
            // 0 to 90 - 1st quadrant
            push(Math.toDegrees(Math.atan(tangent)), PS_REAL);
        } else if (first > 0 && second <= 0) {
            // 90 to 180 - 2nd quadrant
            double tmp = Math.toDegrees(Math.atan(tangent));

            if (tmp < 0) {
                tmp = -tmp;
            }

            push(tmp + 90, PS_REAL);
        } else if (first <= 0 && second <= 0) {
            // 180 to 270 - 3rd quadrant
            double tmp = Math.toDegrees(Math.atan(tangent));

            if (tmp < 0) {
                tmp = -tmp;
            }

            push(tmp + 180, PS_REAL);
        } else if (first <= 0 && second >= 0) {
            // 270 to 360 - 4th quadrant
            double tmp = Math.toDegrees(Math.atan(tangent));

            if (tmp < 0) {
                tmp = -tmp;
            }

            push(tmp + 270, PS_REAL);
        }
    }

    private void calculateDup() {
        // get duplicate and place it on the stack

        final double value = pop();
        final int type = currentType;
        push(value, type);
        push(value, type);
    }

    private void calculateIndex() {
        // get the n element
        final int n = popInt();

        if (n == 0) {
            calculateDup();
        } else if (n > 0) {

            final double[] temp;
            final int[] types;

            temp = new double[n];
            types = new int[n];

            // take n elements of the stack
            for (int i = 0; i < temp.length; i++) {
                temp[i] = pop();
                types[i] = currentType;
            }

            // copy the remaining one

            final double val = pop();
            final int fType = currentType;
            push(val, fType);

            //put rest back (allow for reverse order)
            for (int ii = temp.length; ii > 0; ii--) {
                push(temp[ii - 1], types[ii - 1]);
            }

            // put the copied one on top of the stack
            push(val, fType);

        } else if (n < 0) {
            System.err.println("-> Index : critical error, n has to be nonnegative");
        }
    }

    private void calculateRoll() {

        int amount = popInt();
        int numberOfElements = popInt();

        if (numberOfElements < 0 && LogWriter.isRunningFromIDE) {
            throw new RuntimeException("-> Roll : critical error");
        }

        // allow for case when rolling over a larger number than elements on stack
        if (numberOfElements > stkPtr) {
            numberOfElements = stkPtr;
        }

        if (amount > 0) {
            // top elements

            final double[] topTemp = new double[amount];
            final int[] topTypes = new int[amount];

            if (numberOfElements - amount <= 0) {
                return;
            }

            // bottom elements
            final double[] bottomTemp = new double[numberOfElements - amount];
            final int[] bottomTypes = new int[numberOfElements - amount];

            // take top elements off
            for (int i = 0; i < topTemp.length; i++) {
                topTemp[i] = pop();
                topTypes[i] = currentType;
            }

            // take bottom elements of the stack
            for (int y = 0; y < bottomTemp.length; y++) {
                bottomTemp[y] = pop();
                bottomTypes[y] = currentType;
            }

            // stack should be empty here

            //  put what was on top before first on the stack
            for (int ii = topTemp.length; ii > 0; ii--) {
                push(topTemp[ii - 1], topTypes[ii - 1]);
            }

            // put whats left back on top of the stk
            for (int yy = bottomTemp.length; yy > 0; yy--) {
                push(bottomTemp[yy - 1], bottomTypes[yy - 1]);
            }

        } else if (amount < 0) {

            amount = -amount;

            // top elements
            final double[] topTemp = new double[numberOfElements - amount];
            final int[] topTypes = new int[numberOfElements - amount];

            // bottom elements
            final double[] bottomTemp = new double[amount];
            final int[] bottomTypes = new int[amount];

            // take top elements off
            for (int i = 0; i < topTemp.length; i++) {
                topTemp[i] = pop();
                topTypes[i] = currentType;
            }

            // take bottom elements of the stack
            for (int y = 0; y < bottomTemp.length; y++) {
                bottomTemp[y] = pop();
                bottomTypes[y] = currentType;
            }

            // stack should be empty here

            //  put what was on top before first on the stack
            for (int ii = topTemp.length; ii > 0; ii--) {
                push(topTemp[ii - 1], topTypes[ii - 1]);
            }

            // put whats left back on top of the stk
            for (int yy = bottomTemp.length; yy > 0; yy--) {
                push(bottomTemp[yy - 1], bottomTypes[yy - 1]);
            }
        }

    }

    /**
     * work through Postscript stream reading commands and executing
     */
    public double[] executePostscript() {
        boolean firstBracket = false;

        //reset pointer to start in stream
        this.ptr = 0;

        if (debug) {
            System.out.println("-----stream data--------\n");
            for (int aa = 0; aa < streamLength; aa++) {
                System.out.print((char) stream[aa]);
            }
            System.out.print("<<<<");
            System.out.println("-------------\n");
        }

        level = 0; //regression level reached { increases and } decreases

        byte[] nextVal;

        //read next value
        while (ptr < streamLength) {

            //get key
            nextVal = getNextValue();

            //we have value so get Command ID
            if (nextVal != null) {

				/*
				 * execute command or read value
				 */
                if (nextVal.length == 1 && (nextVal[0] == START_BRACE || nextVal[0] == END_BRACE)) { //recursion

                    if (firstBracket && (nextVal[0] == START_BRACE)) {
                        final double i = pop();
                        final int fType = currentType;
                        safeStack = new double[100];
                        safeStackType = new int[100];
                        System.arraycopy(stack, 0, safeStack, 0, 100);
                        System.arraycopy(stackType, 0, safeStackType, 0, 100);
                        safeStkPtr = stkPtr;
                        safeStkTypePrt = stkTypePtr;
                        if (fType == PS_BOOLEAN) {
                            if (i > 0) {
                                cont = true;
                            }
                        } else {
                            throw new RuntimeException("Possible syntax error in PostScript stream!");
                        }

                    }
                    firstBracket = true;

                } else {

                    final int ID = getCommandID(nextVal);

                    if (ID == -1) { //read parameter and put on stack

                        try {
                            final double number = convertToDouble(nextVal);

                            if (debug) {
                                System.out.println("number=" + number);
                            }

                            //determine if it is a double or int
                            final int numberInt = (int) number;

                            if (numberInt == number) {
                                push(number, PS_INTEGER);
                            } else {
                                push(number, PS_REAL);
                            }

                        } catch (final Exception e) {
                            LogWriter.writeLog("Exception " + e);
                        }
                    } else { //execute commands
                        //System.out.println(" ID value : " + ID);
                        final int result = execute(ID);

                        if (result == -1 && LogWriter.isRunningFromIDE) {
                            throw new RuntimeException("Unsupported command with value " + PostscriptUtils.toString(ID));
                        }
                    }

                    //show stack
                    if (debug) {
                        final StringBuilder str = new StringBuilder("Stack now ");
                        for (int ii = 0; ii < stkPtr; ii++) {
                            str.append(stack[ii]).append(' ');
                        }
                        System.out.println(str);
                    }
                }
            }

            //should be set to 1 on first call as first entry should be {
            if (level == 0) {
                break;
            }

            //safety trap
            if (ptr >= streamLength) {
                break;
            }

        }

        return stack;

    }

    /**
     * put number on stack
     *
     * @param number
     */
    private void push(final double number, final int type) {

        if (stkPtr > 99 || stkTypePtr > 99) { //error
            if (LogWriter.isRunningFromIDE) {
                throw new RuntimeException("Stack or stackType overflow");
            }
        } else {
            stack[stkPtr] = number;
            stackType[stkTypePtr] = type;
        }

        stkPtr++;
        stkTypePtr++;

    }

    /**
     * take number from stack
     */
    @SuppressWarnings("UnusedAssignment")
    private double pop() {

        double value = 0;


        stkPtr--;

        stkTypePtr--;

        if (stkTypePtr < 0) {
            if (LogWriter.isRunningFromIDE) { //error
                throw new RuntimeException("Stack type underflow");
            }
        } else {
            currentType = stackType[stkTypePtr];
        }

        if (stkPtr < 0) { //error
            if (LogWriter.isRunningFromIDE) {
                throw new RuntimeException("Stack underflow");
            }

        } else {
            value = stack[stkPtr];
        }


        return value;

    }


    /**
     * take number from stack
     */
    private int popInt() {
        return (int) pop();
    }

    /**
     * convert byteStream to double primitive
     *
     * @return
     */
    private static double convertToDouble(final byte[] stream) {

        final double d;

        final int start = 0;
        final int charCount = stream.length;

        int ptr = charCount;
        int intStart = 0;
        boolean isMinus = false;
        //hand optimised float code
        //find decimal point
        for (int j = charCount - 1; j > -1; j--) {
            if (stream[start + j] == 46) { //'.'=46
                ptr = j;
                break;
            }
        }

        int intChars = ptr;
        //allow for minus
        if (stream[start] == 43) { //'+'=43
            intChars--;
            intStart++;
        } else if (stream[start] == 45) { //'-'=45
            //intChars--;
            intStart++;
            isMinus = true;
        }

        //optimisations
        final int intNumbers = intChars - intStart;
        int decNumbers = charCount - ptr;

        if ((intNumbers > 3)) { //non-optimised to cover others
            isMinus = false;

            d = Double.parseDouble(new String(stream));

        } else {

            if (decNumbers > 6) { //old code used this accuracy so kept to avoid lots of minor changes
                decNumbers = 6;
            }

            d = NumberUtils.convertStreamFromDouble(stream, start + intStart, start + ptr, intNumbers, decNumbers);
        }

        if (isMinus) {
            return -d;
        } else {
            return d;
        }
    }

    private byte[] getNextValue() {

        final int start;
        final int end;
        int next;
        byte[] returnValue = null;

        ptr = StreamReaderUtils.skipSpaces(stream, ptr);

        //log start
        start = ptr;

        //find end
        while (ptr < streamLength) {

            next = stream[ptr];

            if (next == START_BRACE) {
                break;
            }

            ptr++;

            if (ptr >= streamLength) {
                break;
            }

            next = stream[ptr];

            if (next == 32 || next == 13 || next == 10 || next == START_BRACE || next == END_BRACE) {
                break;
            }

        }

        //track level of recusrion and increment on start to make loop roll on
        if (stream[start] == START_BRACE) {
            ptr++;
            level++;
        } else if (stream[start] == END_BRACE) {
            level--;
        }

        end = ptr;

        //put value into array to return for further processing
        if (end >= start) {

            //strip and excess zeros from numbers (ie 1.00000000000 becomes 1)
//			while(end-start>1 && (stream[end-1]=='0' || stream[end-1]=='.')) {
//                end--;
//            }

            final int len = end - start;
            returnValue = new byte[len];
            System.arraycopy(stream, start, returnValue, start - start, end - start);

        }

        if (debug) {
            System.out.print(">>>>>>>> ");
            for (int aa = start; aa < end; aa++) {
                System.out.print((char) stream[aa]);
            }

            System.out.println(" <<<");
        }

        return returnValue;
    }


    /**
     * put input values onto stack for processing
     *
     * @param values
     */
    public void resetStacks(final float[] values) {

        //rest stack
        //may need to change structure- try for double
        Arrays.fill(stack, 0);
        Arrays.fill(stackType, 0);
        stkPtr = 0;
        stkTypePtr = 0;
//		for(int ii=0;ii<100;ii++) {
//                    stack[ii] = 0d;
//                }
//
//		for(int iii=0;iii<100;iii++) {
//                    stackType[iii] = 0;
//                }
        for (final float value : values) {

            if (debug) {
                System.out.println("Added to stack " + value + " count=" + values.length);
            }
            push(value, PS_REAL);
        }

    }

}
