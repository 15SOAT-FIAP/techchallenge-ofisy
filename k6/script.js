import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 20 },  // Sobe carga gradual
        { duration: '20s', target: 50 },  // Mantem subindo
        { duration: '20s', target: 100 }, // Força escalar mais ainda
        { duration: '30s', target: 150 }, // Pico de carga
        { duration: '30s', target: 0 },   // Desce carga gradual
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'], // Taxa de falhas deve ser menor que 5%
    }
};

export default function () {
    const res = http.get('http://localhost:8080/api/v1/notifications');
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1);
}