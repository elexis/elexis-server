/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package info.elexis.server.core.connector.elexis.services.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ch.elexis.core.constants.Preferences;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.services.ConfigService;

/**
 * Class for manipulation of Multiplikator. <br>
 * 
 * @author thomashu
 * 
 */
public class MultiplikatorList {
	private java.util.List<MultiplikatorInfo> list = new ArrayList<MultiplikatorList.MultiplikatorInfo>();
	private String typ;
	private String table;

	public MultiplikatorList(String table, String typ) {
		this.typ = typ;
		this.table = table;
	}

	/**
	 * Update multiRes with ResultSet of all existing Multiplikators
	 */
	private void fetchResultSet() {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Query nativeQuery = em.createNativeQuery(
					"SELECT DATUM_VON, DATUM_BIS, MULTIPLIKATOR FROM " + table + " WHERE TYP=" + JdbcLink.wrap(typ));
			List<Object[]> resultList = nativeQuery.getResultList();
			for (Object[] object : resultList) {
				list.add(new MultiplikatorInfo(object[0].toString(), object[1].toString(), object[2].toString()));
			}
		} finally {
			em.close();
		}
	}

	public void insertMultiplikator(TimeTool dateFrom, String value) {
		TimeTool dateTo = null;

		String sqlString;

		fetchResultSet();
		Iterator<MultiplikatorInfo> iter = list.iterator();
		// update existing multiplier for that date
		while (iter.hasNext()) {
			MultiplikatorInfo info = iter.next();
			TimeTool fromDate = new TimeTool(info.validFrom);
			TimeTool toDate = new TimeTool(info.validTo);
			if (dateFrom.isAfter(fromDate) && dateFrom.isBefore(toDate)) { // if contains
																			// update the to
																			// value of the
																			// existing
																			// multiplikator
				StringBuilder sql = new StringBuilder();
				// update the old to date
				TimeTool newToDate = new TimeTool(dateFrom);
				newToDate.addDays(-1);
				sql.append("UPDATE ").append(table)
						.append(" SET DATUM_BIS=" + JdbcLink.wrap(newToDate.toString(TimeTool.DATE_COMPACT))
								+ " WHERE DATUM_VON=" + JdbcLink.wrap(fromDate.toString(TimeTool.DATE_COMPACT))
								+ " AND TYP=" + JdbcLink.wrap(typ));
				executeSqlString(sql.toString());
				// set to date of new multiplikator to to date of old multiplikator
				dateTo = new TimeTool(toDate);
			} else if (dateFrom.isEqual(fromDate)) { // if from equals update the value
				StringBuilder sql = new StringBuilder();
				// update the value and return
				TimeTool newToDate = new TimeTool(dateFrom);
				newToDate.addDays(-1);
				sql.append("UPDATE ").append(table)
						.append(" SET MULTIPLIKATOR=" + JdbcLink.wrap(value) + " WHERE DATUM_VON="
								+ JdbcLink.wrap(fromDate.toString(TimeTool.DATE_COMPACT)) + " AND TYP="
								+ JdbcLink.wrap(typ));
				executeSqlString(sql.toString());
				return;
			}
		}
		// if we have not found a to Date yet search for oldest existing
		if (dateTo == null) {
			fetchResultSet();
			iter = list.iterator();
			dateTo = new TimeTool("99991231");
			while (iter.hasNext()) {
				MultiplikatorInfo info = iter.next();
				TimeTool fromDate = new TimeTool(info.validFrom);
				if (fromDate.isBefore(dateTo)) {
					dateTo.set(fromDate);
					dateTo.addDays(-1);
				}
			}
		}
		// create a new entry
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(table)
				.append(" (ID,DATUM_VON,DATUM_BIS,MULTIPLIKATOR,TYP) VALUES ("
						+ JdbcLink.wrap(StringTool.unique("prso")) + ","
						+ JdbcLink.wrap(dateFrom.toString(TimeTool.DATE_COMPACT)) + ","
						+ JdbcLink.wrap(dateTo.toString(TimeTool.DATE_COMPACT)) + "," + JdbcLink.wrap(value) + ","
						+ JdbcLink.wrap(typ) + ");");
		executeSqlString(sql.toString());

	}

	private void executeSqlString(String string) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Query nativeQuery = em.createNativeQuery(string);
			em.getTransaction().begin();
			nativeQuery.executeUpdate();
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public void removeMultiplikator(TimeTool dateFrom, String value) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			Query statement = em.createNativeQuery(getPreparedStatementSql());
			em.getTransaction().begin();
			statement.setParameter(1, value);
			statement.setParameter(2, dateFrom.toString(TimeTool.DATE_COMPACT));
			statement.setParameter(3, typ);
			statement.executeUpdate();
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	private String getPreparedStatementSql() {
		return "DELETE FROM " + table + " WHERE MULTIPLIKATOR=? AND DATUM_VON=? AND TYP=?";
	}

	public synchronized double getMultiplikator(TimeTool date) {
		// get Mutliplikator for date
		fetchResultSet();
		Iterator<MultiplikatorInfo> iter = list.iterator();
		while (iter.hasNext()) {
			MultiplikatorInfo info = iter.next();
			TimeTool fromDate = new TimeTool(info.validFrom);
			TimeTool toDate = new TimeTool(info.validTo);
			if (date.isAfterOrEqual(fromDate) && date.isBeforeOrEqual(toDate)) {
				String value = info.multiplikator;
				if (value != null && !value.isEmpty()) {
					try {
						return Double.parseDouble(value);
					} catch (NumberFormatException nfe) {
						ExHandler.handle(nfe);
						return 0.0;
					}
				}
			}
		}
		return 1.0;
	}

	private static class MultiplikatorInfo {
		String validFrom;
		String validTo;
		String multiplikator;

		MultiplikatorInfo(String validFrom, String validTo, String multiplikator) {
			this.validFrom = validFrom;
			this.validTo = validTo;
			this.multiplikator = multiplikator;
		}
	}

	private static String[] getEigenleistungUseMultiSystems() {
		String systems = ConfigService.INSTANCE.get(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS, "");
		return systems.split("\\|\\|");
	}

	public static boolean isEigenleistungUseMulti(String system) {
		String[] systems = getEigenleistungUseMultiSystems();
		for (String string : systems) {
			if (system.equals(string)) {
				return true;
			}
		}
		return false;
	}

	public static void setEigenleistungUseMulti(String system) {
		String systems = ConfigService.INSTANCE.get(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS, "");
		if (!systems.isEmpty()) {
			systems = systems.concat("||");
		}
		systems = systems.concat(system);
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS, systems);
	}

	public static void removeEigenleistungUseMulti(String system) {
		String[] systems = getEigenleistungUseMultiSystems();
		StringBuilder sb = new StringBuilder();
		for (String string : systems) {
			if (!system.equals(string)) {
				if (!(sb.length() == 0)) {
					sb.append("||");
				}
				sb.append(string);
			}
		}
		ConfigService.INSTANCE.set(Preferences.LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS, sb.toString());
	}
}
