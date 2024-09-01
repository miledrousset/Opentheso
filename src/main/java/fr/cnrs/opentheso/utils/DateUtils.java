package fr.cnrs.opentheso.utils;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Persee team
 */
public class DateUtils {

	public String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date date = new java.util.Date();
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		return dateFormat.format(sqlDate);
	}

	private Date getCurrentSqlDate() {
		var date = new java.util.Date();
		return new java.sql.Date(date.getTime());
	}

	public java.sql.Date getDateFromString(String date) {
		try {
			java.util.Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(date);
			return new Date(date1.getTime());
		} catch (Exception e) {
		}
		// en cas d'erreur, on retourne la date du jour
		return getCurrentSqlDate();
	}
}
