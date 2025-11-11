package de.hype.hypenotify.app.screen.features

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import de.hype.hypenotify.R
import de.hype.hypenotify.app.core.interfaces.Core
import de.hype.hypenotify.app.screen.Screen
import de.hype.hypenotify.layouts.autodetection.Layout

@Layout(name = "Timers")
class ShowTimersLayout(core: Core, parent: View?) : Screen(core, parent) {
    private val timerListView: ListView
    private val checkValidityButton: Button
    private val timerAdapter: ArrayAdapter<String?>?

    init {
        timerListView = ListView(context)
        checkValidityButton = Button(context)
        checkValidityButton.setText(R.string.check_timers_validity)

        setOrientation(VERTICAL)
        addView(timerListView)
        addView(checkValidityButton)

        timerAdapter = ArrayAdapter<String?>(context, android.R.layout.simple_list_item_1)
        timerListView.setAdapter(timerAdapter)

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
        checkValidityButton.setOnClickListener(OnClickListener { view: View? -> checkTimersValidity() })
    }

    override fun inflateLayouts() {
    }

    override fun updateScreen(dynamicScreen: LinearLayout) {
    }

    override fun getDynamicScreen(): LinearLayout? {
        return null
    }

    private fun checkTimersValidity() {
        //TODO
//        new Thread(() -> {
//            for (TimerData timer : timers) {
//                try {
//                    // Add apiKey and userId parameters
//                    URL url = new URL(/hypenotify/checkTimer?id="
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

    override fun onPause() {
    }

    override fun onResume() {
    }
}
