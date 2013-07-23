/*
 * Copyright 2009-2012 Alan Kennedy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 */

package com.xhaus.jyson;

import org.python.core.*;

public class JysonDecoder

{

	/** Controls whether the Jyson decoder accepts data other than object or array at the top level */
	public boolean accept_any_primary_datum = false;

	/** Controls whether the Jyson decoder accepts dangling commas on arrays and dicts ("[1,2,3,]" or "{"hello":"world",}") */
	public boolean accept_dangling_commas = false;

	/** Controls whether the Jyson decoder accepts shell style comments (" # This is a comment ") */
	public boolean accept_shell_style_comments = false;

	/** Controls whether the Jyson decoder accepts quotes delimited with single quote characters ('is this a string?')*/
	public boolean accept_single_quoted_strings = false;

	/** Controls whether the Jyson decoder accepts hexadecimal character escapes ("A" == "0x41") */
	public boolean accept_hex_char_escapes = false;

	/** Controls whether the Jyson decoder accepts hexadecimal integer constants (255 == 0xFF == 0xff) */
	public boolean accept_hexadecimal_integers = false;

	/** Controls whether the Jyson decoder accepts octal integer constants (0100 == 64 == 0x40) */
	public boolean accept_octal_integers = false;

	/** Controls whether the Jyson decoder accepts extraneous data after primary expression */
	public boolean accept_junk_after_data = false;

	protected int curr_pos;

	protected String json_text;

	protected JysonDecoder(String s)
	{
		curr_pos = 0;
		json_text = s;
	}

	private void reset_defaults()
	{
		accept_any_primary_datum = false;
		accept_dangling_commas = false;
		accept_shell_style_comments = false;
		accept_single_quoted_strings = false;
		accept_hex_char_escapes = false;
		accept_hexadecimal_integers = false;
		accept_octal_integers = false;
		accept_junk_after_data = false;
	}

	/**
	* Set the JysonDecoder into STRICT mode. In strict mode, the Jyson decoder will NOT accept
	*
	* <ol>
	* <li>A primary datum other than an object or an array ("{}" or "[]")</li>
	* <li>Dangling commas on objects or arrays ('{"1": 2,}' or "[1,]")</li>
	* <li>Shell style comments (" # This is a comment ")</li>
	* <li>Single quoted strings ('is this a string?')</li>
	* <li>Hexadecimal character escapes ("A" == "0x41")</li>
	* <li>Hexadecimal integer constants (255 == 0xFF == 0xff)</li>
	* <li>Octal integer constants (0100 == 64 == 0x40)</li>
	* <li>Junk after primary expression ('[1] "extra data"')</li>
	* </ol>
	*
	*/

	public void strict_mode()
	{
		reset_defaults();
	}

	/**
	* Set the JysonDecoder into PERMISSIVE mode. In permissive mode, the Jyson decoder WILL accept
	*
	* <ol>
	* <li>Any primary datum, i.e. data that is not an object or an array ("1")</li>
	* <li>Dangling commas on objects or arrays ('{"1": 2,}' or "[1,]")</li>
	* <li>Shell style comments (" # This is a comment ")</li>
	* <li>Single quoted strings ('is this a string?')</li>
	* <li>Hexadecimal character escapes ("A" == "0x41")</li>
	* <li>Hexadecimal integer constants (255 == 0xFF == 0xff)</li>
	* <li>Octal integer constants (0100 == 64 == 0x40)</li>
	* <li>Junk after primary expression ('[1] "extra data"')</li>
	* </ol>
	*
	*/

	public void permissive_mode()
	{
		accept_any_primary_datum = true;
		accept_dangling_commas = true;
		accept_shell_style_comments = true;
		accept_single_quoted_strings = true;
		accept_hex_char_escapes = true;
		accept_hexadecimal_integers = true;
		accept_octal_integers = true;
		accept_junk_after_data = true;
	}

	protected void push()
	{
		if (curr_pos > 0)
			{ curr_pos -= 1; }
	}

	protected char get_char ( )
	{
		if (curr_pos < json_text.length())
			return json_text.charAt(curr_pos++);
		else
			return 0;
	}

	protected String get_chars ( int n, String desc )
		throws JSONDecodeError
	{
		try
		{
			String next = json_text.substring(curr_pos, curr_pos+n);
			curr_pos += n;
			return next;
		}
		catch (IndexOutOfBoundsException ioobe)
			{ throw decode_exception("Ran out of characters reading "+desc); }
	}

	protected char get_data_char()
		throws JSONDecodeError
	{
		while (true)
		{
			char c = get_char();
			switch (c)
			{
				case '/':
					switch (get_char())
					{
						case '/':
							do {
								c = get_char();
							} while (c != '\n' && c != '\r' && c != 0);
							while (c == '\n' || c == '\r')
								c = get_char();
							if (c != 0) push();
							break;
						case '*':
							while (true)
								{
								c = get_char();
								if (c == 0)
									{ throw decode_exception("Unclosed comment."); }
								if (c == '*')
									{
									if (get_char() == '/')
										{ break; }
									push();
									}
								}
							break;
						default:
							push();
							return '/';
					}
					break;
				case '#':
					if (accept_shell_style_comments)
						{
						do {
							c = get_char();
						} while (c != '\n' && c != '\r' && c != 0);
						while (c == '\n' || c == '\r')
							c = get_char();
						if (c != 0) push();
						}
					else
						throw decode_exception("Shell style comments are not accepted");
					break;
				case 0:
					return c;
				default:
					if (c > ' ')
						return c;
			}
		}
	}

	protected String get_string(char quote)
		throws JSONDecodeError
	{
		char c;
		StringBuffer buf = new StringBuffer();
		while (true)
		{
			c = get_char();
			switch (c)
			{
				case '\\':
					c = get_char();
					switch (c)
						{
						case 'b':
							buf.append('\b');
							break;
						case 'f':
							buf.append('\f');
							break;
						case 'n':
							buf.append('\n');
							break;
						case 'r':
							buf.append('\r');
							break;
						case 't':
							buf.append('\t');
							break;
						case '\\':
							buf.append('\\');
							break;
						case '"':
							buf.append('"');
							break;
						case '/':
							buf.append('/');
							break;
						case 'u':
							String unichars = get_chars(4, "Unicode escape");
							try
								{ buf.append((char)Integer.parseInt(unichars, 16)); }
							catch (NumberFormatException nfx)
								{ throw decode_exception("Illegal character in unicode hex constant: " + unichars); }
							break;
						case 'x' :
							if (accept_hex_char_escapes)
								buf.append((char) Integer.parseInt(get_chars(2, "Hexadecimal escape"), 16));
							else
								throw decode_exception("Hexadecimal escapes for characters are not accepted");
							break;
						default:
							throw decode_exception("Illegal escape character: '"+c+"'");
					}
					break;
				case 0:
				case '\n':
				case '\r':
					throw decode_exception("Line terminators must be escaped inside strings");
				case '\'':
				case '"':
					if (c == quote)
						{ return buf.toString(); }
					// else let it flow into the default case
				default:
					buf.append(c);
			}
		}
	}

	protected PyObject decode_constant ( String s )
		throws JSONDecodeError
	{
		if (s.compareTo("true") == 0)
			{ return Py.True; }
		if (s.compareTo("false") == 0)
			{ return Py.False; }
		if (s.compareTo("null") == 0)
			{ return Py.None; }
		if (s.length() == 0)
			{ throw decode_exception("No value specified"); }
		return null;
	}

	protected PyObject decode_number ( String s )
		throws JSONDecodeError
	{
		char first = s.charAt(0);
		if (Character.isDigit(first) || ".-+".indexOf(first) != -1)
		{
			if (first == '0' && s.length() > 1)
			{
				if (s.charAt(1) == 'x' || s.charAt(1) == 'X')
				{
					if (accept_hexadecimal_integers)
					{
						String hexchars = s.substring(2);
						try
							{ return new PyInteger(Integer.parseInt(hexchars, 16)); }
						catch (NumberFormatException nfx)
							{ throw decode_exception("Format error in hexadecimal constant: " + hexchars); }
					}
					else
						throw decode_exception("Hexadecimal integers are not accepted.");
				}
				if (s.charAt(1) != '.')
				{
					if (accept_octal_integers)
						{
						try
							{ return new PyInteger(Integer.parseInt(s, 8)); }
						catch (NumberFormatException nfx)
							{ throw decode_exception("Format error in octal constant: " + s); }
						}
					else
						{ throw decode_exception("Octal integers are not accepted."); }
				}
			}
			String possible_number = s;
			if (possible_number.charAt(0) == '+')
				possible_number = possible_number.substring(1);
			try
				{ return new PyInteger(Integer.parseInt(possible_number));	}
			catch (Exception e)
				{}
			try
				{ return new PyLong(possible_number); }
			catch (Exception e)
				{}
			try
				{
				double result = Double.parseDouble(possible_number);
				return new PyFloat(result);
				}
			catch (NumberFormatException nfx)
				{}
		}
		return null;
	}

	protected PyStringMap get_json_object( )
		throws JSONDecodeError
	{
		char c;
		String key;

		PyStringMap json_object = new PyStringMap(); // PyStringMaps accept only string keys, like JSON
		while (true)
		{
			c = get_data_char();
			switch (c)
			{
				case 0:
					throw decode_exception("A JSON object must end with '}'");
				case '}':
					return json_object;
				case '\'':
					if (accept_single_quoted_strings)
						key = get_string('\'');
					else
						throw decode_exception("Single quoted strings are not acceptable in JSON");
					break;
				case '"':
					key = get_string('"');
					break;
				default:
					throw decode_exception("Only strings are acceptable as object keys in JSON");
			}
			c = get_data_char();
			if (c != ':')
				{ throw decode_exception("Object keys and values must be separated by ':'"); }
			PyObject value = get_object();
			json_object.__setitem__(new PyUnicode(key), value);
			switch (get_data_char())
			{
				case ',':
					if (get_data_char() == '}')
						{
						if (accept_dangling_commas)
							return json_object;
						else
							throw decode_exception("Commas after last entry of object not accepted");
						}
					push();
					break;
				case '}':
					return json_object;
				default:
					throw decode_exception("Expected a ',' or '}'");
			}
		}
	}

	protected PyList get_json_array ()
		throws JSONDecodeError
	{
		char next = get_data_char();
		if (next == 0)
			{ throw decode_exception("Ran out of characters reading array"); }

		PyList json_array = new PyList();
		if ( next == ']')
			{ return json_array; }
			
		push();
		while (true)
		{
			if (get_data_char() == ',')
				{ throw decode_exception("Arrays may not contain consecutive or dangling commas"); }
			push();
			json_array.append(get_object());
			switch (get_data_char())
			{
				case 0:
					{ throw decode_exception("Ran out of characters reading array"); }
				case ',':
					if (get_data_char() == ']')
						{
						if (accept_dangling_commas)
							return json_array;
						else
							throw decode_exception("Commas after last element of array not accepted");
						}
					push();
					break;
				case ']':
					return json_array;
				default:
					throw decode_exception("Array elements must be followed by ',' or ']'");
			}
		}
	}

	protected PyObject get_object()
		throws JSONDecodeError
	{
		char c = get_data_char();

		switch (c)
		{
			case '{':
				return get_json_object();
			case '[':
				return get_json_array();
			case '"':
				return new PyUnicode(get_string(c));
			case '\'':
				if (accept_single_quoted_strings)
					return new PyUnicode(get_string(c));
				else
					throw decode_exception("Single quoted strings are not accepted");
		}

		// OK, we have unquoted text. Try to figure out what to do with it

		StringBuffer buf = new StringBuffer();
		while (c >= ' ' && ",:]}/\\[{#".indexOf(c) == -1)
		{
			buf.append(c);
			c = get_char();
		}
		if (c != 0) push();

		String s = buf.toString().trim();
		
		// Check if it is one of the known constants
		
		PyObject result = decode_constant(s);
		if (result != null)
			return result;

		// Try to convert it to a number

		result = decode_number(s);
		if (result != null)
			return result;

		throw decode_exception("Unable to decode '"+s+"'");
	}

	protected PyObject get_top_level_object ( )
		throws JSONDecodeError
	{
		PyObject result = get_object();
		if (!(result instanceof  PyStringMap || result instanceof PyList) && 
			!accept_any_primary_datum)
			throw decode_exception("JSON expressions must strictly be either objects or lists");
		char ch = get_data_char();
		if (ch != 0 && !accept_junk_after_data)
			throw decode_exception("Only whitespace is permitted after the primary datum: not '"+ch+"'");
		return result;
	}

	protected JSONDecodeError decode_exception(String message)
	{
		return new JSONDecodeError(message+": position="+curr_pos);
	}

}
