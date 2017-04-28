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
 * CIDType1CParser.java
 * ---------------
 */
package org.jpedal.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CIDType1CParser {

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

	public static final String[] EXPERT_ENCODING = {
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", "space", "exclamsmall", "Hungarumlautsmall",
		".notdef", "dollaroldstyle", "dollarsuperior", "ampersandsmall",
		"Acutesmall", "parenleftsuperior", "parenrightsuperior", "twodotenleader",
		"onedotenleader", "comma",
		"hyphen", "period", "fraction", "zerooldstyle", "oneoldstyle",
		"twooldstyle", "threeoldstyle", "fouroldstyle", "fiveoldstyle",
		"sixoldstyle", "sevenoldstyle", "eightoldstyle", "nineoldstyle", "colon",
		"semicolon", "commasuperior", "threequartersemdash", "periodsuperior",
		"questionsmall", ".notdef", "asuperior", "bsuperior", "centsuperior",
		"dsuperior", "esuperior", ".notdef", ".notdef", "isuperior", ".notdef",
		".notdef", "lsuperior", "msuperior", "nsuperior", "osuperior", ".notdef",
		".notdef", "rsuperior", "ssuperior", "tsuperior",
		".notdef", "ff", "fi", "fl", "ffi", "ffl", "parenleftinferior", ".notdef",
		"parenrightinferior", "Circumflexsmall", "hyphensuperior", "Gravesmall",
		"Asmall", "Bsmall", "Csmall", "Dsmall", "Esmall", "Fsmall", "Gsmall",
		"Hsmall", "Ismall", "Jsmall", "Ksmall", "Lsmall", "Msmall", "Nsmall",
		"Osmall", "Psmall", "Qsmall", "Rsmall", "Ssmall", "Tsmall", "Usmall",
		"Vsmall", "Wsmall", "Xsmall", "Ysmall", "Zsmall", "colonmonetary",
		"onefitted", "rupiah", "Tildesmall", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
		".notdef", "exclamdownsmall", "centoldstyle", "Lslashsmall", ".notdef",
		".notdef", "Scaronsmall", "Zcaronsmall", "Dieresissmall", "Brevesmall",
		"Caronsmall", ".notdef", "Dotaccentsmall", ".notdef", ".notdef",
		"Macronsmall", ".notdef", ".notdef", "figuredash", "hypheninferior",
		".notdef", ".notdef", "Ogoneksmall", "Ringsmall",
		"Cedillasmall", ".notdef", ".notdef", ".notdef", "onequarter", "onehalf",
		"threequarters", "questiondownsmall", "oneeighth", "threeeighths", "fiveeighths",
		"seveneighths", "onethird", "twothirds", ".notdef", ".notdef", "zerosuperior",
		"onesuperior", "twosuperior", "threesuperior", "foursuperior",
		"fivesuperior", "sixsuperior", "sevensuperior", "eightsuperior",
		"ninesuperior", "zeroinferior", "oneinferior", "twoinferior",
		"threeinferior", "fourinferior", "fiveinferior", "sixinferior",
		"seveninferior", "eightinferior", "nineinferior", "centinferior",
		"dollarinferior", "periodinferior", "commainferior", "Agravesmall",
		"Aacutesmall", "Acircumflexsmall", "Atildesmall", "Adieresissmall",
		"Aringsmall", "AEsmall", "Ccedillasmall", "Egravesmall", "Eacutesmall",
		"Ecircumflexsmall", "Edieresissmall", "Igravesmall", "Iacutesmall",
		"Icircumflexsmall", "Idieresissmall", "Ethsmall", "Ntildesmall",
		"Ogravesmall", "Oacutesmall", "Ocircumflexsmall", "Otildesmall",
		"Odieresissmall", "OEsmall", "Oslashsmall", "Ugravesmall", "Uacutesmall",
		"Ucircumflexsmall", "Udieresissmall", "Yacutesmall", "Thornsmall",
		"Ydieresissmall"
	};

	public static final String[] MACROMAN_STANDARD_ENCODING = {
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

	public static final int CFF_STANDARD_STRING_COUNT = 391;
	public static final HashMap<String, Integer> CFF_STANDARD_STRING_MAP = new HashMap<String, Integer>();

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

	public static final CCSV[] CS_VALIDATION1 = new CCSV[32];
	public static final CCSV[] CS_VALIDATION2 = new CCSV[40];

	static {
		CS_VALIDATION1[0] = null;
		CS_VALIDATION1[1] = new CCSV("hstem", 2, true, false, false, true, null, false);
		CS_VALIDATION1[2] = null;
		CS_VALIDATION1[3] = new CCSV("vstem", 2, true, false, false, true, null, false);
		CS_VALIDATION1[4] = new CCSV("vmoveto", 1, true, false, false, false, null, false);
		CS_VALIDATION1[5] = new CCSV("rlineto", 2, false, true, false, false, null, false);
		CS_VALIDATION1[6] = new CCSV("hlineto", 1, false, true, false, false, null, false);
		CS_VALIDATION1[7] = new CCSV("vlineto", 1, false, true, false, false, null, false);
		CS_VALIDATION1[8] = new CCSV("rrcurveto", 6, false, true, false, false, null, false);
		CS_VALIDATION1[9] = null;
		CS_VALIDATION1[10] = new CCSV("callsubr", 1, false, false, true, false, null, false);
		CS_VALIDATION1[11] = new CCSV("return", 0, false, false, true, false, null, false);
		CS_VALIDATION1[12] = null;
		CS_VALIDATION1[13] = null;
		CS_VALIDATION1[14] = new CCSV("endchar", 0, true, false, false, false, null, false);
		CS_VALIDATION1[15] = null;
		CS_VALIDATION1[16] = null;
		CS_VALIDATION1[17] = null;
		CS_VALIDATION1[18] = new CCSV("hstemhm", 2, true, false, false, true, null, false);
		CS_VALIDATION1[19] = new CCSV("hintmask", 0, true, false, false, false, null, false);
		CS_VALIDATION1[20] = new CCSV("cntrmask", 0, true, false, false, false, null, false);
		CS_VALIDATION1[21] = new CCSV("rmoveto", 2, true, false, false, false, null, false);
		CS_VALIDATION1[22] = new CCSV("hmoveto", 1, true, false, false, false, null, false);
		CS_VALIDATION1[23] = new CCSV("vstemhm", 2, true, false, false, true, null, false);
		CS_VALIDATION1[24] = new CCSV("rcurveline", 8, false, true, false, false, null, false);
		CS_VALIDATION1[25] = new CCSV("rlinecurve", 8, false, true, false, false, null, false);
		CS_VALIDATION1[26] = new CCSV("vvcurveto", 4, false, true, false, false, null, false);
		CS_VALIDATION1[27] = new CCSV("hhcurveto", 4, false, true, false, false, null, false);
		CS_VALIDATION1[28] = null;
		CS_VALIDATION1[29] = new CCSV("callgsubr", 1, false, false, true, false, null, false);
		CS_VALIDATION1[30] = new CCSV("vhcurveto", 4, false, true, false, false, null, false);
		CS_VALIDATION1[31] = new CCSV("hvcurveto", 4, false, true, false, false, null, false);

		CS_VALIDATION2[0] = null;
		CS_VALIDATION2[1] = null;
		CS_VALIDATION2[2] = null;
		CS_VALIDATION2[4] = new CCSV("and", 2, false, false, false, false, -1, false);
		CS_VALIDATION2[5] = new CCSV("or", 2, false, false, false, false, -1, false);
		CS_VALIDATION2[6] = new CCSV("not", 1, false, false, false, false, 0, false);
		CS_VALIDATION2[7] = null;
		CS_VALIDATION2[8] = null;
		CS_VALIDATION2[9] = null;
		CS_VALIDATION2[10] = new CCSV("abs", 1, false, false, false, false, 0, false);
		CS_VALIDATION2[11] = new CCSV("add", 2, false, false, false, false, -1, true);
		CS_VALIDATION2[12] = new CCSV("sub", 2, false, false, false, false, -1, true);
		CS_VALIDATION2[13] = new CCSV("div", 2, false, false, false, false, -1, true);
		CS_VALIDATION2[14] = null;
		CS_VALIDATION2[15] = new CCSV("neg", 1, false, false, false, false, 0, true);
		CS_VALIDATION2[16] = new CCSV("eq", 2, false, false, false, false, -1, false);
		CS_VALIDATION2[17] = null;
		CS_VALIDATION2[18] = null;
		CS_VALIDATION2[19] = new CCSV("drop", 1, false, false, false, false, -1, false);
		CS_VALIDATION2[20] = null;
		CS_VALIDATION2[21] = null;
		CS_VALIDATION2[22] = new CCSV("put", 2, false, false, false, false, -2, false);
		CS_VALIDATION2[23] = new CCSV("get", 1, false, false, false, false, 0, false);
		CS_VALIDATION2[24] = new CCSV("ifelse", 4, false, false, false, false, -3, false);
		CS_VALIDATION2[25] = new CCSV("random", 0, false, false, false, false, 1, false);
		CS_VALIDATION2[26] = new CCSV("mul", 2, false, false, false, false, -1, true);
		CS_VALIDATION2[27] = null;
		CS_VALIDATION2[28] = new CCSV("sqrt", 1, false, false, false, false, 0, false);
		CS_VALIDATION2[29] = new CCSV("dub", 1, false, false, false, false, 1, false);
		CS_VALIDATION2[30] = new CCSV("exch", 2, false, false, false, false, 0, false);
		CS_VALIDATION2[31] = new CCSV("index", 2, false, false, false, false, 0, false);
		CS_VALIDATION2[32] = new CCSV("roll", 3, false, false, false, false, -2, false);
		CS_VALIDATION2[33] = null;
		CS_VALIDATION2[34] = null;
		CS_VALIDATION2[35] = null;
		CS_VALIDATION2[36] = new CCSV("hflex", 7, false, true, false, false, null, false);
		CS_VALIDATION2[37] = new CCSV("flex", 13, false, true, false, false, null, false);
		CS_VALIDATION2[38] = new CCSV("hflex1", 9, false, true, false, false, null, false);
		CS_VALIDATION2[39] = new CCSV("flex1", 11, false, true, false, false, null, false);

	}

	public static boolean debug = true;

	public static CIDType1C parse(FileInput fb) throws IOException {
		CIDType1C font = new CIDType1C();

		// reading header
		font.headerMajor = fb.getU8();
		font.headerMinor = fb.getU8();
		font.headerSize = fb.getU8();
		font.headerOffsize = fb.getU8();

		int[] nameIndex = parseIndex(fb, font.headerSize);
		font.nameIndexData = indexToStringArray(fb, nameIndex);
		int[] topDictIndex = parseIndex(fb, nameIndex[nameIndex.length - 1]);
		font.topDicts = parseDict(fb, topDictIndex);
		int[] stringIndex = parseIndex(fb, topDictIndex[topDictIndex.length - 1]);
		font.stringIndexData = indexToStringArray(fb, stringIndex);

		int[] globalSubrIndex = parseIndex(fb, stringIndex[stringIndex.length - 1]);
		font.globalSubrsIndexData = indexToByteArray(fb, globalSubrIndex);

		for (Dict dict : font.topDicts) {
			boolean isCID = dict.entries.containsKey(ROS);

			// update private dictionary
			dict.privateDict = parsePrivateDict(fb, dict);

			// update ascent, descent
			Number[] values = dict.entries.get(FONTBBOX);
			if (values != null && values.length == 4) {
				dict.ascent = Math.max(values[3].intValue(), values[1].intValue());
				dict.descent = Math.min(values[1].intValue(), values[3].intValue());
			}

			// update charstring index
			values = dict.entries.get(CHARSTRINGS);
			if (values != null) {
				int charStringOffset = values[0].intValue();
				int[] charStringIndex = parseIndex(fb, charStringOffset);
				font.charStringIndexData = indexToByteArray(fb, charStringIndex);
				
				// update charsets
				values = dict.entries.get(CHARSET);
				dict.charset = parseCharset(fb, values, charStringIndex.length - 1, font.stringIndexData, isCID);
				
			}		

			// update encoding and remove encodings for CID
			if (isCID) {
				dict.entries.remove(ENCODING);
				Number[] fVal = dict.entries.get(FDARRAY);
				int[] fdArrayIndex = parseIndex(fb, fVal[0].intValue());
				byte[][] fdArrayIndexData = indexToByteArray(fb, fdArrayIndex);
				int fdArrLen = fdArrayIndexData.length;
				dict.fontDicts = new Dict[fdArrLen];
				for (int i = 0; i < fdArrLen; i++) {
					dict.fontDicts[i] = new Dict(fdArrayIndexData[i]);
					dict.fontDicts[i].privateDict = parsePrivateDict(fb, dict.fontDicts[i]);
				}
				fVal = dict.entries.get(FDSELECT);
				dict.fdSelect = parseFDSelect(fb, fVal, font.charStringIndexData.length);

			} else {
				values = dict.entries.get(ENCODING);
				dict.encoding = parseEncoding(fb, values, dict.charset.charset);
			}
			byte[][] localSubrsIndexData = dict.privateDict != null ? dict.privateDict.subrsIndexData : null;
			parseCharStrings(dict, font.charStringIndexData, font.globalSubrsIndexData, localSubrsIndexData);
			font.glyphWidths = new int[font.charStringIndexData.length][1];
			updateWidthsNormal(font.charStringIndexData, font.glyphWidths, dict);
		}

		// now print dict
		if (debug) {
			System.out.println("printing Name Index .....................");
			for (String string : font.nameIndexData) {
				System.out.println(string);
			}

			for (Dict dict : font.topDicts) {
				System.out.println("printing Dictionary Normal Values.....................");
				for (Integer entryKey : dict.entries.keySet()) {
					Number[] values = dict.entries.get(entryKey);
					printDictEntry(entryKey, values);
					if (getKeyType(entryKey) == TYPE_SID) {
						System.out.println("sid : " + getFromStringIndexData(font.stringIndexData, values[0].intValue()));
					}
				}
				if (dict.privateDict != null) {
					System.out.println("-------- private Dictionary All Values------------");
					for (Integer entryKey : dict.privateDict.entries.keySet()) {
						Number[] vv = dict.privateDict.entries.get(entryKey);
						printDictEntry(entryKey, vv);
						if (getKeyType(entryKey) == TYPE_SID) {
							System.out.println("\tsid : " + getFromStringIndexData(font.stringIndexData, vv[0].intValue()));
						}
					}
					System.out.println("--------------------------------------------------");
				}

				Number[] rosValues = dict.entries.get(ROS);
				if (rosValues != null) {
					System.out.println("---------- printing FDSelect --------------------");

					System.out.println("\n--------------------------------------------------");
				} else {
					System.out.println("---------- printing Encodings --------------------");
					System.out.println("encoding array : " + dict.encoding.format);
					int[] encodings = dict.encoding.encoding;
					for (int i = 0; i < encodings.length; i++) {
						if (encodings[i] != -1) {
							System.out.print(" enc : " + encodings[i]);
						}
					}
					System.out.println("\n--------------------------------------------------");
				}

				System.out.println("----------- printing Widths ----------------------");
				if (debug) {
					for (int i = 0; i < font.glyphWidths.length; i++) {
						System.out.println(i + " width : " + font.glyphWidths[i][0]);
					}
				}
				System.out.println("--------------------------------------------------");
				boolean isCID = dict.entries.containsKey(ROS);
				if (isCID) {
					System.out.println("-------- printing cid Fontdict Values------------");
					for (Dict fontDict : dict.fontDicts) {
						for (Integer entryKey : fontDict.entries.keySet()) {
							Number[] values = fontDict.entries.get(entryKey);
							printDictEntry(entryKey, values);
							if (getKeyType(entryKey) == TYPE_SID) {
								System.out.println("sid : " + getFromStringIndexData(font.stringIndexData, values[0].intValue()));
							}
						}
					}
					System.out.println("--------------------------------------------------");
				}
			}

			System.out.println("GlobalSubrIndex : " + globalSubrIndex.length);
		}
		fb.close();

//		Index nameIndex = new Index();
		// reading name index
		return font;
	}

	public static byte[] fix(CIDType1C font, String customName) throws IOException {

		changeFontName(font, customName);

		// write header
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(font.headerMajor);
		bos.write(font.headerMinor);
		bos.write(4);
		bos.write(font.headerOffsize);

		// write name index
		bos.write(createIndex(font.nameIndexData));

		// write TOPDict
		Dict firstTop = font.topDicts[0];
		byte[] dictBytes = createDictBytes(firstTop);

		Dict t = new Dict(dictBytes);

		System.out.println("printing Dictionary Normal Values.....................");
		for (Integer entryKey : t.entries.keySet()) {
			Number[] values = t.entries.get(entryKey);
			printDictEntry(entryKey, values);
			if (getKeyType(entryKey) == TYPE_SID) {
				System.out.println("sid : " + getFromStringIndexData(font.stringIndexData, values[0].intValue()));
			}
		}

		bos.write(createIndex(font.stringIndexData));
		bos.write(createIndex(font.globalSubrsIndexData));

		return bos.toByteArray();
	}

	private static void changeFontName(CIDType1C font, String customName) {
		font.nameIndexData = new String[]{customName};
		Dict firstTop = font.topDicts[0];
		String[] temp = new String[font.stringIndexData.length + 1];
		System.arraycopy(font.stringIndexData, 0, temp, 0, temp.length - 1);
		temp[temp.length - 1] = customName;
		font.stringIndexData = temp;
		firstTop.entries.put(FULLNAME, new Number[]{390 + temp.length});
		firstTop.entries.put(FAMILYNAME, new Number[]{390 + temp.length});
		firstTop.entries.put(BASEFONTNAME, new Number[]{390 + temp.length});
	}

	private static byte[] createDictBytes(Dict dict) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (Integer key : dict.entries.keySet()) {
			Number[] values = dict.entries.get(key);
			switch (key) {
				case CHARSET:
				case ENCODING:
				case CHARSTRINGS:
				case PRIVATE:
				case FDARRAY:
				case FDSELECT:
				case SUBRS:
					dict.trackers.put(key, bos.size());
					byte[] tt = new byte[5];
					tt[0] = 0x1d;
					bos.write(tt);
					break;
				default:
					for (Number value : values) {
						if (value.intValue() == value.floatValue()) {
							byte[] temp = encodeInt(value.intValue());
							bos.write(temp);
						} else {
							byte[] temp = encodeFloat(value.floatValue());
							bos.write(temp);
						}
					}
			}

			if (key < 3000) {
				bos.write(key);
			} else {
				int b1 = (key >> 8) & 0xff;
				int b2 = key & 0xff;
				bos.write(b1);
				bos.write(b2);
			}

		}
		return bos.toByteArray();

	}

	private static byte[] createIndex(String[] strArr) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (strArr == null || strArr.length == 0) {
			return new byte[]{0, 0, 0};
		}
		int sLen = strArr.length;

		bos.write((sLen >> 8) & 0xff);
		bos.write(sLen & 0xff);

		int lastOffset = 1;
		for (int i = 0; i < sLen; ++i) {
			lastOffset += strArr[i].length();
		}

		int offsetSize;
		if (lastOffset < 0x100) {
			offsetSize = 1;
		} else if (lastOffset < 0x10000) {
			offsetSize = 2;
		} else if (lastOffset < 0x1000000) {
			offsetSize = 3;
		} else {
			offsetSize = 4;
		}

		bos.write(offsetSize);

		int relativeOffset = 1;
		for (int i = 0; i < sLen + 1; i++) {
			switch (offsetSize) {
				case 1:
					bos.write(relativeOffset & 0xFF);
					break;
				case 2:
					bos.write((relativeOffset >> 8) & 0xFF);
					bos.write(relativeOffset & 0xFF);
					break;
				case 3:
					bos.write((relativeOffset >> 16) & 0xFF);
					bos.write((relativeOffset >> 8) & 0xFF);
					bos.write(relativeOffset & 0xFF);
					break;
				default:
					bos.write((relativeOffset >>> 24) & 0xFF);
					bos.write((relativeOffset >> 16) & 0xFF);
					bos.write((relativeOffset >> 8) & 0xFF);
					bos.write(relativeOffset & 0xFF);
					break;
			}
			if (i < sLen) {
				relativeOffset += strArr[i].length();
			}
		}

		for (String str : strArr) {
			bos.write(str.getBytes());
		}
		return bos.toByteArray();

	}

	private static byte[] createIndex(byte[][] byteArr) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (byteArr == null || byteArr.length == 0) {
			return new byte[]{0, 0, 0};
		}
		int arrLen = byteArr.length;

		bos.write((arrLen >> 8) & 0xff);
		bos.write(arrLen & 0xff);

		int lastOffset = 1;
		for (int i = 0; i < arrLen; ++i) {
			lastOffset += byteArr[i].length;
		}

		int offsetSize;
		if (lastOffset < 0x100) {
			offsetSize = 1;
		} else if (lastOffset < 0x10000) {
			offsetSize = 2;
		} else if (lastOffset < 0x1000000) {
			offsetSize = 3;
		} else {
			offsetSize = 4;
		}

		bos.write(offsetSize);

		int relativeOffset = 1;
		for (int i = 0; i < arrLen + 1; i++) {
			switch (offsetSize) {
				case 1:
					bos.write(relativeOffset & 0xFF);
					break;
				case 2:
					bos.write((relativeOffset >> 8) & 0xFF);
					bos.write(relativeOffset & 0xFF);
					break;
				case 3:
					bos.write((relativeOffset >> 16) & 0xFF);
					bos.write((relativeOffset >> 8) & 0xFF);
					bos.write(relativeOffset & 0xFF);
					break;
				default:
					bos.write((relativeOffset >>> 24) & 0xFF);
					bos.write((relativeOffset >> 16) & 0xFF);
					bos.write((relativeOffset >> 8) & 0xFF);
					bos.write(relativeOffset & 0xFF);
					break;
			}
			if (i < arrLen) {
				relativeOffset += byteArr[i].length;
			}
		}

		for (int i = 0; i < arrLen; i++) {
			bos.write(byteArr[i]);
		}
		return bos.toByteArray();

	}

	private static String getFromStringIndexData(String[] arr, int sid) {
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

	private static Dict parsePrivateDict(FileInput fb, Dict dict) throws IOException {
		Number[] values = dict.entries.get(PRIVATE);
		if (values != null && values.length > 1) {
			int len = values[0].intValue();
			int offset = values[1].intValue();
			if (len > 0) {
				byte[] temp = new byte[len];
				fb.moveTo(offset);
				fb.read(temp);
				Dict privateDict = new Dict(temp);
				Number[] subrsValues = privateDict.entries.get(SUBRS);
				if (subrsValues != null) {
					int subrOffset = subrsValues[0].intValue();
					if (subrOffset != 0) {
						int[] indexes = parseIndex(fb, subrOffset + offset);
						privateDict.subrsIndexData = indexToByteArray(fb, indexes);
					}
				}
				return privateDict;
			} else {
				dict.entries.remove(PRIVATE);
			}
		}
		return null;
	}

	private static void parseCharStrings(Dict dict, byte[][] charStringIndexData, byte[][] globalSubrsIndexData, byte[][] localSubrsIndexData) {
		double[] widths = new double[charStringIndexData.length];
		double[][] seacs = new double[charStringIndexData.length][];

		for (int i = 0, ii = charStringIndexData.length; i < ii; i++) {
			CIDState state = new CIDState();
			boolean valid = true;
			byte[][] localSubrToUse = null;
			if (dict.fdSelect != null && dict.fontDicts != null) {
				int fdIndex = dict.fdSelect.getFDIndex(i);
				if (fdIndex == -1) {
					valid = false;
				}
				if (fdIndex >= dict.fontDicts.length) {
					valid = false;
				}
				if (valid) {
					localSubrToUse = dict.fontDicts[fdIndex].privateDict.subrsIndexData;
				}
			} else if (localSubrsIndexData != null) {
				localSubrToUse = localSubrsIndexData;
			}
			if (valid) {
				valid = parseCharString(state, charStringIndexData[i], localSubrToUse, globalSubrsIndexData);
			}
			if (state.width != null) {
				widths[i] = state.width;
			}
			if (state.seac != null) {
				seacs[i] = state.seac;
			}
			if (!valid) {
				charStringIndexData[i] = new byte[14];
			}
		}
	}

	private static void updateWidthsNormal(byte[][] charStringIndexData, int[][] glyphWidths, Dict dict) {
		int defaultWidthX = 0;
		int nominalWidthX = 0;

		if (dict.privateDict != null) {
			Number[] values = dict.privateDict.entries.get(DEFAULTWIDTHX);
			if (values != null) {
				defaultWidthX = values[0].intValue();
			}
			values = dict.privateDict.entries.get(NOMINALWIDTHX);
			if (values != null) {
				nominalWidthX = values[0].intValue();
			}
		}
		for (int i = 0, ii = charStringIndexData.length; i < ii; i++) {
			Integer ww = findWidth(charStringIndexData[i]);
			glyphWidths[i][0] = ww == null ? defaultWidthX : ww + nominalWidthX;
		}
	}

//	private static void updateWidthsCID(byte[][] charStringIndexData, int[][] glyphWidths, Dict fontDict) {
//		
//	}
	private static Integer findWidth(byte[] data) {
		int stackSize = 0;
		Integer[] stack = new Integer[50];
		int len = data.length;
		for (int j = 0; j < len;) {
			int value = data[j++] & 0xff;
			if (debug && (value == 5 || value == 6 || value == 7 || value == 8
					|| value == 12 || value == 25 || value == 26 || value == 27
					|| value == 30 || value == 31)) {
				System.out.println("Width calculation error command found " + value);
			}
			switch (value) {
				case 1: // hstem
				case 3: // vstem
					return (stackSize % 2 != 0) ? stack[0] : null;
				case 4: // vmoveto
					return (stackSize > 1) ? stack[0] : null;
				case 5: // rlineto					
				case 6: // hlineto
				case 7: // vlineto
				case 8: // rrcurveto
					return null;
				case 10: // callsubr
					return (stackSize == 2) ? stack[0] : null;
				case 11:
					return null;
				case 12:
					int vv = data[j++] & 0xff;
					switch (vv) {
						case 3: // and
						case 4: // or
							return stackSize == 3 ? stack[0] : null;
						case 5: // not
							return stackSize == 2 ? stack[0] : null;
						case 9: // abs
							return stackSize == 2 ? stack[0] : null;
						case 10: // add
						case 11: // sub
						case 12: // div
							return stackSize == 3 ? stack[0] : null;
						case 14: // neg
							return stackSize == 2 ? stack[0] : null;
						case 15: // eq
							return stackSize == 3 ? stack[0] : null;
						case 18: // drop
							return stackSize == 2 ? stack[0] : null;
						case 22: // ifelse
							return stackSize == 5 ? stack[0] : null;
						case 23: // random
							return stackSize == 2 ? stack[0] : null;
						case 24: // mul
							return stackSize == 3 ? stack[0] : null;
						case 26: // sqrt
						case 27: // dup
							return stackSize == 2 ? stack[0] : null;
						case 28: // exch
							return stackSize == 3 ? stack[0] : null;
						case 34: // hflex
							return stackSize == 8 ? stack[0] : null;
						case 35: // flex;
							return stackSize == 14 ? stack[0] : null;
						case 36: // hflex1
							return stackSize == 10 ? stack[0] : null;
						case 37: // flex1
							return stackSize == 12 ? stack[0] : null;
					}
					return null;
				case 14: // end char
					return (stackSize > 0) ? stack[0] : null;
				case 18: // hstemhm
				case 19: // hintmask
				case 20: // cntrmask
					return (stackSize % 2 != 0) ? stack[0] : null;
				case 21: // rmoveto
					return (stackSize > 2) ? stack[0] : null;
				case 22: // hmoveto
					return (stackSize > 1) ? stack[0] : null;
				case 23: // vstemhm
					return (stackSize % 2 != 0) ? stack[0] : null;
				case 24: // rcurveline
				case 25: // rlinecurve
				case 26: // vvcurveto
				case 27: // hhcurveto
					return null;
				case 28: // shortint
					stack[stackSize] = (((data[j] & 0xff) << 24) | ((data[j + 1] & 0xff) << 16)) >> 16;
					j += 2;
					stackSize++;
					break;
				case 29: //callgsubr
					return (stackSize == 2) ? stack[0] : null;
				case 30: // vhcurveto
				case 31: // hvcurveto
					return null;
				default:
					if (value >= 32 && value <= 246) {
						stack[stackSize] = value - 139;
						stackSize++;
					} else if (value >= 247 && value <= 254) {
						stack[stackSize] = (value < 251
								? ((value - 247) << 8) + (data[j] & 0xff) + 108
								: -((value - 251) << 8) - (data[j] & 0xff) - 108);
						j++;
						stackSize++;
					} else if (value == 255) {
						stack[stackSize] = (((data[j] & 0xff) << 24) | ((data[j + 1] & 0xff) << 16)
								| ((data[j + 2] & 0xff) << 8) | (data[j + 3] & 0xff)) / 65536;
						j += 4;
						stackSize++;
					} else {
						return null;
					}
			}
		}
		return stack[0];
	}

	private static boolean parseCharString(CIDState state, byte[] data, byte[][] localSubrToUse, byte[][] globalSubrsIndexData) {

		if (data == null || state.callDepths > 10) {
			return false;
		}
		int stackSize = state.stackSize;
		double[] stack = state.stack;
		int len = data.length;
		for (int j = 0; j < len;) {
			int value = data[j++] & 0xff;
			CCSV validationCommand = null;
			if (value == 12) {
				int q = data[j++] & 0xff;
				if (q == 0) { // dotsection removal
					data[j - 2] = (byte) 139;
					data[j - 1] = 22;
					stackSize = 0;
				} else {
					validationCommand = CS_VALIDATION2[q];
				}
			} else if (value == 28) { // number (16 bit)
				stack[stackSize] = (((data[j] & 0xff) << 24) | ((data[j + 1] & 0xff) << 16)) >> 16;
				j += 2;
				stackSize++;
			} else if (value == 14) {
				if (stackSize >= 4) {
					stackSize -= 4;
//						if (this.seacAnalysisEnabled) {
//							state.seac = stack.slice(stackSize, stackSize + 4);
//							return false;
//						}
				}
				validationCommand = CS_VALIDATION1[value];
			} else if (value >= 32 && value <= 246) {
				stack[stackSize] = value - 139;
				stackSize++;
			} else if (value >= 247 && value <= 254) {
				stack[stackSize] = (value < 251
						? ((value - 247) << 8) + (data[j] & 0xff) + 108
						: -((value - 251) << 8) - (data[j] & 0xff) - 108);
				j++;
				stackSize++;
			} else if (value == 255) {
				stack[stackSize] = (((data[j] & 0xff) << 24) | ((data[j + 1] & 0xff) << 16)
						| ((data[j + 2] & 0xff) << 8) | (data[j + 3] & 0xff)) / 65536;
				j += 4;
				stackSize++;
			} else if (value == 19 || value == 20) {
				state.hints += stackSize >> 1;
				j += (state.hints + 7) >> 3;
				stackSize %= 2;
				validationCommand = CS_VALIDATION1[value];
			} else if (value == 10 || value == 29) {
				byte[][] subrsIndex;
				if (value == 10) {
					subrsIndex = localSubrToUse;
				} else {
					subrsIndex = globalSubrsIndexData;
				}
				if (subrsIndex == null) {
					return false;
				}
				int bias = 32768;
				if (subrsIndex.length < 1240) {
					bias = 107;
				} else if (subrsIndex.length < 33900) {
					bias = 1131;
				}
				double subrNumber = stack[--stackSize] + bias;
				if (subrNumber < 0 || subrNumber >= subrsIndex.length) {
					return false;
				}
				state.stackSize = stackSize;
				state.callDepths++;
				boolean valid = parseCharString(state, subrsIndex[(int) subrNumber], localSubrToUse, globalSubrsIndexData);
				if (!valid) {
					return false;
				}
				state.callDepths--;
				stackSize = state.stackSize;
				continue;
			} else if (value == 11) {
				state.stackSize = stackSize;
				return true;
			} else {
				validationCommand = CS_VALIDATION1[value];
			}
			if (validationCommand != null) {
				if (validationCommand.stem) {
					state.hints += stackSize >> 1;
				}

				if (!state.undefStack && stackSize < validationCommand.min) {
					// not enough parameters
					return false;
				}

				if (state.firstStackClearing && validationCommand.stackClearing) {
					state.firstStackClearing = false;
					stackSize -= validationCommand.min;
					if (stackSize >= 2 && validationCommand.stem) {
						stackSize %= 2;
					} else if (stackSize > 1) {
						// too many stack size
					}
					if (stackSize > 0 && stack[stackSize - 1] >= 0) {
						state.width = stack[stackSize - 1];
					}
				}
				if (validationCommand.stackDelta != null) {
					if (validationCommand.hasFunction) {
						validationCommand.performFunction(validationCommand.id, stack, stackSize);
					}
					stackSize += validationCommand.stackDelta;
				} else if (validationCommand.stackClearing) {
					stackSize = 0;
				} else if (validationCommand.resetStack) {
					stackSize = 0;
					state.undefStack = false;
				} else if (validationCommand.undefStack) {
					stackSize = 0;
					state.undefStack = true;
					state.firstStackClearing = false;
				}
			}
		}
		state.stackSize = stackSize;
		return true;
	}

	private static CIDCharset parseCharset(FileInput fb, Number[] values,
			int charStringIndexLen, String[] stringIndexData, boolean isCID) throws IOException {
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

	private static CIDEncoding parseEncoding(FileInput fb, Number[] values, String[] charset) throws IOException {
		int pos = values == null || values.length == 0 ? 0 : values[0].intValue();

		int[] encoding = new int[65536];
		Arrays.fill(encoding, -1);
		boolean predefined = false;
		int format;
		byte[] raw = null;

		if (pos == 0 || pos == 1) {
			predefined = true;
			format = pos;
			String[] baseEncoding = pos != 0 ? EXPERT_ENCODING : STANDARD_ENCODING;
			for (int i = 0, ii = charset.length; i < ii; i++) {
				int index = -1;
				for (int j = 0; j < baseEncoding.length; j++) {
					if (charset[i].equals(baseEncoding[j])) {
						index = j;
					}
				}
				if (index != -1) {
					encoding[index] = i;
				}
				if (index != -1) {
					encoding[index] = i;
				}
			}
		} else {
			fb.moveTo(pos);
			format = fb.getU8();
			switch (format & 0x7f) {
				case 0:
					int glyphsCount = fb.getU8();
					for (int i = 1; i <= glyphsCount; i++) {
						encoding[fb.getU8()] = i;
					}
					break;
				case 1:
					int rangesCount = fb.getU8();
					int gid = 1;
					for (int i = 0; i < rangesCount; i++) {
						int start = fb.getU8();
						int left = fb.getU8();
						for (int j = start; j <= start + left; j++) {
							encoding[j] = gid++;
						}
					}
					break;
				default:
					throw new IOException("unsupported encoding found in CFF Data");

			}
			int end = fb.getPosition();
			int rawLen = end - pos;
			raw = new byte[rawLen];
			fb.moveTo(pos);
			fb.read(raw);

		}

		return new CIDEncoding(predefined, format, encoding, raw);

	}

	private static CIDFDSelect parseFDSelect(FileInput fb, Number[] values, int len) throws IOException {
		int pos = values[0].intValue();

		fb.moveTo(pos);
		int format = fb.getU8();
		int[] fdSelect = null;
		boolean invalidFirst = false;

		switch (format) {
			case 0:
				fdSelect = new int[len];
				for (int i = 0; i < len; i++) {
					fdSelect[i] = fb.getU8();
				}
				break;
			case 3:
				List<Integer> temp = new ArrayList<Integer>();
				int rangeCount = fb.getU16();
				for (int i = 0; i < rangeCount; i++) {
					int first = fb.getU16();
					if (i == 0 && first != 0) {
						invalidFirst = true;
						first = 0;
					}
					int fdIndex = fb.getU8();
					int next = fb.getU8() << 8 | fb.getU8();
					fb.moveTo(fb.getPosition() - 2);
					for (int j = first; j < next; j++) {
						temp.add(fdIndex);
					}
				}
				fdSelect = new int[temp.size()];
				for (int i = 0; i < fdSelect.length; i++) {
					fdSelect[i] = temp.get(i);
				}
				break;
		}

		byte[] raw = new byte[len];
		fb.moveTo(pos);
		fb.read(raw);

		if (invalidFirst) {
			raw[3] = raw[4] = 0;
		}

		return new CIDFDSelect(fdSelect, raw);

	}

	private static String[] indexToStringArray(FileInput fb, int[] index) throws IOException {
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

	private static byte[][] indexToByteArray(FileInput fb, int[] index) throws IOException {
		if (index.length > 1) {
			byte[][] result = new byte[index.length - 1][];
			for (int i = 0, ii = index.length - 1; i < ii; i++) {
				int len = index[i + 1] - index[i];
				if (len < 0) {
					return result;
				}
				result[i] = new byte[len];
				fb.moveTo(index[i]);
				fb.read(result[i]);
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
			System.err.println("No Dict Found");
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

	private static byte[] encodeInt(int value) {
		byte[] code;
		if (value >= -107 && value <= 107) {
			code = new byte[1];
			code[0] = (byte) (value + 139);
		} else if (value >= 108 && value <= 1131) {
			code = new byte[2];
			value -= 108;
			code[0] = (byte) ((value >> 8) + 247);
			code[1] = (byte) (value & 0xFF);
		} else if (value >= -1131 && value <= -108) {
			code = new byte[2];
			value = -value - 108;
			code[0] = (byte) ((value >> 8) + 251);
			code[1] = (byte) (value & 0xFF);
		} else if (value >= -32768 && value <= 32767) {
			code = new byte[3];
			code[0] = 0x1c;
			code[1] = (byte) ((value >> 8) & 0xFF);
			code[2] = code[1] = (byte) (value & 0xFF);
		} else {
			code = new byte[5];
			code[0] = 0x1d;
			code[1] = (byte) ((value >> 24) & 0xFF);
			code[2] = (byte) ((value >> 16) & 0xFF);
			code[3] = (byte) ((value >> 8) & 0xFF);
			code[4] = (byte) (value & 0xFF);
		}
		return code;
	}

	private static byte[] encodeFloat(float fv) {
		String str = Float.toString(fv);
		char[] values = str.toCharArray();
		StringBuilder nib = new StringBuilder();
		for (int i = 0; i < values.length; ++i) {
			char a = values[i];
			switch (a) {
				case 'c':
					nib.append(values[++i] == '-' ? 'c' : 'b');
					break;
				case '.':
					nib.append('a');
					break;
				case '-':
					nib.append('e');
					break;
				default:
					nib.append(a);
					break;
			}
		}
		if ((nib.length() & 1) != 0) {
			nib.append('f');
		} else {
			nib.append("ff");
		}
		String nibStr = nib.toString();
		byte[] results = new byte[(nibStr.length() / 2) + 1];
		results[0] = 30;
		int p = 1;
		for (int i = 0, ii = nibStr.length(); i < ii; i += 2) {
			results[p++] = (byte) (Integer.parseInt(nibStr.substring(i, i + 2), 16));
		}
		return results;
	}

	public static void writeOTF(FileOutput fo, byte[] cffData, CIDType1C font) throws IOException {
		// write sfnt version
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Dict topDict = font.topDicts[0];

		int firstCharIndex = 0;
		int lastCharIndex = 0;

		// write CFF Table
		// write os2 Table
	}

	public static void main(String[] args) throws IOException {
		java.io.File file = new java.io.File("C:\\Users\\suda\\Desktop\\created\\results\\test11.cff");
		java.io.File file2 = new java.io.File("C:\\Users\\suda\\Desktop\\created\\results\\suda.cff");
		CIDType1C font = parse(new FileInput(file));
		byte[] bb = fix(font, "mingle");
		FileOutput fo = new FileOutput(file2);
		fo.write(bb);
		fo.close();
		parse(new FileInput(file2));
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
