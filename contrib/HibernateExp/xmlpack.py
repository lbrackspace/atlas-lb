#!/usr/bin/env jython

from xml.dom.minidom import parse, parseString
from xml.dom import Node
import copy
import re

node_re = re.compile(".*NODE")
nodetypemap = {}

#Snatch a nodetypemap that contains a mapping from the value to an
#actutual string for debugging. 
#for example nodetypemap[1] will map to "ELEMENT_NODE"
for key in Node.__dict__.keys():	
    if node_re.match(key):
        nodetypemap[ Node.__dict__.get(key) ] = key

def shownodeType(node):
    return nodetypemap[node.nodeType]

def showtagName(node):
    if not hasattr(node,"tagName"):
        return ""
    return node.tagName

def scanNode(node,level = 0):
    msg = "Class = \"%s\" "%node.__class__.__name__
    msg += "NodeType=\"%s\""%shownodeType(node)
    if node.nodeType == Node.ELEMENT_NODE:
        msg += ", tag: \"%s\""%node.tagName
    msg += "attr{"
    if node.attributes:
        for (key,val)  in node.attributes.items():
            msg +=  "%s = %s "%(key,val)
    msg += "}"
    if node.nodeType == Node.TEXT_NODE:
        msg += "text: "
        if not node.wholeText.strip():
            msg += "{Spaces}"
        else:
            msg += "\"%s\""%(node.wholeText)
            
    
    print " " * level * 4, msg
    if node.hasChildNodes:
        for child in node.childNodes:
            scanNode(child,level + 1 )

def xml_escape(str_in):
    out = str_in.replace("&","&amp;")
    out = out.replace(">","&gt;")
    out = out.replace("<","&lt;")
    out = out.replace("\"","&quote;")
    return out

def xml_deescape(str_in):
    out = str_in.replace("&quote;","\"")
    out = out.replace("&lt;","<")
    out = out.replace("&gt;",">")
    out = out.replace("&amp;","&")
    return out    

class ConfigReader(object):
    def __init__(self,space=4):
        self.space=4
        self.typemap = { type(0)         : (" type=\"int\"","int"),
                         type(0.0)       : (" type=\"float\"","float"),
                         type(list())    : (" type=\"list\"","list"),
                         type({})        : (" type=\"dict\"","dict"),
                         type(tuple(())) : (" type=\"tuple\"","tuple"),
                         type(bool())    : (" type=\"bool\"","bool"),
                         type("")        : ("","str")
                       }

    def node_attrs(self,node):
        if node.attributes:
            return dict([(key,val) for (key,val) in node.attributes.items()])
        # Return an empty Dictionary so we can sainly search its 
        # empty list of keys
        return {} 

    def getattrtype(self,node):
        attrs = self.node_attrs(node)
        if "type" in attrs:
            return attrs["type"]
        else:
            return ""

    def getstr(self,node):
        return node.childNodes[0].wholeText #This should be a TEXT_NODE

    def getlist(self,node):
        out = []
        for child in node.childNodes:
            if child.nodeType == Node.ELEMENT_NODE:
                found_item = self.getitem(child)
                out.append(found_item)
        return out

    def gettuple(self,node):
        out = []
        for child in node.childNodes:
            if child.nodeType == Node.ELEMENT_NODE:
                found_item = self.getitem(child)
                out.append(found_item)
        return tuple(out)

    def getdict(self,node):
        out = {}
        for child in node.childNodes:
            if child.nodeType == Node.ELEMENT_NODE:
                 found_item = self.getitem(child)
                 out[child.tagName] = found_item
        return out
        
    def getitem(self,node):
        attrtype = self.getattrtype(node)
        if attrtype == "list":
            found_list = self.getlist(node)
            return found_list
        if attrtype == "tuple":
            found_tuple = self.gettuple(node)
            return found_tuple
        if attrtype == "dict":
            found_dict = self.getdict(node)
            return found_dict
        if attrtype == "int":
            found_str = self.getstr(node)
            return int(found_str)
        if attrtype == "float":
            found_str = self.getstr(node)
            return float(self.getstr(node))
        if attrtype == "bool":
            found_str = self.getstr(node)
            if found_str == "True":
                return True
            elif found_str == "False":
                return False
            else:
                 raise ValueError, "%s is not a bool"%found_str
                 return False
        if attrtype == "string":
            found_str = self.getstr(node)
            return found_str
        else:
            #Assume its a string otherwise
            found_str = self.getstr(node)
            return xml_deescape(found_str)

    def loads(self,xml_data):
        doc = parseString(xml_data)
        return self.parse(doc)

    def setspace(self,level):
        return " "*self.space*level

    def setlist(self,name,list_in,level):
        space = self.setspace(level)
        (found_type,type_str) = self.settype(list_in)
        xml_data  = "%s<%s%s>\n"%(space,name,found_type)
        for element in list_in:
            xml_data += self.setitem("le",element,level + 1 )
        xml_data  += "%s</%s>\n"%(space,name)
        return xml_data

    def settuple(self,name,tuple_in,level):
        space = self.setspace(level)
        (found_type,type_str) = self.settype(tuple_in)
        xml_data  = "%s<%s%s>\n"%(space,name,found_type)
        for element in tuple_in:
            xml_data += self.setitem("te",element,level + 1 )
        xml_data  += "%s</%s>\n"%(space,name)
        return xml_data

    def setdict(self,name,val,level):
        (found_type,type_str) = self.settype(val)
        space = self.setspace(level)
        xml_data  = "%s<%s%s>\n"%(space,name,found_type)
        for (key,val)  in val.items():
            xml_data += self.setitem(key,val,level + 1 )
        xml_data  += "%s</%s>\n"%(space,name)
        return xml_data
   
    def setitem(self,name,val,level):
        space = self.setspace(level)
        (found_type,type_str) = self.settype(val)
        if   type_str == "list":
            xml_data = self.setlist(name,val,level + 1)
        elif type_str == "tuple":
            xml_data = self.settuple(name,val,level + 1)
        elif type_str == "dict":
            xml_data = self.setdict(name,val,level + 1)
        elif type_str == "int":
            xml_data  = "%s<%s%s>"%(space,name,found_type)
            xml_data += "%i</%s>\n"%(val,name)
        elif type_str == "float":
            pass
            xml_data  = "%s<%s%s>"%(space,name,found_type)
            xml_data += "%f</%s>\n"%(val,name)
        elif type_str == "bool":
            xml_data  = "%s<%s%s>"%(space,name,found_type)
            xml_data += "%s</%s>\n"%(val,name)        
        else:
            #Assume this is a string
            str_out = xml_escape(val)
            xml_data  = "%s<%s%s>"%(space,name,found_type)
            xml_data += "%s</%s>\n"%(str_out,name)
        return xml_data

    def dumps(self,config):
        level = 0
        xml_data  = ""
        xml_data += "<?xml version=\"1.0\" ?>\n"
        xml_data += self.setitem("config",config,level + 0)
        return xml_data

    def write(self,filename,config):
        xml_data = self.dumps(config)
        fp = open(filename,"w")
        fp.write(xml_data)
        fp.close()

    def settype(self,item):
        if type(item) in self.typemap:
           (found_xml,found_type) = self.typemap[ type(item) ]
           return (found_xml,found_type)
        else:
            return ("",None)

    def read(self,filename):
        doc = parse(filename)
        return self.parse(doc)
    
    def parse(self,doc):
        config = {}
        root = doc.childNodes[0]
        for child in root.childNodes:
            attrtype = self.getattrtype(child)
            if child.nodeType == Node.ELEMENT_NODE:
                found_item = self.getitem(child)
                config[child.tagName] = found_item
        return config

def config_read(file_name):
    cr = ConfigReader()
    config = cr.read(file_name)
    return config 


def config_write(file_name,config):
    cr = ConfigReader()
    cr.write(file_name,config);

