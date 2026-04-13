import React, { useEffect, useState, useCallback } from 'react';
import StatCard from './components/StatCard';
import PipelineCard from './components/PipelineCard';
import FailureTracker from './components/FailureTracker';
import MetricsChart from './components/MetricsChart';
import SuccessRateChart from './components/SuccessRateChart';
import { api } from './services/api';

const POLL_INTERVAL = 30000; // 30s

const styles = {
  app: { minHeight: '100vh', background: '#0f1117', color: '#e2e8f0', padding: '0 0 40px' },
  header: {
    background: '#1a1f2e',
    borderBottom: '1px solid #2d3748',
    padding: '0 32px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    height: 60,
  },
  logo: { fontSize: 20, fontWeight: 700, color: '#63b3ed', letterSpacing: -0.5 },
  logoSub: { fontSize: 12, color: '#718096', marginLeft: 8 },
  headerRight: { display: 'flex', alignItems: 'center', gap: 16 },
  refreshBtn: {
    background: 'none', border: '1px solid #2d3748', borderRadius: 8,
    color: '#a0aec0', cursor: 'pointer', padding: '6px 14px', fontSize: 13,
    transition: 'all 0.2s',
  },
  pulse: { width: 8, height: 8, borderRadius: '50%', background: '#48bb78', animation: 'pulse 2s infinite' },
  liveLabel: { fontSize: 12, color: '#48bb78' },
  main: { padding: '24px 32px', maxWidth: 1400, margin: '0 auto' },
  sectionTitle: { fontSize: 13, color: '#718096', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 12, marginTop: 28 },
  statsRow: { display: 'flex', gap: 16, flexWrap: 'wrap', marginBottom: 8 },
  chartsRow: { display: 'grid', gridTemplateColumns: '1fr 280px', gap: 16, marginBottom: 8 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 14 },
  error: { background: '#fc818122', border: '1px solid #fc8181', borderRadius: 8, padding: '10px 16px', fontSize: 13, color: '#fc8181', marginBottom: 16 },
  timestamp: { fontSize: 12, color: '#718096' },
};

function formatDur(ms) {
  if (!ms) return '—';
  const s = Math.floor(ms / 1000);
  return s < 60 ? `${s}s` : `${Math.floor(s / 60)}m ${s % 60}s`;
}

export default function App() {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  const fetchData = useCallback(async (manual = false) => {
    try {
      if (manual) setLoading(true);
      const data = manual ? await api.refresh() : await api.getMetrics();
      setMetrics(data);
      setLastUpdated(new Date());
      setError(null);
    } catch (e) {
      setError('Could not reach backend. Showing cached or mock data.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
    const id = setInterval(() => fetchData(), POLL_INTERVAL);
    return () => clearInterval(id);
  }, [fetchData]);

  const jobs = metrics?.jobs || [];
  const failures = metrics?.recentFailures || [];

  return (
    <div style={styles.app}>
      <style>{`@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }`}</style>
      <header style={styles.header}>
        <div style={{ display: 'flex', alignItems: 'baseline' }}>
          <span style={styles.logo}>DeployLens</span>
          <span style={styles.logoSub}>Jenkins Pipeline Monitor</span>
        </div>
        <div style={styles.headerRight}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <div style={styles.pulse} />
            <span style={styles.liveLabel}>Live</span>
          </div>
          {lastUpdated && (
            <span style={styles.timestamp}>
              Updated {lastUpdated.toLocaleTimeString()}
            </span>
          )}
          <button style={styles.refreshBtn} onClick={() => fetchData(true)} disabled={loading}>
            {loading ? 'Refreshing…' : 'Refresh'}
          </button>
        </div>
      </header>

      <main style={styles.main}>
        {error && <div style={styles.error}>{error}</div>}

        <div style={styles.sectionTitle}>Summary</div>
        <div style={styles.statsRow}>
          <StatCard label="Total Pipelines" value={metrics?.totalJobs ?? '—'} />
          <StatCard label="Healthy" value={metrics?.healthyJobs ?? '—'} color="#48bb78" sub="last build success" />
          <StatCard label="Failing" value={metrics?.failingJobs ?? '—'} color="#fc8181" sub="last build failed" />
          <StatCard label="Unstable" value={metrics?.unstableJobs ?? '—'} color="#f6ad55" />
          <StatCard label="Avg Duration" value={formatDur(metrics?.averageBuildDuration)} color="#63b3ed" />
        </div>

        <div style={styles.sectionTitle}>Analytics</div>
        <div style={styles.chartsRow}>
          <MetricsChart jobs={jobs} />
          <SuccessRateChart rate={metrics?.overallSuccessRate ?? 0} />
        </div>

        <div style={styles.sectionTitle}>Recent Failures</div>
        <FailureTracker failures={failures} />

        <div style={styles.sectionTitle}>Pipelines ({jobs.length})</div>
        <div style={styles.grid}>
          {jobs.map(job => (
            <PipelineCard key={job.name} job={job} />
          ))}
        </div>
      </main>
    </div>
  );
}
