import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

export const notificationLatency = new Trend('notification_latency', true);

export const options = {
    scenarios: {
        notification_load_test: {
            executor: 'ramping-vus',  // 동시 사용자수 점진 증가
            startVUs: 0,
            stages: [
                { duration: '20s', target: 50 },   // 워밍업
                { duration: '1m', target: 100 },   // 실제 측정 구간
                { duration: '20s', target: 0 },    // 종료
            ],
            gracefulRampDown: '30s',
        },
    },
    // thresholds: {
    //     // 알림 API에 대해서만 p95 기준 적용 (로그인 제외)
    //     'http_req_duration{name:notifications}': ['p(95)<300'],  // p95가 300ms 미만이어야 성공
    //     'http_req_failed{name:notifications}': ['rate<0.01'],
    // },
};


// 로그인
export function setup() {
    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            email: 's5763305@naver.com',
            password: 'hyoungmi1015',
        }),
        { headers: { 'Content-Type': 'application/json' } }
    );

    check(loginRes, {
        'login success': (r) => r.status === 200,
    });

    const token = loginRes.json('data.accessToken');

    if (!token) {
        throw new Error('login fail');
    }

    return { token };
}

// 실제 부하테스트
export default function (data) {
    const url = `${BASE_URL}/api/v1/notifications?cursor=2026-02-10T20:54:21.399482&size=10`;

    const res = http.get(url, {
        headers: {
            Authorization: `Bearer ${data.token}`,
        },
        tags: {
            name: 'notifications',
        },
    });

    notificationLatency.add(res.timings.duration);

    check(res, {
        'status is 200': (r) => r.status === 200
    });

    if (res.status !== 200) {
        console.log(`status: ${res.status}`);
        console.log(`body: ${res.body}`);
    }

    sleep(0.2); // 실제 사용자 행동 간격
}
