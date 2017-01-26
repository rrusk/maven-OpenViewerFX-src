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
 * PostScriptCompiler.java
 * ---------------
 */



package org.jpedal.function;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

//class will be used in future for speed execution
public class PostScriptCompiler {

    private static final int C_ABS = 96370;
    private static final int C_ADD = 96417;
    private static final int C_ATAN = 3004320;
    private static final int C_CEILING = 660387005;
    private static final int C_COS = 98695;
    private static final int C_CVI = 98902;
    private static final int C_CVR = 98911;
    private static final int C_DIV = 99473;
    private static final int C_EXP = 100893;
    private static final int C_FLOOR = 97526796;
    private static final int C_IDIV = 3227528;
    private static final int C_LN = 3458;
    private static final int C_LOG = 107332;
    private static final int C_MOD = 108290;
    private static final int C_MUL = 108484;
    private static final int C_NEG = 108944;
    private static final int C_SIN = 113880;
    private static final int C_SQRT = 3538208;
    private static final int C_SUB = 114240;
    private static final int C_ROUND = 108704142;
    private static final int C_TRUNCATE = 1852984678;
    private static final int C_AND = 96727;
    private static final int C_BITSHIFT = 1125715861;
    private static final int C_EQ = 3244;
    private static final int C_FALSE = 97196323;
    private static final int C_GE = 3294;
    private static final int C_GT = 3309;
    private static final int C_LE = 3449;
    private static final int C_LT = 3464;
    private static final int C_NE = 3511;
    private static final int C_NOT = 109267;
    private static final int C_OR = 3555;
    private static final int C_TRUE = 3569038;
    private static final int C_XOR = 118875;
    private static final int C_IF = 3357;
    private static final int C_IFELSE = -1191590954;
    private static final int C_COPY = 3059573;
    private static final int C_EXCH = 3127384;
    private static final int C_POP = 111185;
    private static final int C_DUP = 99839;
    private static final int C_INDEX = 100346066;
    private static final int C_ROLL = 3506301;

    private static final int T_COMMAND = 1;
    private static final int T_NUMBER = 2;
    private static final int T_BOOLEAN = 3;
    private static final int T_SBRACE = 123; //start brace
    private static final int T_EBRACE = 125; //end brace

    private static final double radToDegrees = 180f / Math.PI;
    private static final double toBase10=Math.log(10);

    private static final byte[] CHAR256 = {
        //      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, A, B, C, D, E, F,
        1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, // 0
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 1
        1, 0, 0, 0, 0, 2, 0, 0, 2, 2, 0, 0, 0, 0, 0, 2, // 2
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 2, 0, 2, 0, // 3
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 4
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, // 5
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 6
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 0, // 7
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 8
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 9
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // A
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // B
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // C
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // D
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // E
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 // F    
    };

    private final double[] dValues = new double[1000]; //hold default values
    private final int[] dTypes = new int[1000]; //hold default types

    private final double[] cValues ;
    private final int[] cTypes ;

    private int sp;//stream pointer
    private int dp;//default pointer
    private int cp;//cur list pointer

    private final Stack<Boolean> braces = new Stack<Boolean>();

    public PostScriptCompiler(final byte[] stream) {
        parseStream(stream);
        cValues = new double[Math.max(20, dp+10)];
        cTypes = new int[cValues.length];
    }

    private void parseStream(byte[] stream) {
        int len = stream.length;
        while (sp < len && dp < dValues.length) {
            int cc = stream[sp++] & 0xff;
            if (cc == T_SBRACE) {
                if (!braces.empty()) {
                    pushDefault(T_SBRACE, T_SBRACE);
                }
                braces.push(Boolean.TRUE);
            } else if (cc == T_EBRACE) {
                braces.pop();
                if (!braces.empty()) {
                    pushDefault(T_EBRACE, T_EBRACE);
                }
            } else if (isDigit(cc) || isFullStop(cc) || cc == 43 || cc == 45) {
                StringBuilder sb = new StringBuilder();
                while (sp < len) {
                    if (isWhiteSpace(cc) || isDelimiter(cc)) {
                        sp--;
                        break;
                    } else {
                        sb.append((char) cc);
                        cc = stream[sp++] & 0xff;
                    }
                }
                pushDefault(Double.parseDouble(sb.toString()), T_NUMBER);
            } else if (cc > 96 && cc < 123) {
                StringBuilder sb = new StringBuilder();
                while (sp < len) {
                    if (isWhiteSpace(cc) || isDelimiter(cc)) {
                        sp--;
                        break;
                    } else {
                        sb.append((char) cc);
                        cc = stream[sp++] & 0xff;
                    }
                }
                int com = getCommand(sb.toString());
                if (com != -1) {
                    pushDefault(com, T_COMMAND);
                }
            }
        }
       
    }

    private static int getCommand(String s) {
        switch (s.hashCode()) {
            case C_ABS:
                return C_ABS;
            case C_ADD:
                return C_ADD;
            case C_ATAN:
                return C_ATAN;
            case C_CEILING:
                return C_CEILING;
            case C_COS:
                return C_COS;
            case C_CVI:
                return C_CVI;
            case C_CVR:
                return C_CVR;
            case C_DIV:
                return C_DIV;
            case C_EXP:
                return C_EXP;
            case C_FLOOR:
                return C_FLOOR;
            case C_IDIV:
                return C_IDIV;
            case C_LN:
                return C_LN;
            case C_LOG:
                return C_LOG;
            case C_MOD:
                return C_MOD;
            case C_MUL:
                return C_MUL;
            case C_NEG:
                return C_NEG;
            case C_SIN:
                return C_SIN;
            case C_SQRT:
                return C_SQRT;
            case C_SUB:
                return C_SUB;
            case C_ROUND:
                return C_ROUND;
            case C_TRUNCATE:
                return C_TRUNCATE;
            case C_AND:
                return C_AND;
            case C_BITSHIFT:
                return C_BITSHIFT;
            case C_EQ:
                return C_EQ;
            case C_FALSE:
                return C_FALSE;
            case C_GE:
                return C_GE;
            case C_GT:
                return C_GT;
            case C_LE:
                return C_LE;
            case C_LT:
                return C_LT;
            case C_NE:
                return C_NE;
            case C_NOT:
                return C_NOT;
            case C_OR:
                return C_OR;
            case C_TRUE:
                return C_TRUE;
            case C_XOR:
                return C_XOR;
            case C_IF:
                return C_IF;
            case C_IFELSE:
                return C_IFELSE;
            case C_COPY:
                return C_COPY;
            case C_EXCH:
                return C_EXCH;
            case C_POP:
                return C_POP;
            case C_DUP:
                return C_DUP;
            case C_INDEX:
                return C_INDEX;
            case C_ROLL:
                return C_ROLL;
        }
        return -1;
    }

    private void pushDefault(double value, int type) {
        dValues[dp] = value;
        dTypes[dp] = type;
        dp++;
    }

    private static boolean isWhiteSpace(int ch) {
        return CHAR256[ch] == 1;
    }

    private static boolean isDigit(int ch) {
        return CHAR256[ch] == 4;
    }

    private static boolean isFullStop(int ch) {
        return ch == 46;
    }

    public static boolean isDelimiter(int ch) {
        return CHAR256[ch] == 2;
    }

    private void executeCommand(int cmd) {
        switch (cmd) {
            case C_ABS:
                C_ABS();
                break;
            case C_ADD:
                C_ADD();
                break;
            case C_ATAN:
                C_ATAN();                
                break;
            case C_CEILING:
                C_CEILING();
                break;
            case C_COS:
                C_COS_OR_SIN();
                break;
            case C_CVI:
                C_CVI();
                break;
            case C_CVR:
                C_CVR();
                break;
            case C_DIV:
                C_DIV();
                break;
            case C_EXP:
                C_EXP();
                break;
            case C_FLOOR:
                C_FLOOR();
                break;
            case C_IDIV:
                C_IDIV();
                break;
            case C_LN:
                C_LN();
                break;
            case C_LOG:
                C_LOG();
                break;
            case C_MOD:
                C_MOD();
                break;
            case C_MUL:
                C_MUL();
                break;
            case C_NEG:
                C_NEG();
                break;
            case C_SIN:
                C_COS_OR_SIN();
                break;
            case C_SQRT:
                C_SORT();
                break;
            case C_SUB:
                C_SUB();
                break;
            case C_ROUND:
                C_ROUND();
                break;
            case C_TRUNCATE:
                C_TRUNCATE();
                break;
            case C_AND:
                C_AND();
                break;
            case C_BITSHIFT:
                C_BITSHIFT();
                break;
            case C_EQ:
                C_EQ();
                break;
            case C_FALSE:
                C_FALSE();
                break;
            case C_GE:
                C_GE();
                break;
            case C_GT:
                C_GT();
                break;
            case C_LE:
                C_LE();
                break;
            case C_LT:
                C_LT();
                break;
            case C_NE:
                C_NE();
                break;
            case C_NOT:
                C_NOT();
                break;
            case C_OR:
                C_OR();
                break;
            case C_TRUE:
                C_TRUE();
                break;
            case C_XOR:
                C_XOR();
                break;
            case C_IF:                
                C_IF();                
                break;
            case C_IFELSE:
                C_IFELSE();
                break;
            case C_COPY:
                C_COPY();
                break;
            case C_EXCH:
                C_EXCH();
                break;
            case C_POP:
                popItem();
                break;
            case C_DUP:
                C_DUP();
                break;
            case C_INDEX:
                C_INDEX();
                break;
            case C_ROLL:
                C_ROLL();                
                break;
        }
    }


    private void C_ABS() {
        final double[] first = popItem();
        cValues[cp] = Math.abs(first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }

    private void C_ATAN() {
        final double[] first = popItem();
        final double[] second = popItem();
        double tt = 0;
        final double tangent = second[0] / first[0];
        if (first[0] >= 0 && second[0] >= 0) {
            tt = Math.toDegrees(Math.atan(tangent));
        } else if (first[0] > 0 && second[0] <= 0) {
            double tmp = Math.abs(Math.toDegrees(Math.atan(tangent)));
            tt = tmp + 90;
        } else if (first[0] <= 0 && second[0] <= 0) {
            double tmp = Math.abs(Math.toDegrees(Math.atan(tangent)));
            tt = tmp + 180;
        } else if (first[0] <= 0 && second[0] >= 0) {
            double tmp = Math.abs(Math.toDegrees(Math.atan(tangent)));
            tt = tmp + 270;
        }
        cValues[cp] = tt;
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_ADD() {
        final double[] first = popItem();
        final double[] second = popItem();
        cValues[cp] = first[0] + second[0];
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_CEILING() {
        final double[] first = popItem();
        cValues[cp] = Math.ceil(first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_COS_OR_SIN() {
        final double[] first = popItem();
        cValues[cp] = Math.sin(first[0] / radToDegrees);
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_CVI() {
        final double[] first = popItem();        
        cValues[cp] = (int) first[0];
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_CVR() {
        final double[] first = popItem();        
        cValues[cp] = first[0];
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_DIV() {
        final double[] first = popItem();
        final double[] second = popItem();
        cValues[cp] = second[0] / first[0];
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    
    private void C_EXP() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = Math.pow(second[0],first[0]);
        cTypes[cp] = (int) first[1];
    }
    
    
    private void C_FLOOR() {
        final double[] first = popItem();
        cValues[cp] = Math.floor(first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }
        
    private void C_IDIV() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = ((int) second[0]) / ((int) first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }   
    
    private void C_LN() {
        final double[] first = popItem();        
        cValues[cp] = Math.log(first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }    
    
    private void C_LOG() {
        final double[] first = popItem();        
        cValues[cp] = Math.log(first[0])/toBase10;
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_MOD() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = ((int) second[0]) % ((int) first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }
       
    private void C_MUL() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = first[0] * second[0];
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_NEG() {
        final double[] first = popItem();        
        cValues[cp] = -first[0];
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    
    private void C_SORT() {
        final double[] first = popItem();        
        cValues[cp] = Math.sqrt(first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_SUB() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = second[0] - first[0];
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_ROUND() {
        final double[] first = popItem();        
        cValues[cp] = Math.round(first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }
    
    private void C_TRUNCATE() {
        final double[] first = popItem();        
        cValues[cp] = (int)(first[0]);
        cTypes[cp] = (int) first[1];
        cp++;
    }
        
    private void C_AND() {
        final double[] first = popItem();
        final double[] second = popItem();        
        if(first[1] == T_NUMBER && second[1] == T_NUMBER){
            cValues[cp] = ((int)second[0]) & ((int)first[0]);
            cTypes[cp] = T_NUMBER;
        }else{
            cValues[cp] = first[0] == second[0] ? 1 : 0 ;
            cTypes[cp] = T_BOOLEAN;
        }
        cp++;
    }

    private void C_BITSHIFT() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = ((int)second[0])<<((int)first[0]);
        cTypes[cp] = (int)first[1];
        cp++;
    }
     
    private void C_EQ() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = first[0] == second[0] ? 1 : 0;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
    
    private void C_FALSE() {
        cValues[cp] = 0;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
    
    private void C_GE() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = second[0] >= first[0] ? 1 : 0;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
    
    private void C_GT() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = second[0] > first[0] ? 1 : 0;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
    
    private void C_LE() {
        final double[] first = popItem();
        final double[] second = popItem();
        cValues[cp] = second[0] <= first[0] ? 1 : 0;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
    
    private void C_LT() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = second[0] < first[0] ? 1 : 0;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
    
    private void C_NE() {
        final double[] first = popItem();
        final double[] second = popItem();        
        cValues[cp] = second[0] != first[0] ? 1 : 0;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
        
    private void C_NOT() {
        final double[] first = popItem();
        if(first[1] == T_NUMBER){
            cValues[cp] = ~(int)first[0];
        }else{
            cValues[cp] = first[0] != 1 ? 1 : 0;
        }
        cTypes[cp] = (int)first[1];
        cp++;
    }
    
    private void C_OR() {
        final double[] first = popItem();
        final double[] second = popItem();        
        if(first[1] == T_NUMBER && second[1] == T_NUMBER){
            cValues[cp] = ((int)second[0]) | ((int)first[0]);
            cTypes[cp] = T_NUMBER;
        }else{
            cValues[cp] = first[0]==1 || second[0]==1 ? 1 : 0 ;
            cTypes[cp] = T_BOOLEAN;
        }
        cp++;
    }
    
    private void C_TRUE() {
        cValues[cp] = 1;
        cTypes[cp] = T_BOOLEAN;
        cp++;
    }
    
    private void C_XOR() {
        final double[] first = popItem();
        final double[] second = popItem();
        cValues[cp] = ((int)second[0]) ^ ((int)first[0]);
        if(first[1] == T_NUMBER && second[1] == T_NUMBER){
            cTypes[cp] = T_NUMBER;
        }else{
            cTypes[cp] = T_BOOLEAN;
        }
        cp++;
    }
    
    private void C_IF() {
        final int old = dp;
        final double[] first = popItem();        
        if (first[0] == 1) {
            while (dTypes[dp] != T_SBRACE && dp > 0) {
                dp--;
            }
            dp++;
            executeInterval(old);
        }
        dp = old;
    }
    
    private void C_IFELSE() {
        final int old = dp;
        final double[] first = popItem();
        //todo
        if (first[0] == 1) {
            int br = 0;
            while(dp > 0){
                int cdp = dTypes[dp--];
                if(cdp == T_SBRACE){
                    br++;
                }else if(cdp == T_EBRACE){
                    br--;
                }
                if(cdp == T_SBRACE && br == 0){
                    break;
                }
            }
            int end = dp;
            br = 0;
            while(dp > 0){
                int cdp = dTypes[dp--];
                if(cdp == T_SBRACE){
                    br++;
                }else if(cdp == T_EBRACE){
                    br--;
                }
                if(cdp == T_SBRACE && br == 0){
                    break;
                }
            }
            dp+=2;
            executeInterval(end);
        } else {
            int br = 0;
            while(dp > 0){
                int cdp = dTypes[dp--];
                if(cdp == T_SBRACE){
                    br++;
                }else if(cdp == T_EBRACE){
                    br--;
                }
                if(cdp == T_SBRACE && br == 0){
                    break;
                }
            }
            dp+=2;
            executeInterval(old);
        }
        dp = old;
    }
    
    private void C_COPY() {
        final double[] first = popItem();
        final int n;
        if (first[0] > 0) {
            n = (int) first[0];
            double[] values = new double[n];
            int[] types = new int[n];
            System.arraycopy(cValues, cValues.length - n, values, 0, n);
            System.arraycopy(cTypes, cValues.length - n, types, 0, n);
            for (int i = 0; i < n; i++) {
                cValues[cp] = values[0];
                cTypes[cp] =  types[1];
                cp++;
            }
        }
    }
    
    private void C_EXCH() {
        final double[] first = popItem();
        final double[] second = popItem();
        cValues[cp] = first[0];
        cTypes[cp] = (int)first[1];
        cp++;
        cValues[cp] = second[0];
        cTypes[cp] = (int)second[1];
        cp++;
    }
    
    private void C_DUP() {
        final double[] first = popItem();
        cValues[cp] = first[0];
        cTypes[cp] = (int)first[1];
        cp++;
        cValues[cp] = first[0];
        cTypes[cp] = (int)first[1];
        cp++;
    }
    
    private void C_INDEX() {
        final double[] first = popItem();
        final int n = (int)first[0];
        cValues[cp] = cValues[cp - 1 - n];
        cTypes[cp] = cTypes[cp - 1 - n];
        cp++;
    }
    
    private void C_ROLL() {
        int j = (int)(popItem()[0]);
        final int n = (int)(popItem()[0]);
        if (n == 0 || j == 0 || n > cp) {
            //should not roll in these cases
            return;
        }
        LinkedList<Double> listV = new LinkedList<Double>();
        LinkedList<Integer> listT = new LinkedList<Integer>();
        
        for (int i = 0; i < n; i++) {
            double[] dd = popItem();
            listV.add(dd[0]);
            listT.add((int)dd[1]);
        }
        
        if (j > 0) {
            for (int i = 0; i < j; i++) {
                double v = listV.removeFirst();
                int t = listT.removeFirst();
                listV.addLast(v);
                listT.addLast(t);
            }
        } else {
            j *= -1;
            for (int i = 0; i < j; i++) {
                double v = listV.removeLast();
                int t = listT.removeLast();
                listV.addFirst(v);
                listT.addFirst(t);
            }
        }
        for (int i = 0; i < n; i++) {
            cValues[cp] = listV.removeLast();
            cTypes[cp] = listT.removeLast();
            cp++;
        }
    }
    
    private double[] popItem() {
        cp--;
        return new double[]{cValues[cp], cTypes[cp]};
    }

    public double[] executeScript(float[] inp) {
        Arrays.fill(cValues, 0);
        cp = inp.length;
        int dLen = dValues.length;
        dp = 0;

        for (int i = 0; i < inp.length; i++) {
            cValues[i] = inp[i];           
            cTypes[i] = T_NUMBER;
        }

        executeInterval(dLen);

        return cValues;

    }
    
    private void executeInterval(int maxDP){
        int type;
        double value;
        while (dp < maxDP) {
            type = dTypes[dp];
            value = dValues[dp];
            switch (type) {
                case T_COMMAND:
                    executeCommand((int) value);
                    dp++;
                    break;
                case T_NUMBER:
                    cValues[cp] = dValues[dp];
                    cTypes[cp] = dTypes[dp];
                    cp++;
                    dp++;
                    break;
                case T_SBRACE:
                    int buff = 1;
                    while (buff != 0 && dp < maxDP) {
                        dp++;
                        int t = dTypes[dp];
                        if(t == T_SBRACE){
                            buff++;
                        }else if(t == T_EBRACE){
                            buff--;
                        }                        
                    }
                    break;
                default:
                    dp++;
            }
        }
    }

//    public static void main(String[] args) {
//        String[] cmds = new String[]{
//            "abs", "add", "atan", "ceiling", "cos", "cvi", "cvr", "div", "exp", "floor", "idiv",
//            "ln", "log", "mod", "mul", "neg", "sin", "sqrt", "sub", "round", "truncate", "and",
//            "bitshift", "eq", "false", "ge", "gt", "le", "lt", "ne", "not", "or", "true", "xor",
//            "if", "ifelse", "copy", "exch", "pop", "dup", "index", "roll"};
//
//        for (String cmd : cmds) {
////            System.out.println("case C_" + cmd.toUpperCase() + " :\n return C_" + cmd.toUpperCase() + ";");
//        }
//
//        String ss = "{ 1 index}";
//        PostScriptCompiler pp = new PostScriptCompiler(ss.getBytes());
//
//        float inp[] = new float[]{4,1,3,2};
//        double vv[] = pp.executeScript(inp);
//
//        for (int i = 0; i < 5; i++) {
//            System.out.println(vv[i]);
//        }
//    }

}
