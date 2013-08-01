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

public class JysonEncoder
{

	/** Controls whether the Jyson encoder emits unicode or ascii strings */
	public boolean emit_ascii = false;

	protected JysonEncoder ( )
	{
	}

	//
	// This code has been pinched from the jython source base: org.core.python.PyString
	// And modified to
	// 1. Always use double quotes, never single
	// 2. Output control characters as unicode escapes, not \x escapes
	// 

    private static char[] hexdigit = "0123456789ABCDEF".toCharArray();

	protected void append_json_string_repr ( StringBuffer buf, String str )
	{
		int size = str.length();
		StringBuffer v = new StringBuffer(str.length());
		char quote = '"';

		buf.append(quote);
		for (int i = 0; size-- > 0; )
		{
			int ch = str.charAt(i++);
			/* Escape quotes */
			if (ch == quote || ch == '\\')
			{
				buf.append('\\');
				buf.append((char) ch);
			}
			else if (ch == '\n') buf.append("\\n");
			else if (ch == '\t') buf.append("\\t");
			else if (ch == '\b') buf.append("\\b");
			else if (ch == '\f') buf.append("\\f");
			else if (ch == '\r') buf.append("\\r");
			else if (ch < ' ' || (ch >= 127 && emit_ascii) || 
			        (ch >= Character.MIN_SURROGATE && ch <= Character.MAX_SURROGATE))
			{
				/* Map control and non ascii characters to '\\uxxxx' */
				buf.append("\\u");
				buf.append(hexdigit[(ch >> 12) & 0xf]);
				buf.append(hexdigit[(ch >> 8) & 0xf]);
				buf.append(hexdigit[(ch >> 4) & 0xf]);
				buf.append(hexdigit[ch & 15]);
			}
			/* Copy everything else as-is */
			else
				buf.append((char) ch);
		}
		buf.append(quote);
	}

	protected void append_json_map_repr ( StringBuffer buf, PyObject map, PyList keys )
		throws JSONEncodeError
	{
		int num_keys = keys.__len__();
		buf.append('{');
		for (int ix = 0 ; ix < num_keys ; ix++)
		{
			PyObject k = keys.__getitem__(ix);
			if (!(k instanceof PyString))
				throw new JSONEncodeError(((PyType)k.fastGetClass()).fastGetName()+" objects are not permitted as JSON object keys.");
			append_json_string_repr(buf, ((PyString)k).toString());
			buf.append(':');
			buf.append(json_repr(map.__finditem__(k)));
			if (ix < num_keys-1)
				buf.append(',');
		}
		buf.append('}');
	}

	protected void append_json_string_map_repr ( StringBuffer buf, PyStringMap map )
		throws JSONEncodeError
	{
		append_json_map_repr(buf, map, map.keys());
	}

	protected void append_json_dictionary_repr ( StringBuffer buf, PyDictionary map )
		throws JSONEncodeError
	{
		append_json_map_repr(buf, map, map.keys());
	}

	protected void append_json_sequence_repr ( StringBuffer buf, PySequence sequence )
		throws JSONEncodeError
	{
		int num_items = sequence.__len__();
		buf.append('[');
		for (int ix = 0 ; ix < num_items ; ix++)
		{
			buf.append(json_repr(sequence.__getitem__(ix)));
			if (ix < num_items-1)
				buf.append(',');
		}
		buf.append(']');
	}

	public void append_json_repr ( StringBuffer buf, PyObject py_obj )
		throws JSONEncodeError
	{
		if (py_obj instanceof PyString)
			append_json_string_repr(buf, ((PyString)py_obj).toString());
		// Must test for PyBoolean before PyInteger because former is a subclass of latter.
		else if (py_obj instanceof PyBoolean)
			buf.append(((PyBoolean)py_obj).getBooleanValue() ? "true" : "false");
		else if (py_obj instanceof PyInteger)
			 buf.append(Integer.toString(((PyInteger)py_obj).getValue()));
		else if (py_obj instanceof PyLong)
			{
			String repr = ((PyLong)py_obj).__repr__().toString();
			buf.append(repr.substring(0, repr.length()-1));
			}
		else if (py_obj instanceof PyFloat)
			buf.append(Double.toString(((PyFloat)py_obj).getValue()));
		else if (py_obj instanceof PyStringMap)
			append_json_string_map_repr(buf, (PyStringMap)py_obj);
		else if (py_obj instanceof PyDictionary)
			append_json_dictionary_repr(buf, (PyDictionary)py_obj);
		else if (py_obj instanceof PySequence)
			append_json_sequence_repr(buf, (PySequence)py_obj);
		else if (py_obj instanceof PyNone)
			buf.append("null");
		else if (py_obj.__findattr__("__json__") != null &&
			py_obj.__findattr__("__json__").isCallable())
				buf.append(((PyMethod)py_obj.__findattr__("__json__")).__call__().toString());
		else
			throw new JSONEncodeError("Python '"+((PyType)py_obj.fastGetClass()).fastGetName()
				+"' object '"+py_obj.__repr__()+"' is not encodable in JSON");
	}

	public String json_repr ( PyObject py_obj )
		throws JSONEncodeError
	{
		StringBuffer buf = new StringBuffer();
		append_json_repr(buf, py_obj);
		return buf.toString();
	}

}
