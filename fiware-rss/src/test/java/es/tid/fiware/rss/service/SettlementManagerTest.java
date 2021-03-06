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

package es.tid.fiware.rss.service;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import es.tid.fiware.rss.common.test.DatabaseLoader;
import es.tid.fiware.rss.model.DbeAppProvider;
import es.tid.fiware.rss.model.DbeTransaction;
import es.tid.fiware.rss.model.RSSFile;
import es.tid.fiware.rss.model.SetRevenueShareConf;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:database.xml", "/META-INF/spring/application-context.xml",
    "/META-INF/spring/cxf-beans.xml"})
public class SettlementManagerTest {
    /***
     * Logging system.
     */
    private final Logger logger = LoggerFactory.getLogger(SettlementManagerTest.class);
    /**
     * 
     */
    @Autowired
    private DatabaseLoader databaseLoader;
    /**
     * 
     */
    @Autowired
    private SettlementManager settlementManager;
    /**
     * 
     */
    private String providerId = "123456";
    /**
     * 
     */
    private String aggregatorId = "mail@mail.com";
    /**
     * 
     */
    private Runtime runtime;

    /**
     * Method to insert data before test.
     * 
     * @throws Exception
     *             from dbb
     */
    @Before
    public void setUp() throws Exception {
        databaseLoader.cleanInsert("dbunit/CREATE_DATATEST_TRANSACTIONS.xml", true);
        // prepare mockito
        Process p = Mockito.mock(Process.class);
        runtime = Mockito.mock(Runtime.class);
        Mockito.when(runtime.exec(Matchers.any(String.class))).thenReturn(p);
        ReflectionTestUtils.setField(unwrapSettlementManager(), "runtime", runtime);
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    private SettlementManager unwrapSettlementManager() throws Exception {
        if (AopUtils.isAopProxy(settlementManager) && settlementManager instanceof Advised) {
            Object target = ((Advised) settlementManager).getTargetSource().getTarget();
            return (SettlementManager) target;
        }
        return null;
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        databaseLoader.deleteAll("dbunit/CREATE_DATATEST_TRANSACTIONS.xml", true);
    }

    /**
    * 
    */
    @Test
    public void getSettlementFilesTest() {
        List<RSSFile> fileList = settlementManager.getSettlementFiles(null);
        if (fileList.isEmpty()) {
            Assert.fail("No files returned");
        } else if (fileList.size() == 1) {
            if (fileList.get(0).getTxName().equals("There are not RSS Files generated for you at this moment")) {
                Assert.fail("There are not RSS Files generated for you at this moment");
            }
        }

        fileList = settlementManager.getSettlementFiles(aggregatorId);
        if (fileList.isEmpty()) {
            Assert.fail("No files returned");
        } else if (fileList.size() == 1) {
            if (fileList.get(0).getTxName().equals("There are not RSS Files generated for you at this moment")) {
                Assert.fail("There are not RSS Files generated for you at this moment");
            }
        }
    }

    /**
     * 
     */
    @Test
    public void runSettlementTest() {
        logger.debug("Into runSettlementTest");
        try {
            logger.debug("run all providers");
            settlementManager.runSettlement("2013-01", "2013-12", null, null);
            logger.debug("run one provider");
            settlementManager.runSettlement("2013-01", "2013-12", null, providerId);
            logger.debug("run one aggregator/store");
            settlementManager.runSettlement("2013-01", "2013-12", aggregatorId, null);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void runSelectTransactionsTest() throws Exception {
        logger.debug("run all providers");
        List<DbeTransaction> transactions = settlementManager.runSelectTransactions(null);
        logger.debug("run aggregatorId");
        transactions = settlementManager.runSelectTransactions(aggregatorId);
        logger.debug("run no aggregatorId");
        transactions = settlementManager.runSelectTransactions("nonExistingAggregatorId");
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void getProvidersTest() throws Exception {
        logger.debug("run all providers");
        List<DbeAppProvider> providers = settlementManager.getProviders(null);
        Assert.assertNotNull(providers);
        logger.debug("run aggregatorId");
        providers = settlementManager.getProviders(aggregatorId);
        Assert.assertNotNull(providers);
        logger.debug("run no aggregatorId");
        providers = settlementManager.getProviders("nonExistingAggregatorId");
        Assert.assertTrue(providers.size() == 0);
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void getRSModelsTest() throws Exception {
        logger.debug("run all models");
        List<SetRevenueShareConf> models = settlementManager.getRSModels(null);
        Assert.assertNotNull(models);
        logger.debug("run models");
        models = settlementManager.getRSModels(aggregatorId);
        Assert.assertNotNull(models);
        logger.debug("run no models");
        models = settlementManager.getRSModels("nonExistingAggregatorId");
        Assert.assertTrue(models.size() == 0);
    }

    /**
     * 
     */
    @Test
    public void runCleanTest() throws IOException {
        logger.debug("runClean");
        try {
            settlementManager.runClean("appProvider");
        } catch (Exception e) {
            Assert.fail();
        }

    }

    /**
     * 
     */
    @Test
    public void createAggregatorTest() {
        logger.debug("runClean");
        try {
            settlementManager.runCreateAggretator("aggregatorId", "aggregatorName");
        } catch (Exception e) {
            Assert.fail();
        }

    }

    /**
     * 
     */
    @Test
    public void createRssModelTest() {
        logger.debug("createRssModelTest");
        try {
            settlementManager.runCreateRSModel(providerId, "newProductClass", new Long(30));
        } catch (Exception e) {
            Assert.fail();
        }

    }

    /**
     * 
     */
    @Test
    public void CreateProviderTest() {
        logger.debug("CreateProviderTest");
        try {
            logger.debug("run with aggregator");
            settlementManager.runCreateProvider("newProvider1", "providerName", null);
            logger.debug("run without aggregator");
            settlementManager.runCreateProvider("newProvider12", "providerName", aggregatorId);
        } catch (Exception e) {
            Assert.fail();
        }

    }

}
