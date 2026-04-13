import React from 'react';

const statusColor = {
  SUCCESS: '#48bb78',
  FAILURE: '#fc8181',
  UNSTABLE: '#f6ad55',
  IN_PROGRESS: '#63b3ed',
  ABORTED: '#a0aec0',
};

function timeAgo(ts) {
  if (!ts) return '—';
  const diff = Date.now() - ts;
  const m = Math.floor(diff / 60000);
  if (m < 1) return 'now';
  if (m < 60) return `${m}m`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h`;
  return `${Math.floor(h / 24)}d`;
}

function formatDur(ms) {
  const s = Math.floor(ms / 1000);
  return s < 60 ? `${s}s` : `${Math.floor(s / 60)}m`;
}

export default function BuildHistoryBar({ builds }) {
  return (
    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
      {builds.slice(0, 10).map(b => (
        <div
          key={b.number}
          title={`#${b.number} — ${b.result} — ${formatDur(b.duration)} — ${timeAgo(b.timestamp)} ago`}
          style={{
            width: 28,
            height: 28,
            borderRadius: 6,
            background: (statusColor[b.result] || '#718096') + '33',
            border: `2px solid ${statusColor[b.result] || '#718096'}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 9,
            color: statusColor[b.result] || '#718096',
            fontWeight: 700,
            cursor: 'default',
          }}
        >
          {b.number}
        </div>
      ))}
    </div>
  );
}
