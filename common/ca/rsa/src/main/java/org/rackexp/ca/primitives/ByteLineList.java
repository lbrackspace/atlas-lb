package org.rackexp.ca.primitives;

import java.util.ArrayList;
import java.util.List;


public class ByteLineList {
    private List<ByteLineListEntry> lines;

    public ByteLineList(){
        lines = new ArrayList<ByteLineListEntry>();
    }

    public boolean addLine(byte[] line){
        return lines.add(new ByteLineListEntry(line));
    }

    public int size(){
        return lines.size();
    }

    public boolean empty(){
        return (lines.size() <= 0);
    }

    public byte[] toBytes(){
        int i;
        int length=0;
        int j=0;
        byte[] out = null;
        for(ByteLineListEntry byteLine : lines){
            length += byteLine.getBytes().length;
        }
        out = new byte[length];
        for(ByteLineListEntry byteLine : lines){
            byte[] line = byteLine.getBytes();
            for(i=0;i<line.length;i++){
                out[j] = line[i];
                j++;
            }
        }
        return out;
    }
}
