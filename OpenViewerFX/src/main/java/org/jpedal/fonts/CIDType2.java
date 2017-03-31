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
 * CIDType2.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CIDType2 {

	public static final String[] STANDARD_ENCODING = {
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", "space", "exclam", "quotedbl",
		"numbersign", "dollar", "percent", "ampersand",
		"quoteright", "parenleft", "parenright", "asterisk", "plus",
		"comma", "hyphen", "period", "slash", "zero", "one", "two",
		"three", "four", "five", "six", "seven", "eight", "nine",
		"colon", "semicolon", "less", "equal", "greater",
		"question", "at", "A", "B", "C", "D", "E", "F", "G", "H",
		"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
		"U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash",
		"bracketright", "asciicircum", "underscore", "quoteleft",
		"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
		"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
		"y", "z", "braceleft", "bar", "braceright", "asciitilde",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", "exclamdown",
		"cent", "sterling", "fraction", "yen", "florin", "section",
		"currency", "quotesingle", "quotedblleft", "guillemotleft",
		"guilsinglleft", "guilsinglright", "fi", "fl", ".notdef",
		"endash", "dagger", "daggerdbl", "periodcentered",
		".notdef", "paragraph", "bullet", "quotesinglbase",
		"quotedblbase", "quotedblright", "guillemotright",
		"ellipsis", "perthousand", ".notdef", "questiondown",
		".notdef", "grave", "acute", "circumflex", "tilde",
		"macron", "breve", "dotaccent", "dieresis", ".notdef",
		"ring", "cedilla", ".notdef", "hungarumlaut", "ogonek",
		"caron", "emdash", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", "AE", ".notdef",
		"ordfeminine", ".notdef", ".notdef", ".notdef", ".notdef",
		"Lslash", "Oslash", "OE", "ordmasculine", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", "ae", ".notdef",
		".notdef", ".notdef", "dotlessi", ".notdef", ".notdef",
		"lslash", "oslash", "oe", "germandbls", ".notdef",
		".notdef", ".notdef", ".notdef"
	};

	public static final String[] MACROMAN_ENCODING = {
		"NUL", "Eth", "eth", "Lslash", "lslash", "Scaron", "scaron", "Yacute",
		"yacute", "HT", "LF", "Thorn", "thorn", "CR", "Zcaron", "zcaron", "DLE", "DC1",
		"DC2", "DC3", "DC4", "onehalf", "onequarter", "onesuperior", "threequarters",
		"threesuperior", "twosuperior", "brokenbar", "minus", "multiply", "RS", "US",
		"space", "exclam", "quotedbl", "numbersign", "dollar", "percent", "ampersand",
		"quotesingle", "parenleft", "parenright", "asterisk", "plus", "comma",
		"hyphen", "period", "slash", "zero", "one", "two", "three", "four", "five",
		"six", "seven", "eight", "nine", "colon", "semicolon", "less", "equal",
		"greater", "question", "at", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
		"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
		"bracketleft", "backslash", "bracketright", "asciicircum", "underscore",
		"grave", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
		"o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft", "bar",
		"braceright", "asciitilde", "DEL", "Adieresis", "Aring", "Ccedilla", "Eacute",
		"Ntilde", "Odieresis", "Udieresis", "aacute", "agrave", "acircumflex",
		"adieresis", "atilde", "aring", "ccedilla", "eacute", "egrave", "ecircumflex",
		"edieresis", "iacute", "igrave", "icircumflex", "idieresis", "ntilde",
		"oacute", "ograve", "ocircumflex", "odieresis", "otilde", "uacute", "ugrave",
		"ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling", "section",
		"bullet", "paragraph", "germandbls", "registered", "copyright", "trademark",
		"acute", "dieresis", "notequal", "AE", "Oslash", "infinity", "plusminus",
		"lessequal", "greaterequal", "yen", "mu", "partialdiff", "summation",
		"product", "pi", "integral", "ordfeminine", "ordmasculine", "Omega", "ae",
		"oslash", "questiondown", "exclamdown", "logicalnot", "radical", "florin",
		"approxequal", "Delta", "guillemotleft", "guillemotright", "ellipsis",
		"nbspace", "Agrave", "Atilde", "Otilde", "OE", "oe", "endash", "emdash",
		"quotedblleft", "quotedblright", "quoteleft", "quoteright", "divide", "lozenge",
		"ydieresis", "Ydieresis", "fraction", "currency", "guilsinglleft",
		"guilsinglright", "fi", "fl", "daggerdbl", "periodcentered", "quotesinglbase",
		"quotedblbase", "perthousand", "Acircumflex", "Ecircumflex", "Aacute",
		"Edieresis", "Egrave", "Iacute", "Icircumflex", "Idieresis", "Igrave", "Oacute",
		"Ocircumflex", "apple", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi",
		"circumflex", "tilde", "macron", "breve", "dotaccent", "ring", "cedilla",
		"hungarumlaut", "ogonek", "caron"
	};

// The 391 Standard Strings as used in the CFF format. [Adobe Technical None #5176]
	public static final String[] CFF_STANDARD_STRINGS = {
		".notdef", "space", "exclam", "quotedbl", "numbersign",
		"dollar", "percent", "ampersand", "quoteright", "parenleft", "parenright",
		"asterisk", "plus", "comma", "hyphen", "period", "slash", "zero", "one",
		"two", "three", "four", "five", "six", "seven", "eight", "nine", "colon",
		"semicolon", "less", "equal", "greater", "question", "at", "A", "B", "C",
		"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
		"S", "T", "U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash",
		"bracketright", "asciicircum", "underscore", "quoteleft", "a", "b", "c",
		"d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
		"s", "t", "u", "v", "w", "x", "y", "z", "braceleft", "bar", "braceright",
		"asciitilde", "exclamdown", "cent", "sterling", "fraction", "yen", "florin",
		"section", "currency", "quotesingle", "quotedblleft", "guillemotleft",
		"guilsinglleft", "guilsinglright", "fi", "fl", "endash", "dagger",
		"daggerdbl", "periodcentered", "paragraph", "bullet", "quotesinglbase",
		"quotedblbase", "quotedblright", "guillemotright", "ellipsis", "perthousand",
		"questiondown", "grave", "acute", "circumflex", "tilde", "macron", "breve",
		"dotaccent", "dieresis", "ring", "cedilla", "hungarumlaut", "ogonek", "caron",
		"emdash", "AE", "ordfeminine", "Lslash", "Oslash", "OE", "ordmasculine", "ae",
		"dotlessi", "lslash", "oslash", "oe", "germandbls", "onesuperior",
		"logicalnot", "mu", "trademark", "Eth", "onehalf", "plusminus", "Thorn",
		"onequarter", "divide", "brokenbar", "degree", "thorn", "threequarters",
		"twosuperior", "registered", "minus", "eth", "multiply", "threesuperior",
		"copyright", "Aacute", "Acircumflex", "Adieresis", "Agrave", "Aring",
		"Atilde", "Ccedilla", "Eacute", "Ecircumflex", "Edieresis", "Egrave",
		"Iacute", "Icircumflex", "Idieresis", "Igrave", "Ntilde", "Oacute",
		"Ocircumflex", "Odieresis", "Ograve", "Otilde", "Scaron", "Uacute",
		"Ucircumflex", "Udieresis", "Ugrave", "Yacute", "Ydieresis", "Zcaron",
		"aacute", "acircumflex", "adieresis", "agrave", "aring", "atilde", "ccedilla",
		"eacute", "ecircumflex", "edieresis", "egrave", "iacute", "icircumflex",
		"idieresis", "igrave", "ntilde", "oacute", "ocircumflex", "odieresis",
		"ograve", "otilde", "scaron", "uacute", "ucircumflex", "udieresis", "ugrave",
		"yacute", "ydieresis", "zcaron", "exclamsmall", "Hungarumlautsmall",
		"dollaroldstyle", "dollarsuperior", "ampersandsmall", "Acutesmall",
		"parenleftsuperior", "parenrightsuperior", "twodotenleader", "onedotenleader",
		"zerooldstyle", "oneoldstyle", "twooldstyle", "threeoldstyle", "fouroldstyle",
		"fiveoldstyle", "sixoldstyle", "sevenoldstyle", "eightoldstyle",
		"nineoldstyle", "commasuperior", "threequartersemdash", "periodsuperior",
		"questionsmall", "asuperior", "bsuperior", "centsuperior", "dsuperior",
		"esuperior", "isuperior", "lsuperior", "msuperior", "nsuperior", "osuperior",
		"rsuperior", "ssuperior", "tsuperior", "ff", "ffi", "ffl", "parenleftinferior",
		"parenrightinferior", "Circumflexsmall", "hyphensuperior", "Gravesmall",
		"Asmall", "Bsmall", "Csmall", "Dsmall", "Esmall", "Fsmall", "Gsmall", "Hsmall",
		"Ismall", "Jsmall", "Ksmall", "Lsmall", "Msmall", "Nsmall", "Osmall", "Psmall",
		"Qsmall", "Rsmall", "Ssmall", "Tsmall", "Usmall", "Vsmall", "Wsmall", "Xsmall",
		"Ysmall", "Zsmall", "colonmonetary", "onefitted", "rupiah", "Tildesmall",
		"exclamdownsmall", "centoldstyle", "Lslashsmall", "Scaronsmall", "Zcaronsmall",
		"Dieresissmall", "Brevesmall", "Caronsmall", "Dotaccentsmall", "Macronsmall",
		"figuredash", "hypheninferior", "Ogoneksmall", "Ringsmall", "Cedillasmall",
		"questiondownsmall", "oneeighth", "threeeighths", "fiveeighths", "seveneighths",
		"onethird", "twothirds", "zerosuperior", "foursuperior", "fivesuperior",
		"sixsuperior", "sevensuperior", "eightsuperior", "ninesuperior", "zeroinferior",
		"oneinferior", "twoinferior", "threeinferior", "fourinferior", "fiveinferior",
		"sixinferior", "seveninferior", "eightinferior", "nineinferior", "centinferior",
		"dollarinferior", "periodinferior", "commainferior", "Agravesmall",
		"Aacutesmall", "Acircumflexsmall", "Atildesmall", "Adieresissmall", "Aringsmall",
		"AEsmall", "Ccedillasmall", "Egravesmall", "Eacutesmall", "Ecircumflexsmall",
		"Edieresissmall", "Igravesmall", "Iacutesmall", "Icircumflexsmall",
		"Idieresissmall", "Ethsmall", "Ntildesmall", "Ogravesmall", "Oacutesmall",
		"Ocircumflexsmall", "Otildesmall", "Odieresissmall", "OEsmall", "Oslashsmall",
		"Ugravesmall", "Uacutesmall", "Ucircumflexsmall", "Udieresissmall",
		"Yacutesmall", "Thornsmall", "Ydieresissmall", "001.000", "001.001", "001.002",
		"001.003", "Black", "Bold", "Book", "Light", "Medium", "Regular", "Roman",
		"Semibold"
	};

	public final static int CFF_STANDARD_STRING_COUNT = 391;
	public final static HashMap<String, Integer> CFF_STANDARD_STRING_MAP = new HashMap<String, Integer>();

	static {
		for (int i = 0; i < 391; i++) {
			CFF_STANDARD_STRING_MAP.put(CFF_STANDARD_STRINGS[i], i);
		}
	}

	public static final String[] CFF_ISO_ADOBE_STRINGS = {
		".notdef", "space", "exclam", "quotedbl", "numbersign",
		"dollar", "percent", "ampersand", "quoteright", "parenleft", "parenright",
		"asterisk", "plus", "comma", "hyphen", "period", "slash", "zero", "one", "two",
		"three", "four", "five", "six", "seven", "eight", "nine", "colon", "semicolon",
		"less", "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F", "G",
		"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
		"X", "Y", "Z", "bracketleft", "backslash", "bracketright", "asciicircum",
		"underscore", "quoteleft", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
		"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
		"braceleft", "bar", "braceright", "asciitilde", "exclamdown", "cent",
		"sterling", "fraction", "yen", "florin", "section", "currency", "quotesingle",
		"quotedblleft", "guillemotleft", "guilsinglleft", "guilsinglright", "fi", "fl",
		"endash", "dagger", "daggerdbl", "periodcentered", "paragraph", "bullet",
		"quotesinglbase", "quotedblbase", "quotedblright", "guillemotright", "ellipsis",
		"perthousand", "questiondown", "grave", "acute", "circumflex", "tilde",
		"macron", "breve", "dotaccent", "dieresis", "ring", "cedilla", "hungarumlaut",
		"ogonek", "caron", "emdash", "AE", "ordfeminine", "Lslash", "Oslash", "OE",
		"ordmasculine", "ae", "dotlessi", "lslash", "oslash", "oe", "germandbls",
		"onesuperior", "logicalnot", "mu", "trademark", "Eth", "onehalf", "plusminus",
		"Thorn", "onequarter", "divide", "brokenbar", "degree", "thorn",
		"threequarters", "twosuperior", "registered", "minus", "eth", "multiply",
		"threesuperior", "copyright", "Aacute", "Acircumflex", "Adieresis", "Agrave",
		"Aring", "Atilde", "Ccedilla", "Eacute", "Ecircumflex", "Edieresis", "Egrave",
		"Iacute", "Icircumflex", "Idieresis", "Igrave", "Ntilde", "Oacute",
		"Ocircumflex", "Odieresis", "Ograve", "Otilde", "Scaron", "Uacute",
		"Ucircumflex", "Udieresis", "Ugrave", "Yacute", "Ydieresis", "Zcaron", "aacute",
		"acircumflex", "adieresis", "agrave", "aring", "atilde", "ccedilla", "eacute",
		"ecircumflex", "edieresis", "egrave", "iacute", "icircumflex", "idieresis",
		"igrave", "ntilde", "oacute", "ocircumflex", "odieresis", "ograve", "otilde",
		"scaron", "uacute", "ucircumflex", "udieresis", "ugrave", "yacute", "ydieresis",
		"zcaron"
	};

	public static final int CFF_ISO_ADOBE_STRING_COUNT = 229;

	public static final String[] CFF_EXPERT_STRINGS = {
		".notdef", "space", "exclamsmall", "Hungarumlautsmall",
		"dollaroldstyle", "dollarsuperior", "ampersandsmall", "Acutesmall",
		"parenleftsuperior", "parenrightsuperior", "twodotenleader", "onedotenleader",
		"comma", "hyphen", "period", "fraction", "zerooldstyle", "oneoldstyle",
		"twooldstyle", "threeoldstyle", "fouroldstyle", "fiveoldstyle", "sixoldstyle",
		"sevenoldstyle", "eightoldstyle", "nineoldstyle", "colon", "semicolon",
		"commasuperior", "threequartersemdash", "periodsuperior", "questionsmall",
		"asuperior", "bsuperior", "centsuperior", "dsuperior", "esuperior", "isuperior",
		"lsuperior", "msuperior", "nsuperior", "osuperior", "rsuperior", "ssuperior",
		"tsuperior", "ff", "fi", "fl", "ffi", "ffl", "parenleftinferior",
		"parenrightinferior", "Circumflexsmall", "hyphensuperior", "Gravesmall",
		"Asmall", "Bsmall", "Csmall", "Dsmall", "Esmall", "Fsmall", "Gsmall", "Hsmall",
		"Ismall", "Jsmall", "Ksmall", "Lsmall", "Msmall", "Nsmall", "Osmall", "Psmall",
		"Qsmall", "Rsmall", "Ssmall", "Tsmall", "Usmall", "Vsmall", "Wsmall", "Xsmall",
		"Ysmall", "Zsmall", "colonmonetary", "onefitted", "rupiah", "Tildesmall",
		"exclamdownsmall", "centoldstyle", "Lslashsmall", "Scaronsmall", "Zcaronsmall",
		"Dieresissmall", "Brevesmall", "Caronsmall", "Dotaccentsmall", "Macronsmall",
		"figuredash", "hypheninferior", "Ogoneksmall", "Ringsmall", "Cedillasmall",
		"onequarter", "onehalf", "threequarters", "questiondownsmall", "oneeighth",
		"threeeighths", "fiveeighths", "seveneighths", "onethird", "twothirds",
		"zerosuperior", "onesuperior", "twosuperior", "threesuperior", "foursuperior",
		"fivesuperior", "sixsuperior", "sevensuperior", "eightsuperior", "ninesuperior",
		"zeroinferior", "oneinferior", "twoinferior", "threeinferior", "fourinferior",
		"fiveinferior", "sixinferior", "seveninferior", "eightinferior", "nineinferior",
		"centinferior", "dollarinferior", "periodinferior", "commainferior",
		"Agravesmall", "Aacutesmall", "Acircumflexsmall", "Atildesmall",
		"Adieresissmall", "Aringsmall", "AEsmall", "Ccedillasmall", "Egravesmall",
		"Eacutesmall", "Ecircumflexsmall", "Edieresissmall", "Igravesmall",
		"Iacutesmall", "Icircumflexsmall", "Idieresissmall", "Ethsmall", "Ntildesmall",
		"Ogravesmall", "Oacutesmall", "Ocircumflexsmall", "Otildesmall",
		"Odieresissmall", "OEsmall", "Oslashsmall", "Ugravesmall", "Uacutesmall",
		"Ucircumflexsmall", "Udieresissmall", "Yacutesmall", "Thornsmall",
		"Ydieresissmall"
	};

	public static final int CFF_EXPERT_STRING_COUNT = 166;

	public static final String[] CFF_EXPERT_SUBSET_STRINGS = {
		".notdef", "space", "dollaroldstyle",
		"dollarsuperior", "parenleftsuperior", "parenrightsuperior", "twodotenleader",
		"onedotenleader", "comma", "hyphen", "period", "fraction", "zerooldstyle",
		"oneoldstyle", "twooldstyle", "threeoldstyle", "fouroldstyle", "fiveoldstyle",
		"sixoldstyle", "sevenoldstyle", "eightoldstyle", "nineoldstyle", "colon",
		"semicolon", "commasuperior", "threequartersemdash", "periodsuperior",
		"asuperior", "bsuperior", "centsuperior", "dsuperior", "esuperior", "isuperior",
		"lsuperior", "msuperior", "nsuperior", "osuperior", "rsuperior", "ssuperior",
		"tsuperior", "ff", "fi", "fl", "ffi", "ffl", "parenleftinferior",
		"parenrightinferior", "hyphensuperior", "colonmonetary", "onefitted", "rupiah",
		"centoldstyle", "figuredash", "hypheninferior", "onequarter", "onehalf",
		"threequarters", "oneeighth", "threeeighths", "fiveeighths", "seveneighths",
		"onethird", "twothirds", "zerosuperior", "onesuperior", "twosuperior",
		"threesuperior", "foursuperior", "fivesuperior", "sixsuperior", "sevensuperior",
		"eightsuperior", "ninesuperior", "zeroinferior", "oneinferior", "twoinferior",
		"threeinferior", "fourinferior", "fiveinferior", "sixinferior", "seveninferior",
		"eightinferior", "nineinferior", "centinferior", "dollarinferior",
		"periodinferior", "commainferior"
	};

	public static final int CFF_EXPERT_SUBSET_STRING_COUNT = 87;

	//one byte operators
	public static final int VERSION = 0;
	public static final int NOTICE = 1;
	public static final int FULLNAME = 2;
	public static final int FAMILYNAME = 3;
	public static final int WEIGHT = 4;
	public static final int FONTBBOX = 5;
	public static final int BLUEVALUES = 6;
	public static final int OTHERBLUES = 7;
	public static final int FAMILYBLUES = 8;
	public static final int FAMILYOTHERBLUES = 9;
	public static final int STDHW = 10;
	public static final int STDVW = 11;
	public static final int ESCAPE = 12;
	public static final int UNIQUEID = 13;
	public static final int XUID = 14;
	public static final int CHARSET = 15;
	public static final int ENCODING = 16;
	public static final int CHARSTRINGS = 17;
	public static final int PRIVATE = 18;
	public static final int SUBRS = 19;
	public static final int DEFAULTWIDTHX = 20;
	public static final int NOMINALWIDTHX = 21;
	public static final int RESERVED = 22;
	public static final int SHORTINT = 28;
	public static final int LONGINT = 29;
	public static final int BCD = 30;

	//two byte operators
	public static final int COPYRIGHT = 3072;
	public static final int ISFIXEDPITCH = 3073;
	public static final int ITALICANGLE = 3074;
	public static final int UNDERLINEPOSITION = 3075;
	public static final int UNDERLINETHICKNESS = 3076;
	public static final int PAINTTYPE = 3077;
	public static final int CHARSTRINGTYPE = 3078;
	public static final int FONTMATRIX = 3079;
	public static final int STROKEWIDTH = 3080;
	public static final int BLUESCALE = 3081;
	public static final int BLUESHIFT = 3082;
	public static final int BLUEFUZZ = 3083;
	public static final int STEMSNAPH = 3084;
	public static final int STEMSNAPV = 3085;
	public static final int FORCEBOLD = 3086;
	public static final int LANGUAGEGROUP = 3089;
	public static final int EXPANSIONFACTOR = 3090;
	public static final int INITIALRANDOMSEED = 3091;
	public static final int SYNTHETICBASE = 3092;
	public static final int POSTSCRIPT = 3093;
	public static final int BASEFONTNAME = 3094;
	public static final int BASEFONTBLEND = 3095;
	public static final int ROS = 3102;
	public static final int CIDFONTVERSION = 3103;
	public static final int CIDFONTREVISION = 3104;
	public static final int CIDFONTTYPE = 3105;
	public static final int CIDCOUNT = 3106;
	public static final int UIDBASE = 3107;
	public static final int FDARRAY = 3108;
	public static final int FDSELECT = 3109;
	public static final int FONTNAME = 3110;

	public static final int TYPE_NUMBER = 0;
	public static final int TYPE_ARRAY = 1;
	public static final int TYPE_DELTA = 2;
	public static final int TYPE_SID = 3;
	public static final int TYPE_PRIVATE = 4;
	public static final int TYPE_ROS = 5;

	public static final int CHARSET_TYPE_ISO_ADOBE = 0;
	public static final int CHARSET_TYPE_EXPERT = 1;
	public static final int CHARSET_TYPE_EXPERTSUBSET = 2;

	public static boolean debug = true;

	public static CIDType2 parseType2(FileInput fb) throws IOException {
		CIDType2 font = new CIDType2();

		// reading header
		int headerMajor = fb.getU8();
		int headerMinor = fb.getU8();
		int headerSize = fb.getU8();
		int headerOffsize = fb.getU8();
		System.out.println(headerMajor + " " + headerMinor + " " + headerSize + " " + headerOffsize);

		int[] nameIndex = parseIndex(fb, headerSize);
		if (1 == 2) {
			printNameIndex(fb, nameIndex);
		}
		int[] topDictIndex = parseIndex(fb, nameIndex[nameIndex.length - 1]);
		Dict[] topDicts = parseDict(fb, topDictIndex);
		int[] stringIndex = parseIndex(fb, topDictIndex[topDictIndex.length - 1]);
		String[] stringIndexData = getStringIndexData(fb, stringIndex);

		int[] globalSubrIndex = parseIndex(fb, stringIndex[stringIndex.length - 1]);

		for (Dict dict : topDicts) {
			boolean isCID = dict.entries.containsKey(ROS);

			// update private dictionary
			Number[] values = dict.entries.get(PRIVATE);
			if (values != null && values.length > 1) {
				int len = values[0].intValue();
				int offset = values[1].intValue();
				byte[] temp = new byte[len];
				fb.moveTo(offset);
				fb.read(temp);
				dict.privateDict = new Dict(temp);
			}
			// update ascent, descent
			values = dict.entries.get(FONTBBOX);
			if (values != null && values.length == 4) {
				dict.ascent = Math.max(values[3].intValue(), values[1].intValue());
				dict.descent = Math.min(values[1].intValue(), values[3].intValue());
			}

			// update charstring index
			values = dict.entries.get(CHARSTRINGS);
			int charStringOffset = values[0].intValue();
			dict.charStringIndex = parseIndex(fb, charStringOffset);

			// update charsets
			values = dict.entries.get(CHARSET);
			dict.charset = parseCharset(fb, values, dict.charStringIndex.length - 1, stringIndexData, isCID);

			// update encoding and remove encodings for CID
			if (isCID) {
				dict.entries.remove(ENCODING);
			} else {

			}
		}

		// now print dict
		if (debug) {
			System.out.println("GlobalSubrIndex : " + globalSubrIndex.length);
			for (Dict dict : topDicts) {
				System.out.println("printing Dictionary Normal Values.....................");
				for (Integer entryKey : dict.entries.keySet()) {
					Number[] values = dict.entries.get(entryKey);
					printDictEntry(entryKey, values);
					if (getKeyType(entryKey) == TYPE_SID) {
						System.out.println("sid : " + getFromStringIndexData(stringIndexData, values[0].intValue()));
					}
				}
				if (dict.privateDict != null) {
					System.out.println("-------- private Dictionary All Values------------");
					for (Integer entryKey : dict.privateDict.entries.keySet()) {
						Number[] vv = dict.privateDict.entries.get(entryKey);
						printDictEntry(entryKey, vv);
						if (getKeyType(entryKey) == TYPE_SID) {
							System.out.println("\tsid : " + getFromStringIndexData(stringIndexData, vv[0].intValue()));
						}
					}
					System.out.println("--------------------------------------------------");
				}
			}
			System.out.println(stringIndex.length);
		}

//		Index nameIndex = new Index();
		// reading name index
		return font;
	}

	private static String getFromStringIndexData(String[] arr, int sid) throws IOException {
		if (sid >= 0 && sid <= 390) {
			return CFF_STANDARD_STRINGS[sid];
		}
		int gap = sid - 391;
		if ((sid - 391) <= (arr.length - 1)) {
			return arr[gap];
		} else {
			return CFF_STANDARD_STRINGS[0];
		}
	}

	private static int[] parseIndex(FileInput fb, int indexOffset) throws IOException {
		fb.moveTo(indexOffset);
		int count = fb.getU16();
		int end = fb.getPosition();
		int[] results = {end};
		if (count != 0) {
			int offSize = fb.getU8();
			results = new int[count + 1];
			int startPos = fb.getPosition() + ((count + 1) * offSize) - 1;
			for (int i = 0, ii = count + 1; i < ii; ++i) {
				int offset = fb.getOffset(offSize);
				results[i] = (startPos + offset);
			}
		}
		return results;
	}

	private static CIDCharset parseCharset(FileInput fb, Number[] values, int charStringIndexLen, String[] stringIndexData, boolean isCID) throws IOException {
		if (values == null || values.length == 0) {
			return new CIDCharset(true, CHARSET_TYPE_ISO_ADOBE, CFF_ISO_ADOBE_STRINGS, null);
		} else if (values[0].intValue() == CHARSET_TYPE_ISO_ADOBE) {
			return new CIDCharset(true, CHARSET_TYPE_ISO_ADOBE, CFF_ISO_ADOBE_STRINGS, null);
		} else if (values[0].intValue() == CHARSET_TYPE_EXPERT) {
			return new CIDCharset(true, CHARSET_TYPE_EXPERT, CFF_EXPERT_STRINGS, null);
		} else if (values[0].intValue() == CHARSET_TYPE_EXPERTSUBSET) {
			return new CIDCharset(true, CHARSET_TYPE_EXPERTSUBSET, CFF_EXPERT_SUBSET_STRINGS, null);
		} else {
			List<String> charsetList = new ArrayList<String>();
			charsetList.add(".notdef");

			int start = values[0].intValue();
			fb.moveTo(start);

			int format = fb.getU8();
			int id, count;
			int length = charStringIndexLen - 1;

			switch (format) {
				case 0:
					for (int i = 0; i < length; i++) {
						id = (fb.getU8() << 8) | fb.getU8();
						charsetList.add(isCID ? "" + id : getFromStringIndexData(stringIndexData, id));
					}
					break;
				case 1:
					while (charsetList.size() <= length) {
						id = (fb.getU8() << 8) | fb.getU8();
						count = fb.getU8();
						for (int i = 0; i <= count; i++) {
							charsetList.add(isCID ? "" + id : getFromStringIndexData(stringIndexData, id));
							id++;
						}
					}
					break;
				case 2:
					while (charsetList.size() <= length) {
						id = (fb.getU8() << 8) | fb.getU8();
						count = (fb.getU8() << 8) | fb.getU8();
						for (int i = 0; i <= count; i++) {
							charsetList.add(isCID ? "" + id : getFromStringIndexData(stringIndexData, id));
							id++;
						}
					}
					break;
			}

			int end = fb.getPosition();
			int rawLen = end - start;
			byte[] raw = new byte[rawLen];
			fb.moveTo(start);
			fb.read(raw);

			String[] charsets = new String[charsetList.size()];
			charsets = charsetList.toArray(charsets);

			return new CIDCharset(false, format, charsets, raw);

		}
	}

	private static void printNameIndex(FileInput fb, int[] index) throws IOException {
		System.out.println("Printing Name Index : ");
		if (index.length > 1) {
			for (int i = 0, ii = index.length - 1; i < ii; i++) {
				int len = index[i + 1] - index[i];
				byte[] data = new byte[len];
				fb.moveTo(index[i]);
				fb.read(data);
				System.out.println(new String(data));
			}
		} else {
			System.out.println("No Name Index");
		}
	}

	private static String[] getStringIndexData(FileInput fb, int[] index) throws IOException {
		if (index.length > 1) {
			String[] result = new String[index.length - 1];
			for (int i = 0, ii = index.length - 1; i < ii; i++) {
				int len = index[i + 1] - index[i];
				byte[] data = new byte[len];
				fb.moveTo(index[i]);
				fb.read(data);
				result[i] = (new String(data));
			}
			return result;
		} else {
			return null;
		}
	}

	private static void printDictEntry(int key, Number[] values) {
		StringBuilder vv = new StringBuilder();
		for (Number n : values) {
			vv.append(n).append(' ');
		}
		System.out.println("DictEntry [ " + getKeyAsString(key) + " : " + vv.toString() + ']');
	}

	private static Dict[] parseDict(FileInput fb, int[] index) throws IOException {
		Dict[] result = null;
		if (index.length > 1) {
			result = new Dict[index.length - 1];
			for (int i = 0, ii = index.length - 1; i < ii; i++) {
				int len = index[i + 1] - index[i];
				byte[] data = new byte[len];
				fb.moveTo(index[i]);
				fb.read(data);
				result[i] = new Dict(data);
			}
		} else {
			System.out.println("No Name Index");
		}
		return result;
	}

	public static String getKeyAsString(int key) {
		return key < 3000 ? getSingleByte(key) : getDoubleByte(key);
	}

	public static int getKeyType(int key) {
		switch (key) {
			case VERSION:
			case NOTICE:
			case COPYRIGHT:
			case FULLNAME:
			case FAMILYNAME:
			case WEIGHT:
			case POSTSCRIPT:
			case BASEFONTNAME:
			case FONTNAME:
				return TYPE_SID;
			case FONTMATRIX:
			case FONTBBOX:
			case XUID:
				return TYPE_ARRAY;
			case BLUEVALUES:
			case OTHERBLUES:
			case FAMILYBLUES:
			case FAMILYOTHERBLUES:
			case STEMSNAPH:
			case STEMSNAPV:
				return TYPE_DELTA;
			case PRIVATE:
				return TYPE_PRIVATE;
			case ROS:
				return TYPE_ROS;
			default:
				return TYPE_NUMBER;
		}
	}

	private static String getSingleByte(int key) {
		switch (key) {
			case VERSION:
				return "version";
			case NOTICE:
				return "Notice";
			case FULLNAME:
				return "FullName";
			case FAMILYNAME:
				return "FamilyName";
			case WEIGHT:
				return "Weight";
			case FONTBBOX:
				return "FontBBox";
			case BLUEVALUES:
				return "BlueValues";
			case OTHERBLUES:
				return "OtherBlues";
			case FAMILYBLUES:
				return "FamilyBlues";
			case FAMILYOTHERBLUES:
				return "FamilyOtherBlues";
			case STDHW:
				return "StdHW";
			case STDVW:
				return "StdVW";
			case ESCAPE:
				return "Escape";
			case UNIQUEID:
				return "UniqueID";
			case XUID:
				return "XUID";
			case CHARSET:
				return "Charset";
			case ENCODING:
				return "Encoding";
			case CHARSTRINGS:
				return "Charstrings";
			case PRIVATE:
				return "Private";
			case SUBRS:
				return "Subrs";
			case DEFAULTWIDTHX:
				return "DefaultWidthX";
			case NOMINALWIDTHX:
				return "NominalWidthx";
			case RESERVED:
				return "Reserved";
			case SHORTINT:
				return "ShortInt";
			case LONGINT:
				return "LongInt";
			case BCD:
				return "BCD";
			default:
				return "key not found" + key;
		}
	}

	private static String getDoubleByte(int key) {
		switch (key) {
			case COPYRIGHT:
				return "CopyRight";
			case ISFIXEDPITCH:
				return "IsFixedPitch";
			case ITALICANGLE:
				return "ItalicAngle";
			case UNDERLINEPOSITION:
				return "UnderlinePosition";
			case UNDERLINETHICKNESS:
				return "UnderlineThickness";
			case PAINTTYPE:
				return "Painttype";
			case CHARSTRINGTYPE:
				return "CharstringType";
			case FONTMATRIX:
				return "FontFatrix";
			case STROKEWIDTH:
				return "StrokeWidth";
			case BLUESCALE:
				return "BlueScale";
			case BLUESHIFT:
				return "BlueShift";
			case BLUEFUZZ:
				return "BlueFuzz";
			case STEMSNAPH:
				return "StemSnapH";
			case STEMSNAPV:
				return "StemSnapV";
			case FORCEBOLD:
				return "ForceBold";
			case LANGUAGEGROUP:
				return "LanguageGroup";
			case EXPANSIONFACTOR:
				return "ExpansionFactor";
			case INITIALRANDOMSEED:
				return "InitialRandomSeed";
			case SYNTHETICBASE:
				return "SyntheticBase";
			case POSTSCRIPT:
				return "PostScript";
			case BASEFONTNAME:
				return "BaseFontName";
			case BASEFONTBLEND:
				return "BaseFontBlend";
			case ROS:
				return "Ros";
			case CIDFONTVERSION:
				return "CidFontVersion";
			case CIDFONTREVISION:
				return "CidFontRevision";
			case CIDFONTTYPE:
				return "CidFontType";
			case CIDCOUNT:
				return "CidCount";
			case UIDBASE:
				return "UidBase";
			case FDARRAY:
				return "FdArray";
			case FDSELECT:
				return "FdSelect";
			case FONTNAME:
				return "FontName";
			default:
				return "key not found " + key;
		}
	}

	public static void main(String[] args) throws IOException {
		java.io.File file = new java.io.File("C:\\Users\\suda\\Desktop\\created\\results\\bade.cff");
		CIDType2.parseType2(new FileInput(file));

		Number x = 15;
		System.out.println(x.equals(15));
	}
//	private static class TopDict {
//
//		Number copyRight = null;
//		Number fullName = null;
//		Number familyName = null;
//		Number weight = null;
//
//		Number isFixedPitch = null;
//		Number italicAngle = 0;
//		Number underLinePostion = -100;
//		Number underLineThickness = 50;
//		Number paintType = null;
//		Number charstringType = 2;
//		float[] fontMatrix = {0.001f, 0, 0, 0.001f, 0, 0};
//		float[] fontBBox = {0, 0, 0, 0};
//		Number strokeWidth = 0;
//		Number charset = 0;
//		Number Encoding = 0;
//		Number CharStrings = 0;
//		float[] privateValues = null;
//		Number postscript = null;
//		Number baseFontName = null;
//		float[] baseFontBlend = null;
//  }    

}
