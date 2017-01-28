package com.github.alexeymorin.rtsiwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.widget.RemoteViews;

import org.xmlpull.v1.XmlPullParser;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of App Widget functionality.
 */
public class RTSIWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Security security) {
        Log.i("RTSIWidget", "updateAppWidget");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.rtsiwidget);
        views.setTextViewText(R.id.ticker, security.secId);
        views.setTextViewText(R.id.changeTextView, security.lastChangePrc);
        // getColor(int) is deprecation
        if (security.lastChangePrc.charAt(0) == '+') {
            views.setTextColor(R.id.changeTextView, context.getResources().getColor(R.color.colorUp));
        } else {
            views.setTextColor(R.id.changeTextView, context.getResources().getColor(R.color.colorDown));
        }
        views.setTextViewText(R.id.timeTextView, security.tradeDate + " " + security.time);
        views.setTextViewText(R.id.valueTextView, security.currentValue);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.i("RTSIWidget", "onUpdate");
        URL url = null;
        try {
            url = new URL(context.getString(R.string.web_service));
        } catch (Exception e) {
            e.printStackTrace();
        }

        new GetTickerValueTask(context, appWidgetManager, appWidgetIds).execute(url);
    }

    private class GetTickerValueTask extends AsyncTask<URL, Void, Security> {

        private Context context;
        private AppWidgetManager appWidgetManager;
        private int[] appWidgetIds;

        public GetTickerValueTask(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            this.context = context;
            this.appWidgetManager = appWidgetManager;
            this.appWidgetIds = appWidgetIds;
        }

        @Override
        protected Security doInBackground(URL... params) {
            Log.i("RTSIWidget", "doInBackground");
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(connection.getInputStream(), "utf-8");
                    while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                        if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equals("row") && parser.getAttributeCount() >= 25) {
                            String secid = parser.getAttributeValue("", "SECID");
                            String currentvalue = parser.getAttributeValue("", "CURRENTVALUE");
                            String lastchangeprc = parser.getAttributeValue("", "LASTCHANGEPRC");
                            double lastchangeprc_d = Double.parseDouble(lastchangeprc);
                            if (lastchangeprc_d >= 0) {
                                lastchangeprc = "+" + lastchangeprc;
                            } else {
                                lastchangeprc = "-" + lastchangeprc;
                            }
                            String time = parser.getAttributeValue("", "TIME");
                            String tradedate = parser.getAttributeValue("", "TRADEDATE");
                            Log.i("RTSIWidget", "parser.getAttributeCount() = " + parser.getAttributeCount());
                            return new Security(secid, currentvalue, lastchangeprc, time, tradedate);
                        }
                        parser.next();
                    }
                } else {
                    Log.i("RTSIWidget", "response != HttpURLConnection.HTTP_OK");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Security security) {
            if (security == null)
                return;
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, security);
            }
        }
    }
}

