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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.tid.fiware.rss.dao.DbeAggregatorAppProviderDao;
import es.tid.fiware.rss.dao.DbeAggregatorDao;
import es.tid.fiware.rss.dao.DbeAppProviderDao;
import es.tid.fiware.rss.dao.DbeTransactionDao;
import es.tid.fiware.rss.dao.SetRevenueShareConfDao;
import es.tid.fiware.rss.model.DbeAggregator;
import es.tid.fiware.rss.model.DbeAggregatorAppProvider;
import es.tid.fiware.rss.model.DbeAggregatorAppProviderId;
import es.tid.fiware.rss.model.DbeAppProvider;
import es.tid.fiware.rss.model.DbeTransaction;
import es.tid.fiware.rss.model.RSSFile;
import es.tid.fiware.rss.model.SetRevenueShareConf;
import es.tid.fiware.rss.model.SetRevenueShareConfId;

@Service
public class SettlementManager {

    /***
     * Logging system.
     */
    private final Logger logger = LoggerFactory.getLogger(SettlementManager.class);
    /**
     * 
     */
    @Autowired
    private DbeAggregatorAppProviderDao aggregatorAppProviderDao;
    /**
     * 
     */
    @Autowired
    /**
     * 
     */
    private DbeAppProviderDao appProviderDao;
    /**
     * 
     */
    @Autowired
    private SetRevenueShareConfDao revenueShareConfDao;
    /**
     * 
     */
    @Autowired
    private DbeTransactionDao transactionDao;
    /**
     * 
     */
    @Autowired
    private DbeAggregatorDao aggregatorDao;
    /**
     * 
     */
    private Runtime runtime;
    /**
     * 
     */
    @Resource(name = "rssProps")
    private Properties rssProps;

    /**
     * 
     * @throws Exception
     */
    @PostConstruct
    private void init() throws Exception {
        runtime = Runtime.getRuntime();
    }

    /**
     * Launch settlement process.
     * 
     * @param startPeriod
     * @param endPeriod
     * @param aggregatorId
     * @throws IOException
     */
    public void runSettlement(String startPeriod, String endPeriod, String aggregatorId, String providerId)
        throws IOException {
        logger.debug("runSettlement - Provider: {} , aggregator: {}", providerId, aggregatorId);
        logger.debug("runSettlement - Start: Init" + startPeriod + ",End:" + endPeriod);
        String settlementScript = (String) rssProps.get("settlementScript");
        File settlementSH = new File(settlementScript);
        logger.debug("Running script: " + "." + settlementSH.getPath() + " " + startPeriod + " " + endPeriod);
        if (null != providerId && providerId.length() > 0) {
            DbeAppProvider provider = appProviderDao.getById(providerId);
            if (provider != null) {
                logger.debug("Running script for provider:{}", provider.getTxName().replace(" ", "_"));
                runtime.exec(settlementSH.getPath() + " " + startPeriod + " " + endPeriod + " "
                    + provider.getTxAppProviderId() + " " + provider.getTxName().replace(" ", "_"));
            }

        } else if (null != aggregatorId && aggregatorId.length() > 0) {
            List<DbeAggregatorAppProvider> provsAgg = aggregatorAppProviderDao
                .getDbeAggregatorAppProviderByAggregatorId(aggregatorId);
            if (null != provsAgg && provsAgg.size() > 0) {
                for (DbeAggregatorAppProvider provAgg : provsAgg) {
                    logger.debug("Running script for provider:{}",
                        provAgg.getDbeAppProvider().getTxName().replace(" ", "_"));
                    runtime.exec(settlementSH.getPath() + " " + startPeriod + " " + endPeriod + " "
                        + provAgg.getDbeAppProvider().getTxAppProviderId() + " "
                        + provAgg.getDbeAppProvider().getTxName().replace(" ", "_"));
                }
            }
        } else {
            // run all reports
            List<DbeAppProvider> providers = appProviderDao.getAll();
            if (providers != null && providers.size() > 0) {
                for (DbeAppProvider provider : providers) {
                    runtime.exec("sh " + settlementSH.getPath() + " " + startPeriod + " " + endPeriod + " "
                        + provider.getTxAppProviderId() + " " + provider.getTxName().replace(" ", "_"));
                }
            }
        }
    }

    /**
     * Get settlement files from file System.
     * 
     * @return
     */
    public List<RSSFile> getSettlementFiles(String aggregatorId) {
        logger.debug("Into getSettlementFiles method.");
        List<RSSFile> rssFilesList = new ArrayList<RSSFile>();
        String reportsPath = (String) rssProps.get("reportsPath");
        if (null != aggregatorId && aggregatorId.length() > 0) {
            List<DbeAggregatorAppProvider> provsAgg = aggregatorAppProviderDao
                .getDbeAggregatorAppProviderByAggregatorId(aggregatorId);
            if (null != provsAgg && provsAgg.size() > 0) {
                String path;
                for (DbeAggregatorAppProvider provAgg : provsAgg) {
                    path = reportsPath + provAgg.getDbeAppProvider().getTxName().replace(" ", "_");
                    logger.debug("Path to search into: {}" + path);
                    rssFilesList.addAll(getSettlementFilesOfPath(path));
                }
            }
        } else {
            // Add all reports
            rssFilesList = getSettlementFilesOfPath(reportsPath);
        }
        if (rssFilesList.size() == 0) {
            RSSFile rssf = new RSSFile();
            rssf.setTxName("There are no RSS Files generated for you at this moment");
            logger.warn("ELSE: There are no RSS Files generated for you at this moment");
            rssf.setTxUrl("");
            rssFilesList.add(rssf);
        }
        return rssFilesList;
    }

    /**
     * Get files from path.
     * 
     * @param path
     * @return
     */
    public List<RSSFile> getSettlementFilesOfPath(String path) {
        // Opening/creating the folder
        File folder = new File(path);
        List<RSSFile> rssFilesList = new ArrayList<RSSFile>();
        RSSFile rssf = new RSSFile();

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            Arrays.sort(files);

            if (files.length > 0) {
                List<File> fileList = new ArrayList<File>(Arrays.asList(files));
                ListIterator<File> lit = fileList.listIterator();

                while (lit.hasNext()) {
                    File file = lit.next();
                    logger.info(file.getAbsolutePath());

                    if (file.isDirectory()) {
                        logger.debug("Is directory. Getting more files...");
                        File[] moreFiles = file.listFiles();
                        Arrays.sort(moreFiles);
                        if (moreFiles.length > 0) {
                            for (File f : moreFiles) {
                                lit.add(f);
                                lit.previous();
                            }
                        }
                    } else {
                        rssf = new RSSFile();
                        rssf.setTxName(file.getName());
                        rssf.setTxUrl(file.getAbsolutePath());
                        rssFilesList.add(rssf);
                        logger.debug("File added");
                    }
                }
            }
        }
        return rssFilesList;
    }

    /**
     * Get transactions from ddbb.
     * 
     * @param appProvider
     * @return
     * @throws Exception
     */
    public List<DbeTransaction> runSelectTransactions(String aggregatorId) throws Exception {
        List<DbeTransaction> transactions = new ArrayList<DbeTransaction>();
        if (null != aggregatorId && aggregatorId.length() > 0) {
            List<DbeAggregatorAppProvider> provsAgg = aggregatorAppProviderDao
                .getDbeAggregatorAppProviderByAggregatorId(aggregatorId);
            if (null != provsAgg && provsAgg.size() > 0) {
                List<DbeTransaction> transactionSelect;
                for (DbeAggregatorAppProvider provAgg : provsAgg) {
                    transactionSelect = transactionDao.getTransactionsByProviderId(provAgg.getDbeAppProvider()
                        .getTxAppProviderId());
                    if (null != transactionSelect && transactionSelect.size() > 0) {
                        transactions.addAll(transactionSelect);
                    }
                }
            }
        } else {
            transactions = transactionDao.getAll();
        }
        return transactions;
    }

    /**
     * Get providers from bbdd.
     * 
     * @return
     * @throws Exception
     */
    public List<DbeAppProvider> getProviders(String aggregatorId) throws Exception {
        List<DbeAppProvider> providers = new ArrayList<DbeAppProvider>();
        if (null != aggregatorId && aggregatorId.length() > 0) {
            List<DbeAggregatorAppProvider> provsAgg = aggregatorAppProviderDao
                .getDbeAggregatorAppProviderByAggregatorId(aggregatorId);
            if (null != provsAgg && provsAgg.size() > 0) {
                for (DbeAggregatorAppProvider provAgg : provsAgg) {
                    providers.add(provAgg.getDbeAppProvider());
                }
            }
        } else {
            providers = appProviderDao.getAll();
        }
        return providers;
    }

    /**
     * Get RS models from database.
     * 
     * @param appProvider
     * @return
     * @throws Exception
     */
    public List<SetRevenueShareConf> getRSModels(String aggregatorId) throws Exception {
        List<SetRevenueShareConf> models = new ArrayList<SetRevenueShareConf>();
        if (null != aggregatorId && aggregatorId.length() > 0) {
            List<DbeAggregatorAppProvider> provsAgg = aggregatorAppProviderDao
                .getDbeAggregatorAppProviderByAggregatorId(aggregatorId);
            if (null != provsAgg && provsAgg.size() > 0) {
                for (DbeAggregatorAppProvider provAgg : provsAgg) {
                    List<SetRevenueShareConf> modelsBBDD = revenueShareConfDao.getRevenueModelsByProviderId(provAgg
                        .getDbeAppProvider()
                        .getTxAppProviderId());
                    if (null != modelsBBDD) {
                        models.addAll(modelsBBDD);
                    }

                }
            }
        } else {
            models = revenueShareConfDao.getAll();
        }
        return models;
    }

    /**
     * Create provider.
     * 
     * @param providerId
     * @param providerName
     * @throws IOException
     */
    @Transactional
    public void runCreateProvider(String providerId, String providerName, String aggregatorId) throws Exception {
        logger.debug("Creating provider: {}", providerId);
        DbeAppProvider provider = new DbeAppProvider();
        provider.setTxAppProviderId(providerId);
        provider.setTxName(providerName);
        appProviderDao.create(provider);
        if (null != aggregatorId && aggregatorId.length() > 0) {
            DbeAggregatorAppProvider object = new DbeAggregatorAppProvider();
            DbeAggregatorAppProviderId id = new DbeAggregatorAppProviderId();
            object.setId(id);
            id.setTxAppProviderId(providerId);
            id.setTxEmail(aggregatorId);
            aggregatorAppProviderDao.create(object);
        }
    }

    /**
     * Create aggregator.
     * 
     * @param providerId
     * @param providerName
     * @throws IOException
     */
    @Transactional
    public void runCreateAggretator(String aggregatorId, String aggregatorName) throws Exception {
        logger.debug("Creating aggregator: {}", aggregatorId);
        DbeAggregator aggregator = new DbeAggregator();
        aggregator.setTxEmail(aggregatorId);
        aggregator.setTxName(aggregatorName);
        aggregatorDao.create(aggregator);
    }

    /**
     * Create RS Model.
     * 
     * @param providerId
     * @param productClass
     * @param revenue
     * @throws IOException
     */
    public void runCreateRSModel(String providerId, String productClass, Long revenue) throws IOException {
        logger.debug("Creating RsModel. Provider: {} productClass {}  revenue:",
            providerId, productClass, revenue.toString());
        SetRevenueShareConf rsModel = new SetRevenueShareConf();
        SetRevenueShareConfId id = new SetRevenueShareConfId();
        rsModel.setId(id);
        rsModel.setNuPercRevenueShare(BigDecimal.valueOf(revenue.longValue()));
        id.setTxAppProviderId(providerId);
        id.setProductClass(productClass);
        id.setNuObId(Long.valueOf(1));
        id.setCountryId(Long.valueOf(1));
        revenueShareConfDao.create(rsModel);
    }

    /**
     * Delete data from provider.
     * 
     * @param appProvider
     * @throws IOException
     */
    @Transactional
    public void runClean(String appProvider) throws IOException {
        logger.debug("Deleting  transactions. Provider: {}", appProvider);
        transactionDao.deleteTransactionsByProviderId(appProvider);
        String reportsPath = (String) rssProps.get("reportsPath");
        reportsPath = reportsPath + appProvider;
        File folder = new File(reportsPath);
        deleteFolder(folder);
    }

    /**
     * Delete folders.
     * 
     * @param folder
     */
    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
