import React from 'react';

const styles = {
  card: {
    background: '#1a1f2e',
    border: '1px solid #2d3748',
    borderRadius: 12,
    padding: '20px 24px',
    flex: 1,
    minWidth: 160,
  },
  label: { fontSize: 12, color: '#718096', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 8 },
  value: { fontSize: 36, fontWeight: 700, lineHeight: 1 },
  sub: { fontSize: 13, color: '#718096', marginTop: 6 },
};

export default function StatCard({ label, value, sub, color = '#e2e8f0' }) {
  return (
    <div style={styles.card}>
      <div style={styles.label}>{label}</div>
      <div style={{ ...styles.value, color }}>{value}</div>
      {sub && <div style={styles.sub}>{sub}</div>}
    </div>
  );
}
