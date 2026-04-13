import axios from 'axios';

const BASE = '/api/v1/pipelines';

export const api = {
  getMetrics:   () => axios.get(`${BASE}/metrics`).then(r => r.data),
  getJobs:      () => axios.get(`${BASE}/jobs`).then(r => r.data),
  getFailures:  () => axios.get(`${BASE}/failures`).then(r => r.data),
  getHealth:    () => axios.get(`${BASE}/health`).then(r => r.data),
  getBuilds:    (name) => axios.get(`${BASE}/jobs/${encodeURIComponent(name)}/builds`).then(r => r.data),
  refresh:      () => axios.post(`${BASE}/refresh`).then(r => r.data),
};
