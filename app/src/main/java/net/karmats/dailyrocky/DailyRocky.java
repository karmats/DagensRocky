package net.karmats.dailyrocky;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.karmats.dailyrocky.builder.HTMLBuilder;
import net.karmats.dailyrocky.constant.ComicType;
import net.karmats.dailyrocky.exceptions.RockyStripNotFoundException;
import net.karmats.dailyrocky.service.ComicReaderService;
import net.karmats.dailyrocky.view.DailyRockDialogPicker;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class DailyRocky extends Activity {

    private static final String MIN_DATE_STRING = "20100301";

    private static int DATE_DIALOG_ID = 0;

    private static final SimpleDateFormat STRIP_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final String DATE_PRESENTATION_FORMAT = "EEEE MMMM d, yyyy";

    // The comic strip web view
    private WebView webView;
    // Text presented at the top
    private TextView textView;
    // The buttons
    private Button previousButton;
    private ImageButton calendarButton;
    private Button todayButton;
    private Button nextButton;
    private ImageButton changeComicTypeButton;
    // Progressdialog for the application
    private ProgressDialog progressDialog;
    // The datepicker
    private DailyRockDialogPicker datePickerDialog;

    // The current date
    private Calendar currentDate;

    // The comic type
    private ComicType comicType;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getLastNonConfigurationInstance() != null) {
            currentDate = (Calendar) getLastNonConfigurationInstance();
        } else {
            currentDate = Calendar.getInstance();
        }
        comicType = ComicType.BLACK_WHITE;

        setContentView(R.layout.main);

        textView = (TextView) findViewById(R.id.dateText);
        setConfigSettings(getResources().getConfiguration().orientation);

        // Put the Rocky strip on the scrollable view
        // scrollView = (Scroll) findViewById(R.id.imageView);
        webView = (WebView) findViewById(R.id.webView);

        // Add action listener to the previous button
        previousButton = (Button) findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back one day
                currentDate.add(Calendar.DATE, -1);
                dispatchStripThread();
            }
        });

        // Add action listener to the calendar button
        calendarButton = (ImageButton) findViewById(R.id.calendarButton);
        calendarButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // Add action listener to the dagens button
        todayButton = (Button) findViewById(R.id.todayButton);
        todayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set todays date
                currentDate = Calendar.getInstance();
                dispatchStripThread();
            }
        });

        // Add action listener to the next button
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go forward one date
                currentDate.add(Calendar.DATE, 1);
                dispatchStripThread();
            }
        });

        changeComicTypeButton = (ImageButton) findViewById(R.id.colorBw);
        changeComicTypeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comicType.equals(ComicType.BLACK_WHITE)) {
                    comicType = ComicType.COLOR;
                    dispatchStripThread();
                    changeComicTypeButton.setImageResource(R.drawable.rocky_bw);
                } else {
                    comicType = ComicType.BLACK_WHITE;
                    dispatchStripThread();
                    changeComicTypeButton.setImageResource(R.drawable.rocky_color);
                }
            }
        });

        dispatchStripThread();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return currentDate;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DATE_DIALOG_ID) {
            datePickerDialog = new DailyRockDialogPicker(this, dateSetListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH),
                                                         currentDate.get(Calendar.DAY_OF_MONTH));
            return datePickerDialog;
        }
        return null;
    }

    /**
     * Called when configuration has changed, used for when user switch orientation
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setConfigSettings(newConfig.orientation);
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        // onDateSet method
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // Check to see that the dates is in the interval 1 march - today
            if (!isValidDate(year, monthOfYear, dayOfMonth)) {
                displayText(getString(R.string.date_range));
                updateDateDialog();
                return;
            }
            // Convert the inputs to a calendar object
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            currentDate = c;
            dispatchStripThread();
        }
    };

    protected final void dispatchStripThread() {
        progressDialog = ProgressDialog.show(DailyRocky.this, null, getString(R.string.progress_text), true);
        new Thread() {
            @Override
            public void run() {
                try {
                    // Try get the date strip
                    String url = ComicReaderService.getInstance().getRockyUrl(currentDate.getTime(), comicType);
                    webView.loadDataWithBaseURL(ComicReaderService.ROCKY_BLACK_WHITE_URL, HTMLBuilder.buildImageHtmlImageView(url), "text/html", "utf-8", null);
                    webView.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int newProgress) {
                            // We are done
                            if (newProgress == 100) {
                                handler.sendEmptyMessage(0);
                            } else {
                                progressDialog.setMessage(getText(R.string.progress_text2));
                            }
                        };
                    });
                } catch (RockyStripNotFoundException rsnfe) {
                    webView.loadData(HTMLBuilder.buildRockyNotFoundView(currentDate.getTime()), "text/html", "utf-8");
                    handler.sendEmptyMessage(0);
                }
            }
        }.start();
    }

    private void updateButtonsAndTextView() {
        String current = STRIP_DATE_FORMAT.format(currentDate.getTime());
        previousButton.setEnabled(!current.equals(MIN_DATE_STRING));
        todayButton.setEnabled(!current.equals(STRIP_DATE_FORMAT.format(new Date())));
        nextButton.setEnabled(!current.equals(STRIP_DATE_FORMAT.format(new Date())));
        textView.setText(DateFormat.format(DATE_PRESENTATION_FORMAT, currentDate.getTime()));
    }

    // Creates a Toast in the bottom
    private void displayText(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private boolean isValidDate(int year, int month, int day) {
        // Check back in time
        if (year < 2010 || (year == 2010 && month < 2)) {
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        Calendar now = Calendar.getInstance();
        // Check that the date isn't after today
        if (cal.after(now)) {
            return false;
        }
        return true;
    }

    private void updateDateDialog() {
        if (datePickerDialog != null) {
            datePickerDialog.updateDate(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
        }
    }

    private void setConfigSettings(int mode) {
        if (mode == Configuration.ORIENTATION_LANDSCAPE) {
            textView.setVisibility(TextView.GONE);
        } else {
            textView.setVisibility(TextView.VISIBLE);
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            updateButtonsAndTextView();
            updateDateDialog();
            progressDialog.dismiss();
        }
    };
}