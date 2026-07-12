import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 100 }, // Sobe rapido para 100 usuários
        { duration: '30s', target: 150 },
        { duration: '90s', target: 200 }, // Sustenta o pico (dá tempo do HPA reagir em vários ciclos)
        { duration: '20s', target: 0 },   // Desce carga
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