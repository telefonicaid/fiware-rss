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
package es.tid.fiware.rss.expenditureLimit.server.test;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import es.tid.fiware.rss.common.properties.AppProperties;
import es.tid.fiware.rss.common.test.DatabaseLoader;
import es.tid.fiware.rss.expenditureLimit.api.LimitGroupBean;
import es.tid.fiware.rss.expenditureLimit.server.ExpenditureLimitServer;
import es.tid.fiware.rss.expenditureLimit.server.service.ExpenditureLimitDataChecker;
import es.tid.fiware.rss.expenditureLimit.test.ExpenditureLimitBeanConstructor;
import es.tid.fiware.rss.oauth.model.ValidatedToken;
import es.tid.fiware.rss.oauth.service.OauthManager;

/**
 * 
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:database.xml", "classpath:cxf-beans.xml"})
public class ExpenditureLimitServerTest {
    /**
     * Logging system.
     */
    private static Logger logger = LoggerFactory.getLogger(ExpenditureLimitServer.class);
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
     * 
     */
    @Autowired
    private AppProperties appProperties;
    /**
     * 
     */
    @Autowired
    private ExpenditureLimitServer server;
    /**
     * 
     */
    private OauthManager oauth;
    /**
     * 
     */
    private ExpenditureLimitDataChecker checker;
    /**
     * Data
     */
    private final String appProvider = "app123456";
    private final String currency = "EUR";
    private final String type = "daily";
    private final String service = "ServiceTest1";
    private final String userId = "userId01";

    /**
     * 
     */
    @PostConstruct
    public void init() throws Exception {
        ValidatedToken validToken = new ValidatedToken();
        validToken.setEmail("mail@mail.com");
        oauth = Mockito.mock(OauthManager.class);
        Mockito.when(oauth.checkAuthenticationToken("authToken")).thenReturn(validToken);
        checker = (ExpenditureLimitDataChecker) ReflectionTestUtils.getField(server, "checker");
        ReflectionTestUtils.setField(checker, "oauthManager", oauth);
        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getBaseUri()).thenReturn(new URI("http://www.test.com/go"));
        ReflectionTestUtils.setField(server, "ui", mockUriInfo);
    }

    /**
     * Method to insert data before test.
     * 
     * @throws Exception
     *             from db
     */
    @Before
    public void setUp() throws Exception {
        databaseLoader.cleanInsert("dbunit/CREATE_DATATEST_EXPLIMIT.xml", true);
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        databaseLoader.deleteAll("dbunit/CREATE_DATATEST_EXPLIMIT.xml", true);
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void getUserExpLimits() throws Exception {
        Response response = server.getUserExpLimits("authToken", appProvider, userId,
            service, currency, type);
        Assert.assertEquals(200, response.getStatus());
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void getProviderExpLimits() throws Exception {
        Response response = server.getProviderExpLimits("authToken", appProvider, service, currency, type);
        Assert.assertEquals(200, response.getStatus());
    }

    /**
     * @throws Exception
     */
    @Test
    public void storeGeneralProviderExpLimits() throws Exception {
        ExpenditureLimitServerTest.logger.debug("Into storeGeneralProviderExpLimits method");
        LimitGroupBean expLimits = ExpenditureLimitBeanConstructor.generateLimitGroupBean();
        Response response = server.createModifProviderExpLimit("Authtoken", appProvider, expLimits);
        Assert.assertEquals(201, response.getStatus());
    }

    /**
     * @throws Exception
     */
    @Test
    public void storeGeneralUserExpLimits() throws Exception {
        ExpenditureLimitServerTest.logger.debug("Into storeGeneralUserExpLimits method");
        LimitGroupBean expLimits = ExpenditureLimitBeanConstructor.generateLimitGroupBean();
        Response response = server.createModifUserExpLimit("Authtoken", appProvider, userId, expLimits);
        Assert.assertEquals(201, response.getStatus());
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void deleteProviderExpLimits() throws Exception {
        ExpenditureLimitServerTest.logger.debug("Into deleteProviderExpLimits method");
        Response response = server.deleteProviderExpLimits("authToken", appProvider, service, currency, type);
        Assert.assertEquals(200, response.getStatus());
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void deleteGeneralUserExpLimits() throws Exception {
        ExpenditureLimitServerTest.logger.debug("Into deleteUserAccumulated method");
        Response response = server.deleteUserExpCtrl("authToken", appProvider, userId, service, currency, type);
        Assert.assertEquals(200, response.getStatus());
    }
}
