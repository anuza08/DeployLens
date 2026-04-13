import React from 'react';

function timeAgo(ts) {
  if (!ts) return '—';
  const diff = Date.now() - ts;
  const m = Math.floor(diff / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  return `${Math.floor(h / 24)}d ago`;
}

function formatDur(ms) {
  if (!ms) return '—';
  const s = Math.floor(ms / 1000);
  return s < 60 ? `${s}s` : `${Math.floor(s / 60)}m ${s % 60}s`;
}

const styles = {
  container: { background: '#1a1f2e', border: '1px solid #2d3748', borderRadius: 12, padding: 20 },
  title: { fontSize: 15, fontWeight: 600, marginBottom: 16, color: '#fc8181' },
  empty: { color: '#48bb78', fontSize: 14, textAlign: 'center', padding: 20 },
  row: {
    display: 'flex', alignItems: 'center', gap: 12,
    padding: '10px 0', borderBottom: '1px solid #1e2535',
    fontSize: 13,
  },
  dot: { width: 8, height: 8, borderRadius: '50%', background: '#fc8181', flexShrink: 0 },
  job: { flex: 1, color: '#e2e8f0', fontWeight: 500 },
  build: { color: '#718096', minWidth: 50 },
  time: { color: '#718096', minWidth: 60, textAlign: 'right' },
  dur: { color: '#718096', minWidth: 50, textAlign: 'right' },
};

export default function FailureTracker({ failures }) {
  return (
    <div style={styles.container}>
      <div style={styles.title}>Recent Failures</div>
      {failures.length === 0 ? (
        <div style={styles.empty}>No recent failures</div>
      ) : (
        failures.map((f, i) => (
          <div key={i} style={{ ...styles.row, borderBottom: i === failures.length - 1 ? 'none' : '1px solid #1e2535' }}>
            <div style={styles.dot} />
            <div style={styles.job}>{f.jobName}</div>
            <div style={styles.build}>#{f.number}</div>
            <div style={styles.dur}>{formatDur(f.duration)}</div>
            <div style={styles.time}>{timeAgo(f.timestamp)}</div>
          </div>
        ))
      )}
    </div>
  );
}
