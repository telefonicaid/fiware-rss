/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014, Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package es.tid.fiware.rss.exceptionhandles;

import java.net.URI;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import es.tid.fiware.rss.common.test.DatabaseLoader;
import es.tid.fiware.rss.exception.RSSException;
import es.tid.fiware.rss.model.ExceptionTypeBean;

/**
 * 
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:database.xml"})
public class FactoryResponseTest {
    /**
     * Logging system.
     */
    private static Logger logger = LoggerFactory.getLogger(FactoryResponseTest.class);
    /**
     * For database access.
     */
    @Autowired
    private DataSource dataSource;
    /**
     * 
     */
    @Autowired
    private DatabaseLoader databaseLoader;
    /**
     * Properties.
     */
    @Resource(name = "rssProps")
    private Properties rssProps;

    /**
     * 
     */
    @Test
    public void createResponseError() {
        RSSException exception = new RSSException("RssException");
        Response response = FactoryResponse.createResponseError(exception, "exceptObj");
        Assert.assertTrue(true);
    }

    /**
     * 
     */
    @Test
    public void createErrorMsg() {
        FactoryResponse factory = new FactoryResponse();
        RSSException exception = new RSSException("RssException");
        String response = FactoryResponse.createErrorMsg(exception);
        Assert.assertTrue(true);
    }

    /**
     * 
     */
    @Test
    public void exceptionJson() throws Exception {
        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));
        RSSException exception = new RSSException("RssException");
        ExceptionTypeBean bean = FactoryResponse.exceptionJson(rssProps, mockUriInfo,
            exception, "resource");
        Assert.assertTrue(true);
    }

    /**
     * 
     */
    @Test
    public void createResponseErrorJson() throws Exception {
        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));
        Response bean = FactoryResponse.createResponseErrorJson(rssProps, mockUriInfo,
            "message", "resource");
        Assert.assertTrue(true);
    }

    /**
     * 
     */
    @Test
    public void catchNewConnectionJson() throws Exception {
        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));
        GenericJDBCException exception = new GenericJDBCException("sql", new SQLException("reason"));
        Response bean = FactoryResponse.catchNewConnectionJson(rssProps, mockUriInfo,
            exception, "message", "resource");
        Assert.assertTrue(true);
    }

    /**
     * 
     */
    @Test
    public void catchConnectionJDBCJson() throws Exception {
        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));
        JDBCConnectionException exception = new JDBCConnectionException("sql", new SQLException("reason"));
        Response bean = FactoryResponse.catchConnectionJDBCJson(rssProps, mockUriInfo,
            exception, "resource", "txId");
        Assert.assertTrue(true);
    }

    /**
     * 
     */
    @Test
    public void catchExceptionJson() throws Exception {
        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));
        RSSException exception = new RSSException("RssException");
        Response bean = FactoryResponse.catchExceptionJson(rssProps, mockUriInfo, exception,
            "resource");
        Assert.assertTrue(true);
    }
}
