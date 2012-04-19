package org.openstack.atlas.atom.pojo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="notes")
    public class NotesList {

        @XmlElement(name="note")
        private List<Note> notes = new ArrayList<Note>();

        public NotesList() {
        }

        @XmlTransient
        public List<Note> getNotes() {
            return notes;
        }

        public void setNotes(List<Note> notes) {
            this.notes = notes;
        }

    }