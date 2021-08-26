package com.jay.customkeyboard.keyboardview;

import static android.content.Context.AUDIO_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jay.customkeyboard.KeyboardInteractionListener;
import com.jay.customkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardNumpad {
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final KeyboardInteractionListener keyboardInteractionListener;

    private final LinearLayout numpadLayout;
    public InputConnection inputConnection = null;
    private final Vibrator vibrator;
    private final ArrayList<Button> buttons = new ArrayList<>();

    private final List<String> firstLineText = Arrays.asList("1", "2", "3", "DEL");
    private final List<String> secondLineText = Arrays.asList("4", "5", "6", "Enter");
    private final List<String> thirdLineText = Arrays.asList("7", "8", "9", ".");
    private final List<String> fourthLineText = Arrays.asList("-", "0", ",", "");
    private final ArrayList<List<String>> myKeysText = new ArrayList<>();
    private final ArrayList<LinearLayout> layoutLines = new ArrayList<>();
    private final int sound = 0;
    private final int vibrate = 0;

    private KeyboardNumpad(Context context, LayoutInflater layoutInflater, InputConnection inputConnection, KeyboardInteractionListener keyboardInteractionListener) {
        numpadLayout = (LinearLayout) layoutInflater.inflate(R.layout.keyboard_numpad, null);
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.inputConnection = inputConnection;
        this.keyboardInteractionListener = keyboardInteractionListener;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int height = sharedPreferences.getInt("keyboardHeight", 150);
        Configuration config = context.getResources().getConfiguration();

        LinearLayout firstLine = numpadLayout.findViewById(R.id.first_line);
        LinearLayout secondLine = numpadLayout.findViewById(R.id.second_line);
        LinearLayout thirdLine = numpadLayout.findViewById(R.id.third_line);
        LinearLayout fourthLine = numpadLayout.findViewById(R.id.fourth_line);

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            firstLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            secondLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            thirdLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            fourthLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
        } else {
            firstLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            secondLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            thirdLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            fourthLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
        }

        myKeysText.clear();
        myKeysText.add(firstLineText);
        myKeysText.add(secondLineText);
        myKeysText.add(thirdLineText);
        myKeysText.add(fourthLineText);

        layoutLines.clear();
        layoutLines.add(firstLine);
        layoutLines.add(secondLine);
        layoutLines.add(thirdLine);
        layoutLines.add(fourthLine);

        setLayoutComponents();
    }

    public static KeyboardNumpad newInstance(Context context, LayoutInflater layoutInflater, InputConnection inputConnection, KeyboardInteractionListener keyboardInteractionListener) {
        return new KeyboardNumpad(context, layoutInflater, inputConnection, keyboardInteractionListener);
    }

    public LinearLayout getLayout() {
        return numpadLayout;
    }

    private void playClick(int i) {
        AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        switch (i) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1F);
                break;
        }
    }

    private void playVibrate() {
        if (vibrate > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(70, vibrate));
            } else {
                vibrator.vibrate(70);
            }
        }
    }

    private void setLayoutComponents() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int vibrate = sharedPreferences.getInt("keyboardVibrate", -1);

        for (int line = 0; line < layoutLines.size(); line++) {
            LinearLayout linearLayout = layoutLines.get(line);
            List<String> myText = myKeysText.get(line);
            int itemCount = linearLayout.getChildCount();
            for (int item = 0; item < itemCount; item++) {
                Button actionButton = linearLayout.getChildAt(item).findViewById(R.id.key_button);
                actionButton.setText(myText.get(item));

                buttons.add(actionButton);

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (vibrate > 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(70, vibrate));
                            } else {
                                vibrator.vibrate(70);
                            }
                        }

                        switch (actionButton.getText().toString()) {
                            case "DEL":
                                inputConnection.deleteSurroundingText(1, 0);
                                break;
                            case "Enter":
                                long eventTime = SystemClock.uptimeMillis();
                                inputConnection.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                                        KeyEvent.FLAG_SOFT_KEYBOARD));
                                inputConnection.sendKeyEvent(new KeyEvent(
                                        SystemClock.uptimeMillis(), eventTime,
                                        KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                                        KeyEvent.FLAG_SOFT_KEYBOARD));
                                break;
                            default:
                                playClick(Integer.valueOf(actionButton.getText().toString().toCharArray()[0]));
                                inputConnection.commitText(actionButton.getText().toString(), 1);
                                break;
                        }
                    }
                };

                actionButton.setOnClickListener(clickListener);
                linearLayout.getChildAt(item).setOnClickListener(clickListener);
            }
        }

    }

}
