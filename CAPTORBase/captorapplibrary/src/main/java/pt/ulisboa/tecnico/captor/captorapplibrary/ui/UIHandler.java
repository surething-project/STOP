package pt.ulisboa.tecnico.captor.captorapplibrary.ui;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * This Handler presents Toasts to the user
 */
public final class UIHandler {

    /**
     * Show Toast message to user
     * @param activity
     * @param context
     * @param text
     */
    public static void showToast(Activity activity, final Context context, final String text)
    {
        activity.runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
