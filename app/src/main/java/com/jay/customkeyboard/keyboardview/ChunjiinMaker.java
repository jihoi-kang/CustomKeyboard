package com.jay.customkeyboard.keyboardview;

import android.view.inputmethod.InputConnection;

import java.util.Arrays;
import java.util.List;

public class ChunjiinMaker extends HangulMaker {
    private char testChar = '\u0000';
    private boolean isComposingMoum = false;
    private boolean onlyMoum = false;
    private final List<Character> gkList = Arrays.asList('ㄱ', 'ㅋ', 'ㄲ');
    private final List<Character> nlList = Arrays.asList('ㄴ', 'ㄹ');
    private final List<Character> dtList = Arrays.asList('ㄷ', 'ㅌ', 'ㄸ');
    private final List<Character> bpList = Arrays.asList('ㅂ', 'ㅍ', 'ㅃ');
    private final List<Character> shList = Arrays.asList('ㅅ', 'ㅎ', 'ㅆ');
    private final List<Character> jchList = Arrays.asList('ㅈ', 'ㅊ', 'ㅉ');
    private final List<Character> aiueomList = Arrays.asList('ㅇ', 'ㅁ');
    private final List<String> commonKeywords = Arrays.asList(".", ",", "?", "!");
    private final List<List<Character>> wholeList = Arrays.asList(gkList, nlList, dtList, bpList, shList, jchList, aiueomList);
    private List<Character> myList = null;
    private int listIndex = 0;
    private char junFlagChunjiin = '\u0000';
    private int keywordIndex = 0;
    boolean keywordExpect = false;
    private boolean stateThreeDot = false;

    public ChunjiinMaker(InputConnection inputConnection) {
        super(inputConnection);
    }

    @Override
    public void commit(char c) {
        if (keywordExpect) {
            inputConnection.finishComposingText();
            keywordExpect = false;
        }
        if (c == 'ㅣ' || c == '·' || c == 'ㅡ') { // 모음구성
            if (state == 0) { // 모음만으로 구성된 글자 ex) ㅠㅠㅠㅠㅠㅠㅠ
                onlyMoum = true;
                if (testChar == '\u0000') {
                    testChar = c;
                    inputConnection.setComposingText(String.valueOf(testChar), 1);
                } else if (combination(c)) {
                    inputConnection.setComposingText(String.valueOf(testChar), 1);
                } else {
                    inputConnection.commitText(String.valueOf(testChar), 1);
                    testChar = c;
                    inputConnection.setComposingText(String.valueOf(testChar), 1);
                }
                junFlagChunjiin = '\u0000';
            } else if (!isComposingMoum) {
                onlyMoum = false;
                testChar = c;
                if (c == '·') {
                    if (state == 3) {
                        // 종성까지 추가된 상태
                        inputConnection.setComposingText(String.valueOf(makeHan()) + testChar, 2);
                        stateThreeDot = true;
                    } else if (!junAvailable()) { // 더이상 추가될 수 없는 모음일 경우 ex) 왜 + ·
                        directlyCommit();
                        inputConnection.setComposingText(String.valueOf(testChar), 1);
                    } else {
                        inputConnection.setComposingText(String.valueOf(makeHan()) + testChar, 2);
                        state = 2;
                    }
                } else {
                    commit(testChar);
                }
                listIndex = 0;
                isComposingMoum = true;
            } else {
                if (combination(c)) { // 이중모음으로 선언 가능한 경우
                    if (testChar == '‥') {
                        inputConnection.setComposingText(String.valueOf(makeHan()) + testChar, 2);
                    } else if (state == 2) { // 모음이 기대되는 상태
                        if (isDoubleJun()) { // 이중모음인 경우 두 모음을 모두 지운다.
                            delete();
                            delete();
                        } else { // 이중모음이 아닌 경우
                            delete();
                        }
                        commit(testChar);
                        junFlag = junFlagChunjiin; // 이중 모음일 경우 이전 모음을 설정한다.
                        junFlagChunjiin = '\u0000'; // 초기화
                    } else {
                        commit(testChar);
                    }
                } else { // 이 + ㅣ와 같은 경우 이전 글자를 commit하고 모음으로만 구성된다고 설정한다.
                    directlyCommit();
                    testChar = c;
                    inputConnection.setComposingText(String.valueOf(testChar), 1);
                    isComposingMoum = false;
                    onlyMoum = true;
                }
            }

        } else if (myList == null) { // 첫입력
            if (onlyMoum) { // 모음으로만 구성된 문자를 작성중이었다면 commit한다.
                inputConnection.commitText(String.valueOf(testChar), 1);
            }
            onlyMoum = false;
            testChar = c;
            for (int i = 0; i < wholeList.size(); i++) { // 전체 리스트를 순회하며 현재 텍스트를 포함하는 리스트를 찾는다.
                if (wholeList.get(i).indexOf(testChar) >= 0) {
                    myList = wholeList.get(i);
                    listIndex = 1;
                }
            }
            commit(testChar);
            isComposingMoum = false;
        } else if (myList != null && myList.indexOf(c) >= 0) { // 현재 작성중인 문자에서 파생될수 있는 문자를 출력 ex) ㄱ -> ㅋ
            if (onlyMoum) {
                inputConnection.commitText(String.valueOf(testChar), 1);
            }
            onlyMoum = false;
            if (listIndex == myList.size()) { // 더이상 파생할 수 없을 경우 첫번째 문자로 돌아간다. ex) ㄲ -> ㄱ
                listIndex = 0;
            }
            testChar = myList.get(listIndex);
            listIndex++;
            if (state == 1 || state == 3) { // 단어를 대체하기 위하여 delete한 뒤 새로운 문자를 commit한다.
                delete();
            }
            commit(testChar);
            isComposingMoum = false;
        } else {
            onlyMoum = false;
            testChar = c;
            for (int i = 0; i < wholeList.size(); i++) {
                if (wholeList.get(i).indexOf(testChar) >= 0) {
                    myList = wholeList.get(i);
                    listIndex = 1;
                }
            }
            commit(c);
            isComposingMoum = false;
        }
    }

    private boolean combination(char c) { // 모음을 구성하기 위한 조합 성공시 true리턴
        switch (testChar) {
            case 'ㅣ':
                if (c == '·') {
                    testChar = 'ㅏ';
                    return true;
                } else {
                    return false;
                }
            case '·':
                if (c == 'ㅡ') {
                    testChar = 'ㅗ';
                    return true;
                } else if (c == 'ㅣ') {
                    testChar = 'ㅓ';
                    return true;
                } else if (c == '·') {
                    testChar = '‥';
                    return true;
                } else {
                    return false;
                }
            case '‥':
                if (c == 'ㅣ') {
                    testChar = 'ㅕ';
                    return true;
                } else if (c == 'ㅡ') {
                    testChar = 'ㅛ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅡ':
                if (c == 'ㅣ') {
                    testChar = 'ㅢ';
                    junFlagChunjiin = 'ㅡ';
                    return true;
                } else if (c == '·') {
                    testChar = 'ㅜ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅏ':
                if (c == '·') {
                    testChar = 'ㅑ';
                    return true;
                } else if (c == 'ㅣ') {
                    testChar = 'ㅐ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅓ':
                if (c == 'ㅣ') {
                    testChar = 'ㅔ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅑ':
                if (c == 'ㅣ') {
                    testChar = 'ㅒ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅕ':
                if (c == 'ㅣ') {
                    testChar = 'ㅖ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅜ':
                if (c == '·') {
                    testChar = 'ㅠ';
                    return true;
                }
                if (c == 'ㅣ') {
                    testChar = 'ㅟ';
                    junFlagChunjiin = 'ㅜ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅠ':
                if (c == 'ㅣ') {
                    testChar = 'ㅝ';
                    junFlagChunjiin = 'ㅜ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅗ':
                if (c == 'ㅣ') {
                    testChar = 'ㅚ';
                    junFlagChunjiin = 'ㅗ';
                    return true;
                } else {
                    return false;
                }
            case 'ㅚ':
                if (c == '·') {
                    testChar = 'ㅘ';
                    junFlagChunjiin = 'ㅗ';
                    return true;
                }
                return false;
            case 'ㅘ':
                if (c == 'ㅣ') {
                    testChar = 'ㅙ';
                    junFlagChunjiin = 'ㅗ';
                    return true;
                }
                return false;
            case 'ㅝ':
                if (c == 'ㅣ') {
                    testChar = 'ㅞ';
                    junFlagChunjiin = 'ㅜ';
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public void directlyCommit() {
        directlyCommit();
        inputConnection.finishComposingText();
        clearChunjiin();
    }

    @Override
    public void delete() {
        if (onlyMoum) { // 현재 커서가 모음으로만 구성된 문자일 경우
            inputConnection.setComposingText("", 1);
            clearChunjiin();
        } else if (stateThreeDot) { // HangulMaker의 3번상태(자음+모음+자음)상태에서 .기호가 들어와 있는 상태
            inputConnection.setComposingText(String.valueOf(makeHan()), 1);
            clearChunjiin();
            stateThreeDot = false;
        } else if (state == 2 && isDoubleJun()) {//상태 2이면서 이중모음이 들어와 있는 상태
            delete();
            setTestCharBefore();
        } else {
            clearChunjiin();
            delete();
        }
        listIndex = 0;
    }

    public void commonKeywordCommit() { // 특수 문자 입력 시
        directlyCommit();
        if (keywordIndex == commonKeywords.size()) {
            keywordIndex = 0;
        }
        inputConnection.setComposingText(commonKeywords.get(keywordIndex++), 1);
        keywordExpect = true;
    }

    public void clearChunjiin() {
        testChar = '\u0000';
        isComposingMoum = false;
        myList = null;
        listIndex = 0;
        onlyMoum = false;
    }

    public boolean isEmpty() {
        return state == 0 && testChar == '\u0000';
    }

    private void setTestCharBefore() { // 이중모음 이전의 상태를 반환
        if (testChar == 'ㅚ' || testChar == 'ㅘ' || testChar == 'ㅙ') {
            testChar = 'ㅗ';
        } else if (testChar == 'ㅟ' || testChar == 'ㅝ' || testChar == 'ㅞ') {
            testChar = 'ㅜ';
        } else if (testChar == 'ㅢ') {
            testChar = 'ㅡ';
        }
    }

}
