package org.openstack.atlas.api.helpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.cfg.RestApiConfiguration;
import org.w3.atom.Link;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class PaginationHelperTest {

    public static class WhenDeterminingPageOffset {

        @Test
        public void shouldReturnDefaultOffsetWhenOffsetIsNull() {
            Integer offset = PaginationHelper.determinePageOffset(null);
            Assert.assertEquals(PaginationHelper.DEFAULT_PAGE_OFFSET, offset);
        }

        @Test
        public void shouldReturnMinOffsetWhenOffsetIsBelowMin() {
            Integer offset = PaginationHelper.determinePageOffset(PaginationHelper.MIN_PAGE_OFFSET - 1);
            Assert.assertEquals(PaginationHelper.MIN_PAGE_OFFSET, offset);
        }

        @Test
        public void shouldReturnSameOffsetWhenOffsetIsNormal() {
            Integer originalOffset = 500;
            Integer offset = PaginationHelper.determinePageOffset(originalOffset);
            Assert.assertEquals(originalOffset, offset);
        }
    }

    public static class WhenDeterminingPageLimit {

        @Test
        public void shouldReturnDefaultLimitWhenLimitIsNull() {
            Integer limit = PaginationHelper.determinePageLimit(null);
            Assert.assertEquals(PaginationHelper.DEFAULT_PAGE_LIMIT, limit);
        }

        @Test
        public void shouldReturnMinLimitWhenLimitIsBelowMin() {
            Integer limit = PaginationHelper.determinePageLimit(PaginationHelper.MIN_PAGE_LIMIT - 1);
            Assert.assertEquals(PaginationHelper.MIN_PAGE_LIMIT, limit);
        }

        @Test
        public void shouldReturnMaxLimitWhenLimitIsAboveMax() {
            Integer limit = PaginationHelper.determinePageLimit(PaginationHelper.MAX_PAGE_LIMIT + 1);
            Assert.assertEquals(PaginationHelper.MAX_PAGE_LIMIT, limit);
        }

        @Test
        public void shouldReturnSameLimitWhenLimitIsWithinBounds() {
            Integer originalLimit = PaginationHelper.DEFAULT_PAGE_LIMIT;
            Integer limit = PaginationHelper.determinePageLimit(originalLimit);
            Assert.assertEquals(originalLimit, limit);
        }
    }

    public static class WhenCalculatingNextOffset {

        @Test
        public void shouldReturnSecondPageWhenOffsetAndLimitAreNull() {
            Integer offset = PaginationHelper.calculateNextOffset(null, null);
            final Integer expectedOffset = PaginationHelper.DEFAULT_PAGE_LIMIT + PaginationHelper.DEFAULT_PAGE_OFFSET;
            Assert.assertEquals(expectedOffset, offset);
        }

        @Test
        public void shouldReturnProperOffsetWhenParametersAreNormal() {
            Integer originalOffset = 10;
            Integer originalLimit = 100;
            Integer newOffset = PaginationHelper.calculateNextOffset(originalOffset, originalLimit);
            final Integer expectedOffset = originalOffset + originalLimit;
            Assert.assertEquals(expectedOffset, newOffset);
        }
    }

    public static class WhenCalculatingPreviousOffset {

        @Test
        public void shouldReturnDefaultOffsetWhenOffsetAndLimitAreNull() {
            Integer offset = PaginationHelper.calculatePreviousOffset(null, null);
            Assert.assertEquals(PaginationHelper.DEFAULT_PAGE_OFFSET, offset);
        }

        @Test
        public void shouldReturnZeroWhenOffsetLessThanLimit() {
            Integer originalOffset = 10;
            Integer originalLimit = 100;
            Integer newOffset = PaginationHelper.calculatePreviousOffset(originalOffset, originalLimit);
            Assert.assertEquals(new Integer(0), newOffset);
        }

        @Test
        public void shouldReturnZeroWhenOffsetEqualsLimit() {
            Integer originalOffset = 100;
            Integer originalLimit = 100;
            Integer newOffset = PaginationHelper.calculatePreviousOffset(originalOffset, originalLimit);
            Assert.assertEquals(new Integer(0), newOffset);
        }

        @Test
        public void shouldReturnProperOffsetWhenOffsetGreaterThanLimit() {
            Integer originalOffset = 101;
            Integer originalLimit = 100;
            Integer newOffset = PaginationHelper.calculatePreviousOffset(originalOffset, originalLimit);
            Assert.assertEquals(new Integer(1), newOffset);
        }
    }

    public static class WhenCreatingLink {
        private RestApiConfiguration configuration;
        private PaginationHelper paginationHelper;
        private String mockedBaseUri = "http://mocked.api.endpoint/v1.0";

        @Before
        public void setup() {
            configuration = mock(RestApiConfiguration.class);
            doReturn(true).when(configuration).hasKeys(PublicApiServiceConfigurationKeys.base_uri);
            when(configuration.getString(PublicApiServiceConfigurationKeys.base_uri)).thenReturn(mockedBaseUri);

            paginationHelper = new PaginationHelper();
            paginationHelper.setRestApiConfiguration(configuration);
        }

        @Test
        public void shouldCreateProperHref() {
            final String ref = "next";
            final String relativeUri = "/foo/bar/baz";
            Link link = PaginationHelper.createLink(ref, relativeUri);
            Assert.assertEquals(mockedBaseUri + relativeUri, link.getHref());
        }
    }
}