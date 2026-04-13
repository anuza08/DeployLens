import React, { useState } from 'react';
import { api } from '../services/api';
import BuildHistoryBar from './BuildHistoryBar';

const statusColor = {
  SUCCESS: '#48bb78',
  FAILURE: '#fc8181',
  UNSTABLE: '#f6ad55',
  IN_PROGRESS: '#63b3ed',
  ABORTED: '#a0aec0',
  UNKNOWN: '#718096',
};

const styles = {
  card: {
    background: '#1a1f2e',
    border: '1px solid #2d3748',
    borderRadius: 12,
    padding: 20,
    cursor: 'pointer',
    transition: 'border-color 0.2s',
  },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 },
  name: { fontSize: 15, fontWeight: 600, color: '#e2e8f0', wordBreak: 'break-all' },
  badge: { borderRadius: 6, padding: '3px 10px', fontSize: 12, fontWeight: 600, whiteSpace: 'nowrap', marginLeft: 8 },
  stats: { display: 'flex', gap: 16, marginTop: 12, fontSize: 13, color: '#a0aec0' },
  stat: { display: 'flex', flexDirection: 'column', gap: 2 },
  statVal: { color: '#e2e8f0', fontWeight: 600, fontSize: 16 },
  builds: { marginTop: 14 },
  buildLabel: { fontSize: 11, color: '#718096', marginBottom: 6, textTransform: 'uppercase', letterSpacing: 0.8 },
};

function formatDuration(ms) {
  if (!ms) return '—';
  const s = Math.floor(ms / 1000);
  return s < 60 ? `${s}s` : `${Math.floor(s / 60)}m ${s % 60}s`;
}

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

export default function PipelineCard({ job }) {
  const [builds, setBuilds] = useState(job.recentBuilds || []);
  const [expanded, setExpanded] = useState(false);
  const color = statusColor[job.lastBuildStatus] || '#718096';

  const handleExpand = async () => {
    if (!expanded) {
      try {
        const data = await api.getBuilds(job.name);
        setBuilds(data);
      } catch (_) {}
    }
    setExpanded(e => !e);
  };

  return (
    <div
      style={{ ...styles.card, borderColor: expanded ? color : '#2d3748' }}
      onClick={handleExpand}
    >
      <div style={styles.header}>
        <div style={styles.name}>{job.name}</div>
        <span style={{ ...styles.badge, background: color + '22', color }}>
          {job.lastBuildStatus}
        </span>
      </div>

      <div style={styles.stats}>
        <div style={styles.stat}>
          <span>Success Rate</span>
          <span style={{ ...styles.statVal, color: job.successRate >= 70 ? '#48bb78' : job.successRate >= 40 ? '#f6ad55' : '#fc8181' }}>
            {job.successRate}%
          </span>
        </div>
        <div style={styles.stat}>
          <span>Build #{job.lastBuildNumber}</span>
          <span style={styles.statVal}>{timeAgo(job.lastBuildTimestamp)}</span>
        </div>
        <div style={styles.stat}>
          <span>Avg Duration</span>
          <span style={styles.statVal}>{formatDuration(job.lastBuildDuration)}</span>
        </div>
        <div style={styles.stat}>
          <span>Failures</span>
          <span style={{ ...styles.statVal, color: job.failureCount > 0 ? '#fc8181' : '#48bb78' }}>
            {job.failureCount}
          </span>
        </div>
      </div>

      {expanded && builds.length > 0 && (
        <div style={styles.builds} onClick={e => e.stopPropagation()}>
          <div style={styles.buildLabel}>Recent Builds</div>
          <BuildHistoryBar builds={builds} />
        </div>
      )}
    </div>
  );
}
