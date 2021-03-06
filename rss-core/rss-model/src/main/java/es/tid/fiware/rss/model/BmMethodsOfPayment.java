/**
 * Revenue Settlement and Sharing System GE
 * Copyright (C) 2011-2014,  Javier Lucio - lucio@tid.es
 * Telefonica Investigacion y Desarrollo, S.A.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.tid.fiware.rss.model;

// Generated 10-feb-2012 11:04:29 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * BmMethodsOfPayment generated by hbm2java.
 */
@Entity
@Table(name = "bm_methods_of_payment")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class BmMethodsOfPayment implements java.io.Serializable {

    private long nuMopId;
    private String txName;
    private String txDescription;
    private String txCode;
    private Set<BmPbMop> bmPbMops = new HashSet<BmPbMop>(0);
    private Set<BmServdeployMop> bmServdeployMops = new HashSet<BmServdeployMop>(0);
    private Set<BmObMop> bmObMops = new HashSet<BmObMop>(0);
    private Set<BmCustomerType> bmCustomerTypes = new HashSet<BmCustomerType>(0);

    /**
     * Constructor.
     */
    public BmMethodsOfPayment() {
    }

    /**
     * Constructor.
     * 
     * @param nuMopId
     * @param txName
     * @param txCode
     */
    public BmMethodsOfPayment(long nuMopId, String txName, String txCode) {
        this.nuMopId = nuMopId;
        this.txName = txName;
        this.txCode = txCode;
    }

    /**
     * Constructor.
     * 
     * @param nuMopId
     * @param txName
     * @param txDescription
     * @param txCode
     * @param bmPbMops
     * @param bmServdeployMops
     * @param bmObMops
     * @param bmCustomerTypes
     */
    public BmMethodsOfPayment(long nuMopId, String txName, String txDescription, String txCode, Set<BmPbMop> bmPbMops,
        Set<BmServdeployMop> bmServdeployMops, Set<BmObMop> bmObMops, Set<BmCustomerType> bmCustomerTypes) {
        this.nuMopId = nuMopId;
        this.txName = txName;
        this.txDescription = txDescription;
        this.txCode = txCode;
        this.bmPbMops = bmPbMops;
        this.bmServdeployMops = bmServdeployMops;
        this.bmObMops = bmObMops;
        this.bmCustomerTypes = bmCustomerTypes;
    }

    @Id
    @Column(name = "NU_MOP_ID", unique = true, nullable = false, precision = 10, scale = 0)
    public long getNuMopId() {
        return this.nuMopId;
    }

    public void setNuMopId(long nuMopId) {
        this.nuMopId = nuMopId;
    }

    @Column(name = "TX_NAME", nullable = false, length = 20)
    public String getTxName() {
        return this.txName;
    }

    public void setTxName(String txName) {
        this.txName = txName;
    }

    @Column(name = "TX_DESCRIPTION", length = 200)
    public String getTxDescription() {
        return this.txDescription;
    }

    public void setTxDescription(String txDescription) {
        this.txDescription = txDescription;
    }

    @Column(name = "TX_CODE", length = 20)
    public final String getTxCode() {
        return txCode;
    }

    public final void setTxCode(String txCode) {
        this.txCode = txCode;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bmMethodsOfPayment")
    public Set<BmPbMop> getBmPbMops() {
        return this.bmPbMops;
    }

    public void setBmPbMops(Set<BmPbMop> bmPbMops) {
        this.bmPbMops = bmPbMops;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bmMethodsOfPayment")
    public Set<BmServdeployMop> getBmServdeployMops() {
        return this.bmServdeployMops;
    }

    public void setBmServdeployMops(Set<BmServdeployMop> bmServdeployMops) {
        this.bmServdeployMops = bmServdeployMops;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bmMethodsOfPayment")
    public Set<BmObMop> getBmObMops() {
        return this.bmObMops;
    }

    public void setBmObMops(Set<BmObMop> bmObMops) {
        this.bmObMops = bmObMops;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bmMethodsOfPayment")
    public Set<BmCustomerType> getBmCustomerTypes() {
        return this.bmCustomerTypes;
    }

    public void setBmCustomerTypes(Set<BmCustomerType> bmCustomerTypes) {
        this.bmCustomerTypes = bmCustomerTypes;
    }

}
