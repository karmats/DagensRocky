package net.karmats.dailyrocky.view;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.widget.DatePicker;

public class DailyRockDialogPicker extends DatePickerDialog {
	
	public DailyRockDialogPicker(Context context, int theme,
			OnDateSetListener callBack, int year, int monthOfYear,
			int dayOfMonth) {
		super(context, theme, callBack, year, monthOfYear, dayOfMonth);
		setDateTitle(year, monthOfYear, dayOfMonth);
	}
	
	public DailyRockDialogPicker(Context context,
			OnDateSetListener callBack, int year, int monthOfYear,
			int dayOfMonth) {
		super(context, callBack, year, monthOfYear, dayOfMonth);
		setDateTitle(year, monthOfYear, dayOfMonth);
	}
	
	@Override
	public void onDateChanged(DatePicker view, int year, int month, int day) {
		setDateTitle(year, month, day);
	}
	
	// Sets the title as a date.
	private void setDateTitle(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		String title = DateFormat.format("EEEE MMMM d, yyyy", c.getTime()).toString();
		setTitle(title);
	}

}
