package org.rackspace.capman.tools.ca.gui.utils;

import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;

public class ButtonGroupMapper {

    private ButtonGroup buttonGroup;
    private Map<ButtonModel, Integer> radioMap;

    public ButtonGroupMapper() {
        buttonGroup = new ButtonGroup();
        radioMap = new HashMap<ButtonModel, Integer>();
    }

    public void add(JRadioButton rb, int id) {
        buttonGroup.add(rb);
        ButtonModel buttonModel = rb.getModel();
        radioMap.put(buttonModel, id);
    }

    public int getSelectedId() {
        ButtonModel buttonModel = buttonGroup.getSelection();
        if (buttonModel == null) {
            return -1;
        }
        if (!radioMap.containsKey(buttonModel)) {
            return -2;
        }
        return radioMap.get(buttonModel);
    }
}
