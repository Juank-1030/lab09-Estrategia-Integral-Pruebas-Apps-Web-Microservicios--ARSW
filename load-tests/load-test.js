import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '30s', target: 30 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

export default function () {
  const payload = JSON.stringify({
    customerId: `CUS-${__VU}`,
    total: 120000,
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const postResponse = http.post('http://localhost:8080/orders', payload, params);

  check(postResponse, {
    'created': (r) => r.status === 201,
    'duration < 800ms': (r) => r.timings.duration < 800,
  });

  if (postResponse.status === 201) {
    const orderId = postResponse.json('id');
    const getResponse = http.get(`http://localhost:8080/orders/${orderId}`);

    check(getResponse, {
      'order found': (r) => r.status === 200,
      'get duration < 500ms': (r) => r.timings.duration < 500,
    });
  }

  sleep(1);
}
