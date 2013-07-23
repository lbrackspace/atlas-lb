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

public class JysonCodec
{

	public boolean strict_mode = true;

	/**
	* Decode the given JSON string, and return the corresponding Jython object (hierarchy)
	*
	* The behaviour of the decoder in relation to the incoming JSON expression is controlled by
	* several JysonCodec options. These options are exposed as public boolean attributes, which can be set
	* individually to <b>true</b> or <b>false</b>, or can be controlled as a group by the use of the 
	* <b>strict_mode()</b> and <b>permissive_mode()</b> methods.
	*
	* @param json_text A java.lang.String containing the JSON text to be decoded
	* @return The Jython object (hierarchy) corresponding to the JSON text
	* @throws JSONDecodeError If an error occurred while decoding the JSON text
	*
	*/

	public static PyObject loads ( PyObject[] args, String[] keywords )
		throws JSONDecodeError
	{
		String json_text = ((PyString)args[0]).toString();
		JysonDecoder decoder = new JysonDecoder(json_text);
	
		boolean strict_mode_arg;
		for (int kix = 0 ; kix < keywords.length ; kix++)
		{
			String keyword = keywords[kix];
			PyObject value = args[args.length-keywords.length+kix];
			if ("strict_mode".compareTo(keyword) == 0)
			{
				strict_mode_arg = value.__nonzero__();
				if (strict_mode_arg)
					decoder.strict_mode();
				else
					decoder.permissive_mode();
			}
			if ("accept_any_primary_datum".compareTo(keyword) == 0)
				decoder.accept_any_primary_datum = value.__nonzero__();
			if ("accept_dangling_commas".compareTo(keyword) == 0)
				decoder.accept_dangling_commas = value.__nonzero__();
			if ("accept_shell_style_comments".compareTo(keyword) == 0)
				decoder.accept_shell_style_comments = value.__nonzero__();
			if ("accept_single_quoted_strings".compareTo(keyword) == 0)
				decoder.accept_single_quoted_strings = value.__nonzero__();
			if ("accept_hex_char_escapes".compareTo(keyword) == 0)
				decoder.accept_hex_char_escapes = value.__nonzero__();
			if ("accept_hexadecimal_integers".compareTo(keyword) == 0)
				decoder.accept_hexadecimal_integers = value.__nonzero__();
			if ("accept_octal_integers".compareTo(keyword) == 0)
				decoder.accept_octal_integers = value.__nonzero__();
			if ("accept_junk_after_data".compareTo(keyword) == 0)
				decoder.accept_junk_after_data = value.__nonzero__();
		}
		return decoder.get_top_level_object();
	}

	/**
	* Encode the given Jython object into JSON, returning the corresponding JSON string.
	* <br/><br/>
	* There is a single option which controls the generated JSON string: <b>emit_ascii</b>.
	* <br/>
	* <ul>
	* <li>If the option is <b>false</b>, then a full Unicode string will be generated.</li>
	* <li>If the option is <b>true</b>, then any characters whose value is above 127 will be represented in 
	* the generated string as a Unicode escape (for example "&#xe1;" will be emiited as "&#x5c;u00E1").</li>
	* </ul>
	* <br/>
	* The following are notes about the encoding process
	* <br/>
	* <ol>
	* 	<li>Strings will always be emitted enclosed in double quotes (")</li>
	* 	<li>If the passed Jython object has a <b>__json__()</b> method, it will be called to generate the JSON corresponding to the object: It is the method implementers responsibility to ensure that the returned string is valid JSON: The return value is <b>not</b> checked for correctness.</li>
	* </ol>
	* <br/>
	* @param py_obj The org.python.core.PyObject to be encoded as JSON.
	* @return The JSON text corresponding to the Jython object (hierarchy) 
	* @throws JSONEncodeError If an error occurred while encoding the Jython object (hierarchy) 
	*/

	public static String dumps ( PyObject[] args, String[] keywords )
		throws JSONEncodeError
	{
		PyObject obj_to_encode = args[0];
		JysonEncoder encoder = new JysonEncoder();
		for (int kix = 0 ; kix < keywords.length ; kix++)
		{
			String keyword = keywords[kix];
			PyObject value = args[args.length-keywords.length+kix];
			if ("emit_ascii".compareTo(keyword) == 0)
				encoder.emit_ascii = value.__nonzero__();
		}
		return encoder.json_repr(obj_to_encode);
	}

}
