package org.openstack.atlas.atom.pojo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="note")
    @XmlType
    public class Note {

        private Long id;
        private String text;

        public Note() {
        }

        public Note(Long id, String text) {
            this.id = id;
            this.text = text;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

    }