package info.elexis.server.core.internal.jaxrs;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateSerializer implements JsonSerializer<Date> {
	
	@Override
	public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context){
		return new JsonPrimitive(format(src, false, TIMEZONE_UTC));
	}
	
	// following code from com.google.gson.internal.bind.util.ISO8601Utils
	private final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");
	public String format(Date date, boolean millis, TimeZone tz){
		Calendar calendar = new GregorianCalendar(tz, Locale.US);
		calendar.setTime(date);
		
		// estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
		int capacity = "yyyy-MM-ddThh:mm:ss".length();
		capacity += millis ? ".sss".length() : 0;
		capacity += tz.getRawOffset() == 0 ? "Z".length() : "+hh:mm".length();
		StringBuilder formatted = new StringBuilder(capacity);
		
		padInt(formatted, calendar.get(Calendar.YEAR), "yyyy".length());
		formatted.append('-');
		padInt(formatted, calendar.get(Calendar.MONTH) + 1, "MM".length());
		formatted.append('-');
		padInt(formatted, calendar.get(Calendar.DAY_OF_MONTH), "dd".length());
		formatted.append('T');
		padInt(formatted, calendar.get(Calendar.HOUR_OF_DAY), "hh".length());
		formatted.append(':');
		padInt(formatted, calendar.get(Calendar.MINUTE), "mm".length());
		formatted.append(':');
		padInt(formatted, calendar.get(Calendar.SECOND), "ss".length());
		if (millis) {
			formatted.append('.');
			padInt(formatted, calendar.get(Calendar.MILLISECOND), "sss".length());
		}
		
		int offset = tz.getOffset(calendar.getTimeInMillis());
		if (offset != 0) {
			int hours = Math.abs((offset / (60 * 1000)) / 60);
			int minutes = Math.abs((offset / (60 * 1000)) % 60);
			formatted.append(offset < 0 ? '-' : '+');
			padInt(formatted, hours, "hh".length());
			formatted.append(':');
			padInt(formatted, minutes, "mm".length());
		} else {
			formatted.append('Z');
		}
		
		return formatted.toString();
	}
	
	/**
	 * Zero pad a number to a specified length
	 * 
	 * @param buffer
	 *            buffer to use for padding
	 * @param value
	 *            the integer value to pad if necessary.
	 * @param length
	 *            the length of the string we should zero pad
	 */
	private void padInt(StringBuilder buffer, int value, int length){
		String strValue = Integer.toString(value);
		for (int i = length - strValue.length(); i > 0; i--) {
			buffer.append('0');
		}
		buffer.append(strValue);
	}
	
}