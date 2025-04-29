package de.hype.hypenotify.screen.features;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.hype.hypenotify.R;
import de.hype.hypenotify.core.interfaces.Core;
import de.hype.hypenotify.layouts.autodetection.Layout;
import de.hype.hypenotify.screen.Screen;


@Layout(name = "Timers")
public class ShowTimersLayout extends Screen {
    private ListView timerListView;
    private Button checkValidityButton;
    private ArrayAdapter<String> timerAdapter;

    public ShowTimersLayout(Core core, View parent) {
        super(core, parent);
        timerListView = new ListView(context);
        checkValidityButton = new Button(context);
        checkValidityButton.setText(R.string.check_timers_validity);

        setOrientation(LinearLayout.VERTICAL);
        addView(timerListView);
        addView(checkValidityButton);

        timerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        timerListView.setAdapter(timerAdapter);
//TODO
//        timerListView.setOnItemClickListener((parent, view, position, id) -> {
//            TimerData timer = timers.get(position);
//            if (timer.active) {
//                cancelTimer(timer);
//                timer.active = false;
//            } else {
//                timer.active = true;
//                scheduleTimer(timer);
//            }
//            timerAdapter.clear();
//            timerAdapter.addAll(getTimerDescriptions());
//            timerAdapter.notifyDataSetChanged();
//        });

        checkValidityButton.setOnClickListener(view -> checkTimersValidity());
    }

    @Override
    protected void inflateLayouts() {

    }

    @Override
    protected void updateScreen(LinearLayout dynamicScreen) {

    }

    @Override
    protected LinearLayout getDynamicScreen() {
        return null;
    }

    private void checkTimersValidity() {
        //TODO
//        new Thread(() -> {
//            for (TimerData timer : timers) {
//                try {
//                    // Add apiKey and userId parameters
//                    URL url = new URL("http://hackthetime.de:8085/hypenotify/checkTimer?id="
//                            + timer.id + "&apiKey=" + apiKey
//                            + "&userId=" + deviceName);
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setRequestMethod("GET");
//                    conn.setConnectTimeout(5000);
//                    conn.setReadTimeout(5000);
//                    int responseCode = conn.getResponseCode();
//                    if (responseCode == 200) {
//                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        String inputLine;
//                        StringBuilder response = new StringBuilder();
//                        while ((inputLine = in.readLine()) != null) {
//                        }
//                        in.close();
//                        JSONObject json = new JSONObject(response.toString());
//                        if (!valid) {
//                            timer.active = false;
//                            cancelTimer(timer);
//                            if (json.has("replacementTimer")) {
//                                timer.active = true;
//                                scheduleTimer(timer);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, "Error checking timer validity: ", e);
//                }
//            }
//            runOnUiThread(() -> {
//                timerAdapter.clear();
//                timerAdapter.addAll(getTimerDescriptions());
//                timerAdapter.notifyDataSetChanged();
//            });
//        }).start();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}
