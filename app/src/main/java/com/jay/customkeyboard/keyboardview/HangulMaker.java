package com.jay.customkeyboard.keyboardview;

import android.util.Log;
import android.view.inputmethod.InputConnection;

import java.util.Arrays;
import java.util.List;

public class HangulMaker {
    private char cho = '\u0000';
    private char jun = '\u0000';
    private char jon = '\u0000';
    private char jonFlag = '\u0000';
    private char doubleJonFlag = '\u0000';
    protected char junFlag = '\u0000';

    private final List<Integer> chos = Arrays.asList(0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e);
    private final List<Integer> juns = Arrays.asList(0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163);
    private final List<Integer> jons = Arrays.asList(0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e);

    /**
     * 0:""
     * 1: 모음 입력상태
     * 2: 모음 + 자음 입력상태
     * 3: 모음 + 자음 + 모음입력상태(초 중 종성)
     * 초성과 종성에 들어갈 수 있는 문자가 다르기 때문에 필요에 맞게 수정이 필요함.(chos != jons)
     */
    protected int state = 0;
    protected InputConnection inputConnection;

    public HangulMaker(InputConnection inputConnection) {
        this.inputConnection = inputConnection;
    }

    public void clear() {
        cho = '\u0000';
        jun = '\u0000';
        jon = '\u0000';
        jonFlag = '\u0000';
        doubleJonFlag = '\u0000';
        junFlag = '\u0000';
    }

    public char makeHan() {
        if (state == 0) return '\u0000';
        if (state == 1) return cho;

        int choIndex = chos.indexOf(Integer.valueOf(cho));
        int junIndex = juns.indexOf(Integer.valueOf(jun));
        int jonIndex = jons.indexOf(Integer.valueOf(jon));

        int makeResult = 0xAC00 + (28 * 21 * choIndex) + (28 * junIndex) + jonIndex;

        return (char) makeResult;
    }

    public void commit(char c) {
        Log.d("jay", "commit: " + c);
        if (chos.indexOf(Integer.valueOf(c)) < 0 &&
                juns.indexOf(Integer.valueOf(c)) < 0 &&
                jons.indexOf(Integer.valueOf(c)) < 0) {
            directlyCommit();
            inputConnection.commitText(String.valueOf(c), 1);
            return;
        }

        switch (state) {
            case 0:
                if (juns.indexOf(Integer.valueOf(c)) >= 0) {
                    inputConnection.commitText(String.valueOf(c), 1);
                    clear();
                } else { // 초성일 경우
                    state = 1;
                    cho = c;
                    inputConnection.setComposingText(String.valueOf(cho), 1);
                }
                break;
            case 1:
                if (chos.indexOf(Integer.valueOf(c)) >= 0) {
                    inputConnection.commitText(String.valueOf(c), 1);
                    clear();
                    cho = c;
                    inputConnection.setComposingText(String.valueOf(cho), 1);
                } else { // 중성일 경우
                    state = 2;
                    jun = c;
                    inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                }
                break;
            case 2:
                if (juns.indexOf(Integer.valueOf(c)) >= 0) {
                    if (doubleJunEnable(c)) {
                        inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                    } else {
                        inputConnection.commitText(String.valueOf(makeHan()), 1);
                        inputConnection.commitText(String.valueOf(c), 1);
                        clear();
                        state = 0;
                    }
                } else if (jons.indexOf(Integer.valueOf(c)) >= 0) { // 종성이 들어왔을 경우
                    jon = c;
                    inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                    state = 3;
                } else {
                    directlyCommit();
                    cho = c;
                    state = 1;
                    inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                }
                break;
            case 3:
                if (jons.indexOf(Integer.valueOf(c)) >= 0) {
                    if (doubleJonEnable(c)) {
                        inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                    } else {
                        inputConnection.commitText(String.valueOf(makeHan()), 1);
                        clear();
                        state = 1;
                        cho = c;
                        inputConnection.setComposingText(String.valueOf(cho), 1);
                    }
                } else if (chos.indexOf(Integer.valueOf(c)) >= 0) {
                    inputConnection.commitText(String.valueOf(makeHan()), 1);
                    state = 1;
                    clear();
                    cho = c;
                    inputConnection.setComposingText(String.valueOf(cho), 1);
                } else { // 중성이 들어올 경우
                    char temp = '\u0000';
                    if (doubleJonFlag == '\u0000') {
                        temp = jon;
                        jon = '\u0000';
                        inputConnection.commitText(String.valueOf(makeHan()), 1);
                    } else {
                        temp = doubleJonFlag;
                        jon = jonFlag;
                        inputConnection.commitText(String.valueOf(makeHan()), 1);
                    }
                    state = 2;
                    clear();
                    cho = temp;
                    jun = c;
                    inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                }
                break;
        }
    }

    public void commitSpace() {
        directlyCommit();
        inputConnection.commitText(" ", 1);
    }

    public void directlyCommit() {
        if (state == 0) return;

        inputConnection.commitText(String.valueOf(makeHan()), 1);
        state = 0;
        clear();
    }

    public void delete() {
        switch (state) {
            case 0:
                inputConnection.deleteSurroundingText(1, 0);
                break;
            case 1:
                cho = '\u0000';
                state = 0;
                inputConnection.setComposingText("", 1);
                inputConnection.commitText("", 1);
                break;
            case 2:
                if (junFlag != '\u0000') {
                    jun = junFlag;
                    junFlag = '\u0000';
                    state = 2;
                    inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                } else {
                    jun = '\u0000';
                    junFlag = '\u0000';
                    state = 1;
                    inputConnection.setComposingText(String.valueOf(cho), 1);
                }
                break;
            case 3:
                if (doubleJonFlag == '\u0000') {
                    jon = '\u0000';
                    state = 2;
                } else {
                    jon = jonFlag;
                    jonFlag = '\u0000';
                    doubleJonFlag = '\u0000';
                    state = 3;
                }
                inputConnection.setComposingText(String.valueOf(makeHan()), 1);
                break;
        }
    }

    public boolean doubleJunEnable(char c) {
        switch (jun) {
            case 'ㅗ':
                if (c == 'ㅏ') {
                    junFlag = jun;
                    jun = 'ㅘ';
                    return true;
                }
                if (c == 'ㅐ') {
                    junFlag = jun;
                    jun = 'ㅙ';
                    return true;
                }
                if (c == 'ㅣ') {
                    junFlag = jun;
                    jun = 'ㅚ';
                    return true;
                }
                return false;
            case 'ㅜ':
                if (c == 'ㅓ') {
                    junFlag = jun;
                    jun = 'ㅝ';
                    return true;
                }
                if (c == 'ㅔ') {
                    junFlag = jun;
                    jun = 'ㅞ';
                    return true;
                }
                if (c == 'ㅣ') {
                    junFlag = jun;
                    jun = 'ㅟ';
                    return true;
                }
                return false;
            case 'ㅡ':
                if (c == 'ㅣ') {
                    junFlag = jun;
                    jun = 'ㅢ';
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public boolean doubleJonEnable(char c) {
        jonFlag = jon;
        doubleJonFlag = c;

        switch (jon) {
            case 'ㄱ':
                if (c == 'ㅅ') {
                    jon = 'ㄳ';
                    return true;
                }
                return false;
            case 'ㄴ':
                if (c == 'ㅈ') {
                    jon = 'ㄵ';
                    return true;
                }
                if (c == 'ㅎ') {
                    jon = 'ㄶ';
                    return true;
                }
                return false;
            case 'ㄹ':
                if (c == 'ㄱ') {
                    jon = 'ㄺ';
                    return true;
                }
                if (c == 'ㅁ') {
                    jon = 'ㄻ';
                    return true;
                }
                if (c == 'ㅂ') {
                    jon = 'ㄼ';
                    return true;
                }
                if (c == 'ㅅ') {
                    jon = 'ㄽ';
                    return true;
                }
                if (c == 'ㅌ') {
                    jon = 'ㄾ';
                    return true;
                }
                if (c == 'ㅍ') {
                    jon = 'ㄿ';
                    return true;
                }
                if (c == 'ㅎ') {
                    jon = 'ㅀ';
                    return true;
                }
                return false;
            case 'ㅂ':
                if (c == 'ㅅ') {
                    jon = 'ㅄ';
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    public boolean junAvailable() {
        return jun != 'ㅙ' && jun != 'ㅞ' && jun != 'ㅢ' && jun != 'ㅐ' && jun != 'ㅔ' && jun != 'ㅛ' && jun != 'ㅒ' && jun != 'ㅖ';
    }

    public boolean isDoubleJun() {
        return jun == 'ㅙ' || jun == 'ㅞ' || jun == 'ㅚ' || jun == 'ㅝ' || jun == 'ㅟ' || jun == 'ㅘ' || jun == 'ㅢ';
    }

}
