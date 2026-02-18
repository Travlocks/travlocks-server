package org.umc.travlocksserver.infra.aop;

// ✨ AOP에서 AI 호출 횟수를 카운트하고 관리하기 위한 유틸 클래스
public class AiCallCounter {

    private int counter = 0;

    public void increment() {
        counter++;
    }
    public int get() {
        return counter;
    }

    public void reset() {
        counter = 0;
    }
}
