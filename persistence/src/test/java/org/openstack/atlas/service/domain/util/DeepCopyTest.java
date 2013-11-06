package org.openstack.atlas.service.domain.util;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeepCopyTest {

    @Test
    public void shouldMakeACopyWhenOneLevelDeep() {
        Integer original = 1;
        Integer copy = (Integer) DeepCopy.copy(original);

        Assert.assertNotSame(copy, original);
    }

    @Test
    public void shouldMakeACopyWhenTwoLevelsDeep() {
        Integer original = 1;
        List<Integer> originalList = new ArrayList<Integer>();
        originalList.add(original);
        List<Integer> copyList = (List<Integer>) DeepCopy.copy(originalList);

        Assert.assertNotSame(copyList, originalList);
        Assert.assertNotSame(copyList.get(0), original);
    }
}
