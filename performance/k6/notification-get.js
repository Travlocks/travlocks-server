import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

/**
 * ===============================
 * Environment Variables
 * ===============================
 * BASE_URL: API 서버 주소
 * TOKEN: JWT 토큰
 */

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const TOKEN = __ENV.TOKEN || 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzcwODY4OTYyLCJleHAiOjE3NzA4NzI1NjJ9.qaLGokMW3_ZZp-ineo6g-tuoJhvGMDHvTLPQ28uK_v4';

/**
 * 커스텀 메트릭 (p95 집중 분석용)
 */
export const notificationLatency = new Trend('notification_latency');

export const options = {
    scenarios: {
        notification_get_test: {
            executor: 'ramping-vus',
            startVUs: 10,
            stages: [
                { duration: '30s', target: 50 },   // 50명까지 증가
                { duration: '1m', target: 100 },   // 100명 유지
                { duration: '30s', target: 0 },    // 종료
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],       // 실패율 1% 이하
        http_req_duration: ['p(95)<300'],     // p95 300ms 이하 목표
    },
};

export default function () {
    const url = `${BASE_URL}/api/v1/notifications?size=20`;

    const params = {
        headers: {
            'Authorization': `Bearer ${TOKEN}`,
            'Content-Type': 'application/json',
        },
    };

    const res = http.get(url, params);

    // 메트릭 수집
    notificationLatency.add(res.timings.duration);

    // 검증
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1); // 요청 간 간격
}